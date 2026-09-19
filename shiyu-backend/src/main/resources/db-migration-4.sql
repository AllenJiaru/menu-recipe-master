-- 组织管理表（替代 couple_config）
-- 执行: mysql -u root -proot -P 3307 shiyu_db < db-migration-4.sql

-- 1. 组织表
CREATE TABLE IF NOT EXISTS `organization` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '组织名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '组织描述',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '组织头像',
  `owner_id` BIGINT NOT NULL COMMENT '创建者用户ID',
  `settings` TEXT DEFAULT NULL COMMENT '组织设置JSON',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织/家庭空间';

-- 2. 组织成员表
CREATE TABLE IF NOT EXISTS `organization_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `organization_id` BIGINT NOT NULL COMMENT '组织ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role` VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT '组织角色: owner/admin/member',
  `nickname_in_org` VARCHAR(50) DEFAULT NULL COMMENT '在组织内的昵称',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_user` (`organization_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织成员';

-- 3. 组织邀请表
CREATE TABLE IF NOT EXISTS `organization_invite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `organization_id` BIGINT NOT NULL COMMENT '组织ID',
  `inviter_id` BIGINT NOT NULL COMMENT '邀请人用户ID',
  `invite_code` VARCHAR(10) NOT NULL COMMENT '邀请码',
  `invitee_username` VARCHAR(50) DEFAULT NULL COMMENT '被邀请人用户名（可选）',
  `role` VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT '邀请加入的角色',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/accepted/expired/revoked',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  KEY `idx_org_id` (`organization_id`),
  KEY `idx_inviter_id` (`inviter_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织邀请记录';

-- 4. 为现有表添加 organization_id 列（如果不存在）
-- recipe 表
SET @exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'shiyu_db' AND table_name = 'recipe' AND column_name = 'organization_id');
SET @sql = IF(@exists = 0, 'ALTER TABLE `recipe` ADD COLUMN `organization_id` BIGINT DEFAULT NULL COMMENT ''组织ID'' AFTER `couple_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- order_record 表
SET @exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'shiyu_db' AND table_name = 'order_record' AND column_name = 'organization_id');
SET @sql = IF(@exists = 0, 'ALTER TABLE `order_record` ADD COLUMN `organization_id` BIGINT DEFAULT NULL COMMENT ''组织ID'' AFTER `couple_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- gallery_image 表
SET @exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'shiyu_db' AND table_name = 'gallery_image' AND column_name = 'organization_id');
SET @sql = IF(@exists = 0, 'ALTER TABLE `gallery_image` ADD COLUMN `organization_id` BIGINT DEFAULT NULL COMMENT ''组织ID'' AFTER `couple_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- sys_user 表
SET @exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'shiyu_db' AND table_name = 'sys_user' AND column_name = 'organization_id');
SET @sql = IF(@exists = 0, 'ALTER TABLE `sys_user` ADD COLUMN `organization_id` BIGINT DEFAULT NULL COMMENT ''当前组织ID'' AFTER `couple_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 5. 数据迁移：couple_config → organization，sys_user.couple_id → organization_member
INSERT INTO `organization` (`name`, `description`, `owner_id`, `create_time`, `update_time`)
SELECT
  CONCAT(space_name, '的家庭空间'),
  CONCAT(chef_name, ' & ', diner_name, ' 的食遇空间'),
  1,
  create_time,
  update_time
FROM `couple_config`
WHERE deleted = 0;

-- 为每对 couple 的用户创建组织成员记录
INSERT IGNORE INTO `organization_member` (`organization_id`, `user_id`, `role`, `nickname_in_org`, `join_time`)
SELECT
  o.id,
  u.id,
  CASE WHEN u.role = 'chef' THEN 'admin' ELSE 'member' END,
  CASE WHEN u.role = 'chef' THEN (SELECT chef_name FROM couple_config WHERE id = u.couple_id) ELSE (SELECT diner_name FROM couple_config WHERE id = u.couple_id) END,
  NOW()
FROM `sys_user` u
INNER JOIN `organization` o ON 1=1
WHERE u.couple_id IS NOT NULL AND u.deleted = 0;

-- 将 organization_id 回填到 sys_user
UPDATE `sys_user` u
INNER JOIN `organization_member` om ON u.id = om.user_id
SET u.organization_id = om.organization_id
WHERE u.deleted = 0;

-- 将 organization_id 回填到 recipe
UPDATE `recipe` r
INNER JOIN `organization_member` om ON r.user_id = om.user_id
SET r.organization_id = om.organization_id
WHERE r.deleted = 0;

-- 将 organization_id 回填到 order_record
UPDATE `order_record` o
INNER JOIN `organization_member` om ON o.user_id = om.user_id
SET o.organization_id = om.organization_id
WHERE o.deleted = 0;

-- 将 organization_id 回填到 gallery_image
UPDATE `gallery_image` g
INNER JOIN `organization_member` om ON g.user_id = om.user_id
SET g.organization_id = om.organization_id
WHERE g.deleted = 0;
