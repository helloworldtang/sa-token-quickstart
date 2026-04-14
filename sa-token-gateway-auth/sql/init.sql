-- Sa-Token 全场景统一权限架构数据库脚本
-- 执行顺序：请在 MySQL 8.0+ 中执行

-- ========================================
-- 1. 用户表（适配普通登录、微服务用户）
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '加密密码（BCrypt）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常，0禁用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ========================================
-- 2. 角色表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码（如：ADMIN、USER、API_USER）',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ========================================
-- 3. 权限表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码（如：user:list、order:create）',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '权限描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ========================================
-- 4. 用户角色关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ========================================
-- 5. 角色权限关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ========================================
-- 6. API Key 表（适配大模型开放平台、第三方接口）
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_api_key` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `api_key` VARCHAR(100) NOT NULL COMMENT 'API Key（唯一）',
  `secret_key` VARCHAR(100) NOT NULL COMMENT '秘钥（用于签名验签）',
  `role_id` BIGINT NOT NULL COMMENT '关联角色（控制权限）',
  `limit_count` INT NOT NULL DEFAULT 1000 COMMENT '日限流次数',
  `app_name` VARCHAR(100) DEFAULT NULL COMMENT '应用名称',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常，0禁用）',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_key` (`api_key`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API Key 管理表';

-- ========================================
-- 初始化数据
-- ========================================

-- 初始化角色
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('超级管理员', 'ADMIN', '拥有所有权限'),
('普通用户', 'USER', '普通用户角色'),
('API用户', 'API_USER', 'API Key调用角色');

-- 初始化权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `description`) VALUES
('user:list', '查看用户列表', '查看用户列表权限'),
('user:add', '添加用户', '添加用户权限'),
('user:delete', '删除用户', '删除用户权限'),
('order:list', '查看订单列表', '查看订单列表权限'),
('order:create', '创建订单', '创建订单权限'),
('order:delete', '删除订单', '删除订单权限'),
('ai:chat', 'AI对话', '大模型对话权限'),
('system:config', '系统配置', '系统配置权限');

-- 为ADMIN角色分配所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 为USER角色分配部分权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 1),  -- user:list
(2, 4);  -- order:list

-- 为API_USER角色分配AI权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3, 7);  -- ai:chat

-- 初始化用户（密码：123456，使用BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1);

-- 为用户分配角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> ADMIN
(2, 2);  -- user -> USER

-- 初始化 API Key（用于测试）
INSERT INTO `sys_api_key` (`api_key`, `secret_key`, `role_id`, `limit_count`, `app_name`, `contact_email`) VALUES
('test_api_key_123', 'test_secret_key_456', 3, 1000, '测试应用', 'test@example.com');

-- ========================================
-- 查询验证
-- ========================================

-- 查询用户及其角色
SELECT
  u.id,
  u.username,
  r.role_code,
  r.role_name
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id;

-- 查询用户权限
SELECT
  u.username,
  p.permission_code,
  p.permission_name
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role_permission rp ON ur.role_id = rp.role_id
LEFT JOIN sys_permission p ON rp.permission_id = p.id
WHERE u.username = 'admin';

-- 查询 API Key 及其权限
SELECT
  ak.api_key,
  ak.app_name,
  r.role_name,
  p.permission_code
FROM sys_api_key ak
LEFT JOIN sys_role r ON ak.role_id = r.id
LEFT JOIN sys_role_permission rp ON r.id = rp.role_id
LEFT JOIN sys_permission p ON rp.permission_id = p.id;
