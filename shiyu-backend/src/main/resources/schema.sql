CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50),
    `avatar` VARCHAR(500),
    `role` VARCHAR(20) NOT NULL DEFAULT 'diner',
    `couple_id` BIGINT,
    `status` TINYINT NOT NULL DEFAULT 1,
    `last_login_time` DATETIME,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    INDEX `idx_couple_id` (`couple_id`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `couple_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `space_name` VARCHAR(100) NOT NULL,
    `chef_name` VARCHAR(50) NOT NULL,
    `diner_name` VARCHAR(50) NOT NULL,
    `space_avatar` VARCHAR(500),
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `recipe_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `icon` VARCHAR(100),
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `recipe` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `couple_id` BIGINT,
    `name` VARCHAR(100) NOT NULL,
    `category_id` BIGINT,
    `type` INT NOT NULL DEFAULT 1,
    `description` TEXT,
    `cover_image` VARCHAR(500),
    `cooking_time` INT,
    `difficulty` TINYINT DEFAULT 1,
    `is_favorite` TINYINT DEFAULT 0,
    `order_count` INT DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `sync_id` VARCHAR(50),
    `sync_time` DATETIME,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    INDEX `idx_couple_id` (`couple_id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_sync_id` (`sync_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `recipe_material` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `recipe_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `amount` VARCHAR(50),
    `unit` VARCHAR(20),
    `sort_order` INT DEFAULT 0,
    `sync_id` VARCHAR(50),
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    INDEX `idx_recipe_id` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `recipe_step` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `recipe_id` BIGINT NOT NULL,
    `step_number` INT NOT NULL,
    `description` TEXT NOT NULL,
    `image_url` VARCHAR(500),
    `sync_id` VARCHAR(50),
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    INDEX `idx_recipe_id` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `couple_id` BIGINT,
    `recipe_id` BIGINT NOT NULL,
    `recipe_name` VARCHAR(100) NOT NULL,
    `recipe_image` VARCHAR(500),
    `status` TINYINT NOT NULL DEFAULT 0,
    `remark` TEXT,
    `reject_reason` VARCHAR(500),
    `order_time` DATETIME NOT NULL,
    `accept_time` DATETIME,
    `complete_time` DATETIME,
    `sync_id` VARCHAR(50),
    `sync_time` DATETIME,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    INDEX `idx_couple_id` (`couple_id`),
    INDEX `idx_recipe_id` (`recipe_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_sync_id` (`sync_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `gallery_image` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `couple_id` BIGINT,
    `image_url` VARCHAR(500) NOT NULL,
    `thumbnail_url` VARCHAR(500),
    `description` VARCHAR(500),
    `order_id` BIGINT,
    `sync_id` VARCHAR(50),
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    INDEX `idx_couple_id` (`couple_id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_sync_id` (`sync_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sync_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `device_id` VARCHAR(100) NOT NULL,
    `sync_type` VARCHAR(20) NOT NULL,
    `sync_direction` VARCHAR(20) NOT NULL,
    `table_name` VARCHAR(50),
    `record_count` INT DEFAULT 0,
    `status` VARCHAR(20) NOT NULL,
    `error_message` TEXT,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL UNIQUE,
    `config_value` TEXT,
    `config_type` VARCHAR(20) DEFAULT 'string',
    `description` VARCHAR(200),
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `recipe_category` (`name`, `icon`, `sort_order`) VALUES
('Meat', 'meat', 1), ('Vegetable', 'vegetable', 2), ('Soup', 'soup', 3),
('Dessert', 'dessert', 4), ('Steamed', 'steamed', 5), ('Stewed', 'stewed', 6),
('Cold', 'cold', 7), ('StirFry', 'stir-fry', 8), ('Braised', 'braised', 9),
('Other', 'other', 10);

INSERT IGNORE INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('app_name', 'Shiyu', 'string', 'AppName'),
('max_upload_size', '10485760', 'number', 'MaxUploadSize'),
('allowed_image_types', 'jpg,jpeg,png,gif,webp', 'string', 'AllowedImageTypes'),
('sync_interval', '300', 'number', 'SyncInterval');

INSERT IGNORE INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Admin', 'admin', 1);
