SET NAMES utf8mb4;

-- Roles table
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  description VARCHAR(200) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  sort_order INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Permissions table (menu + button)
CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT DEFAULT 0,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(100) NOT NULL UNIQUE,
  type TINYINT NOT NULL DEFAULT 1 COMMENT '1=directory,2=menu,3=button',
  path VARCHAR(200) DEFAULT NULL,
  component VARCHAR(200) DEFAULT NULL,
  icon VARCHAR(64) DEFAULT NULL,
  sort_order INT DEFAULT 0,
  visible TINYINT DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User-Role relationship
CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role-Permission relationship
CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed data: default roles
INSERT IGNORE INTO sys_role (id, name, code, description, status, sort_order) VALUES
(1, '超级管理员', 'super_admin', '拥有所有权限', 1, 1),
(2, '管理员', 'admin', '管理大部分功能', 1, 2),
(3, '普通用户', 'user', '基本查看权限', 1, 3);

-- Seed data: permissions (matching current navigation)
INSERT IGNORE INTO sys_permission (id, parent_id, name, code, type, path, component, icon, sort_order) VALUES
-- Top-level directories
(1, 0, '概览', 'overview', 1, NULL, NULL, 'Odometer', 1),
(2, 0, '内容管理', 'content', 1, NULL, NULL, 'Notebook', 2),
(3, 0, '业务管理', 'business', 1, NULL, NULL, 'ShoppingCart', 3),
(4, 0, '系统管理', 'system', 1, NULL, NULL, 'Setting', 4),
(5, 0, '日志与监控', 'logs_monitor', 1, NULL, NULL, 'Document', 5),
-- Overview
(10, 1, '仪表盘', 'overview:dashboard', 2, '/dashboard', 'DashboardView', 'Odometer', 1),
-- Content
(20, 2, '菜谱管理', 'content:recipe', 2, '/recipes', 'RecipeListView', 'Notebook', 1),
(21, 2, '分类管理', 'content:category', 2, '/categories', 'CategoryView', 'Grid', 2),
(22, 2, '我的收藏', 'content:favorite', 2, '/favorites', 'FavoriteListView', 'Star', 3),
(23, 2, '美食相册', 'content:gallery', 2, '/gallery', 'GalleryView', 'Picture', 4),
(24, 2, '菜谱审核', 'content:review', 2, '/reviews', 'ReviewView', 'CircleCheck', 5),
-- Content buttons
(201, 20, '新增菜谱', 'content:recipe:create', 3, NULL, NULL, NULL, 1),
(202, 20, '编辑菜谱', 'content:recipe:edit', 3, NULL, NULL, NULL, 2),
(203, 20, '删除菜谱', 'content:recipe:delete', 3, NULL, NULL, NULL, 3),
(204, 20, '导入菜谱', 'content:recipe:import', 3, NULL, NULL, NULL, 4),
(211, 21, '新增分类', 'content:category:create', 3, NULL, NULL, NULL, 1),
(212, 21, '编辑分类', 'content:category:edit', 3, NULL, NULL, NULL, 2),
(213, 21, '删除分类', 'content:category:delete', 3, NULL, NULL, NULL, 3),
-- Business
(30, 3, '订单管理', 'business:order', 2, '/orders', 'OrderListView', 'ShoppingCart', 1),
(31, 3, '库存管理', 'business:inventory', 2, '/inventory', 'InventoryView', 'Box', 2),
(32, 3, '周菜谱规划', 'business:mealplan', 2, '/meal-plans', 'MealPlanView', 'Calendar', 3),
(33, 3, '采购清单', 'business:shopping', 2, '/shopping', 'ShoppingView', 'ShoppingBag', 4),
(34, 3, '供应商管理', 'business:supplier', 2, '/suppliers', 'SupplierView', 'Van', 5),
(35, 3, '情侣空间', 'business:couple', 2, '/couples', 'CoupleListView', 'Connection', 6),
-- System
(40, 4, '公告管理', 'system:notice', 2, '/notices', 'NoticeView', 'Bell', 1),
(41, 4, '用户管理', 'system:user', 2, '/users', 'UserListView', 'User', 2),
(42, 4, '角色管理', 'system:role', 2, '/roles', 'RoleView', 'UserFilled', 3),
(43, 4, '权限管理', 'system:permission', 2, '/permissions', 'PermissionView', 'Lock', 4),
(44, 4, '数据统计', 'system:statistics', 2, '/statistics', 'StatisticsView', 'TrendCharts', 5),
(45, 4, '报表分析', 'system:reports', 2, '/reports', 'ReportsView', 'DataLine', 6),
(46, 4, '数据导出', 'system:export', 2, '/export', 'ExportView', 'Download', 7),
-- Logs & Monitor
(50, 5, '操作日志', 'logs:operation', 2, '/logs', 'OperationLogView', 'Document', 1),
(51, 5, '同步日志', 'logs:sync', 2, '/sync-logs', 'SyncLogView', 'Refresh', 2),
(52, 5, '系统监控', 'logs:health', 2, '/health', 'HealthView', 'Monitor', 3),
(53, 5, '系统设置', 'logs:settings', 2, '/settings', 'SettingsView', 'Setting', 4),
-- Business buttons
(301, 30, '编辑订单', 'business:order:edit', 3, NULL, NULL, NULL, 1),
(302, 30, '删除订单', 'business:order:delete', 3, NULL, NULL, NULL, 2),
(303, 30, '新增订单', 'business:order:create', 3, NULL, NULL, NULL, 3),
(304, 30, '接受订单', 'business:order:accept', 3, NULL, NULL, NULL, 4),
(305, 30, '完成订单', 'business:order:complete', 3, NULL, NULL, NULL, 5),
(306, 30, '取消订单', 'business:order:cancel', 3, NULL, NULL, NULL, 6),
-- Inventory buttons
(311, 31, '新增库存', 'business:inventory:create', 3, NULL, NULL, NULL, 1),
(312, 31, '编辑库存', 'business:inventory:edit', 3, NULL, NULL, NULL, 2),
(313, 31, '删除库存', 'business:inventory:delete', 3, NULL, NULL, NULL, 3),
(314, 31, '库存补货', 'business:inventory:restock', 3, NULL, NULL, NULL, 4),
-- MealPlan buttons
(321, 32, '新增周菜谱', 'business:mealplan:create', 3, NULL, NULL, NULL, 1),
(322, 32, '编辑周菜谱', 'business:mealplan:edit', 3, NULL, NULL, NULL, 2),
(323, 32, '删除周菜谱', 'business:mealplan:delete', 3, NULL, NULL, NULL, 3),
-- Shopping buttons
(331, 33, '新增采购项', 'business:shopping:create', 3, NULL, NULL, NULL, 1),
(332, 33, '编辑采购项', 'business:shopping:edit', 3, NULL, NULL, NULL, 2),
(333, 33, '删除采购项', 'business:shopping:delete', 3, NULL, NULL, NULL, 3),
-- Supplier buttons
(341, 34, '新增供应商', 'business:supplier:create', 3, NULL, NULL, NULL, 1),
(342, 34, '编辑供应商', 'business:supplier:edit', 3, NULL, NULL, NULL, 2),
(343, 34, '删除供应商', 'business:supplier:delete', 3, NULL, NULL, NULL, 3),
-- Couple buttons
(351, 35, '新增情侣配置', 'business:couple:create', 3, NULL, NULL, NULL, 1),
(352, 35, '编辑情侣配置', 'business:couple:edit', 3, NULL, NULL, NULL, 2),
(353, 35, '删除情侣配置', 'business:couple:delete', 3, NULL, NULL, NULL, 3),
-- Notice buttons
(405, 40, '新增公告', 'business:notice:create', 3, NULL, NULL, NULL, 1),
(406, 40, '编辑公告', 'business:notice:edit', 3, NULL, NULL, NULL, 2),
(407, 40, '删除公告', 'business:notice:delete', 3, NULL, NULL, NULL, 3),
(408, 40, '发布公告', 'business:notice:publish', 3, NULL, NULL, NULL, 4),
-- Review buttons
(241, 24, '审核评论', 'business:review:approve', 3, NULL, NULL, NULL, 1),
(242, 24, '拒绝评论', 'business:review:reject', 3, NULL, NULL, NULL, 2),
-- Gallery buttons
(231, 23, '上传相册', 'content:gallery:upload', 3, NULL, NULL, NULL, 1),
(232, 23, '删除相册', 'content:gallery:delete', 3, NULL, NULL, NULL, 2),
-- Favorite buttons
(221, 22, '收藏/取消', 'content:favorite:toggle', 3, NULL, NULL, NULL, 1),
-- Health buttons
(521, 52, '清理缓存', 'logs:health:clearcache', 3, NULL, NULL, NULL, 1),
(522, 52, '优化数据库', 'logs:health:optimize', 3, NULL, NULL, NULL, 2),
(523, 52, '创建备份', 'logs:health:backup', 3, NULL, NULL, NULL, 3),
(524, 52, '恢复备份', 'logs:health:restore', 3, NULL, NULL, NULL, 4),
-- Settings buttons
(531, 53, '修改设置', 'logs:settings:edit', 3, NULL, NULL, NULL, 1),
(532, 53, '清除数据', 'logs:settings:clear', 3, NULL, NULL, NULL, 2),
-- Export buttons
(461, 46, '导出菜谱', 'system:export:recipe', 3, NULL, NULL, NULL, 1),
(462, 46, '导出订单', 'system:export:order', 3, NULL, NULL, NULL, 2),
-- Statistics buttons
(441, 44, '查看统计', 'system:statistics:view', 3, NULL, NULL, NULL, 1),
-- Reports buttons
(451, 45, '查看报表', 'system:reports:view', 3, NULL, NULL, NULL, 1),
-- System user buttons
(402, 41, '编辑用户', 'system:user:edit', 3, NULL, NULL, NULL, 2),
(403, 41, '删除用户', 'system:user:delete', 3, NULL, NULL, NULL, 3),
(404, 41, '重置密码', 'system:user:resetpwd', 3, NULL, NULL, NULL, 4);

-- Seed data: super_admin has all permissions
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE deleted = 0;

-- Seed data: admin has most permissions (except system:role, system:permission)
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE deleted = 0 AND code NOT LIKE 'system:role%' AND code NOT LIKE 'system:permission%';

-- Seed data: user has basic view permissions
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission WHERE deleted = 0 AND type = 2 AND code LIKE '%:dashboard' OR (type = 2 AND code LIKE 'content:%' AND id <= 23) OR (type = 2 AND code LIKE 'business:order');
