# Sa-Token 多模块示例项目

## 项目结构

本项目展示了 Sa-Token 框架的基础使用和优雅实践，采用多模块结构设计：

```
sa-token-quickstart/
├── sa-token-basic/                     # 基础示例模块
├── sa-token-elegant-demo/              # 优雅实践模块
├── sa-token-apikey/                    # API Key 管理模块
├── sa-token-apisign/                   # API 接口参数签名模块
├── sa-token-websocket-authentication/  # WebSocket 鉴权模块
└── pom.xml                             # 父POM
```

## 模块说明

### sa-token-basic
- **定位**: 基础功能演示
- **端口**: 8080
- **特点**: 简单直接，快速上手
- **文档**: http://localhost:8080/doc.html

### sa-token-elegant-demo
- **定位**: 优雅实践示例
- **端口**: 8081
- **特点**: 展示框架设计与工程实践
- **文档**: http://localhost:8081/doc.html
- **包含**:
  - 声明式权限控制（@SaCheckLogin, @SaCheckRole, @SaCheckPermission）
  - 统一异常处理
  - 统一响应封装
  - 完整的单元测试
  - Redis集成示例
  - 优雅的代码组织
  - **在线会话管理**（查看和踢掉在线用户会话）
  - **改进的踢人功能**（支持按用户ID和会话ID踢人）

### sa-token-apikey
- **定位**: API Key 管理系统
- **端口**: 8082
- **特点**: 基于 Sa-Token 官方 API Key 插件的完整实现
- **管理后台**: http://localhost:8082/
- **默认账号**: admin / admin123
- **包含**:
  - API Key 创建、查询、删除
  - API Key 认证（Bearer sk-xxx 格式）
  - **双重限流机制**（IP 限流 + API Key 限流）
  - API Key 使用统计
  - Web 管理后台
  - Redis 持久化
- **详细文档**: [sa-token-apikey/README.md](sa-token-apikey/README.md)

### sa-token-apisign
- **定位**: API 接口参数签名模块
- **端口**: 8083
- **特点**: 基于 Sa-Token Sign 插件，防止参数篡改和重放攻击
- **签名帮助**: http://localhost:8083/sign/help
- **API 文档**: http://localhost:8083/doc.html
- **包含**:
  - API 接口参数签名校验（一行代码完成）
  - 签名生成帮助接口（方便理解和使用）
  - 模拟转账业务接口
  - 完整的签名生成工具类
  - 单元测试示例
- **详细文档**: [sa-token-apisign/README.md](sa-token-apisign/README.md)

### sa-token-websocket-authentication
- **定位**: WebSocket 鉴权模块
- **端口**: 8084
- **特点**: 基于 Sa-Token + Spring WebSocket 的完整鉴权方案
- **API 文档**: http://localhost:8084/doc.html
- **包含**:
  - WebSocket 握手时 Token 验证（HandshakeInterceptor）
  - 支持 3 种 Token 传递方式（URL 参数、Header、Authorization）
  - 在线用户管理
  - 消息广播和私聊功能
  - 完整的集成测试（13 个用例）
  - 登出后 Token 失效验证
- **详细文档**: [sa-token-websocket-authentication/README.md](sa-token-websocket-authentication/README.md)

## 技术栈

- Spring Boot 3.3.0
- Sa-Token 1.45.0
- Java 17
- Lombok
- Knife4j (Swagger)
- Testcontainers
- Redis（sa-token-apikey 模块必需）

## 快速开始

### 前置要求
- JDK 17+
- Maven 3.6+
- Redis 6.0+（运行 sa-token-apikey 模块需要）

### 构建项目
```bash
# 构建所有模块
mvn clean install

# 跳过测试构建
mvn clean install -DskipTests
```

### 运行测试
```bash
# 运行所有模块的测试
mvn test

# 运行特定模块的测试
cd sa-token-elegant-demo
mvn test
```

### 启动应用

#### 启动基础示例
```bash
cd sa-token-basic
mvn spring-boot:run
```
访问: http://localhost:8080/doc.html

#### 启动优雅实践示例

**重要提示**：推荐使用 IntelliJ IDEA 启动，Maven 命令行编译时 Java 21 与 Lombok 存在兼容性问题。

在 IDEA 中：
1. 打开项目
2. 找到 `SaTokenElegantApplication.java`
3. 右键 → Run

访问: http://localhost:8081/doc.html

详细启动指南请参考 [STARTUP_GUIDE.md](STARTUP_GUIDE.md)

#### 启动 API Key 管理系统

**前置要求**：
- Redis 服务必须运行

```bash
# 启动 Redis
redis-server

# 启动应用
cd sa-token-apikey
mvn spring-boot:run
```

访问: http://localhost:8082/

默认账号：
- 用户名：admin
- 密码：admin123

详细使用说明请参考 [sa-token-apikey/README.md](sa-token-apikey/README.md)

#### 启动 API 接口参数签名模块

```bash
cd sa-token-apisign
mvn spring-boot:run
```

访问: http://localhost:8083/sign/help

详细使用说明请参考 [sa-token-apisign/README.md](sa-token-apisign/README.md)

## 学习路径

### 1. 从基础开始
1. 启动 sa-token-basic 模块
2. 阅读代码，了解 Sa-Token 的基本用法
3. 使用 Knife4j 测试各个接口

### 2. 进阶学习
1. 启动 sa-token-elegant-demo 模块
2. 阅读代码，学习优雅实践
3. 理解声明式权限控制
4. 学习统一异常处理
5. 查看单元测试，了解测试方法

### 3. API Key 管理
1. 启动 sa-token-apikey 模块（需先启动 Redis）
2. 登录管理后台创建 API Key
3. 了解 API Key 认证机制
4. 学习双重限流实现
5. 查看使用统计功能

### 4. API 接口参数签名
1. 启动 sa-token-apisign 模块
2. 访问签名帮助接口了解签名生成过程
3. 学习签名校验的使用方法
4. 理解如何防止参数篡改和重放攻击
5. 查看单元测试，了解测试方法

## 优雅实践亮点

### 1. 框架设计的优雅
- API设计简洁（一行代码完成登录）
- 配置灵活（支持多种配置选项）
- 开箱即用（无需复杂配置）

### 2. 工程实践的优雅
- 声明式权限控制（注解驱动）
- 统一异常处理（集中管理）
- 统一响应封装（前端友好）
- 完整的单元测试（保证质量）
- 清晰的代码组织（易于维护）

## 相关资源

- [Sa-Token 官网](https://sa-token.cc/)
- [Sa-Token GitHub](https://github.com/dromara/sa-token)
- [Sa-Token 文档](https://sa-token.cc/doc.html)

## License

MIT License
