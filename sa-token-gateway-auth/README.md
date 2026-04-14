# Sa-Token 微服务网关鉴权示例

> 演示如何使用 Sa-Token 在 Spring Cloud Gateway 中实现统一鉴权，解决权限认证碎片化问题

## 🌟 项目亮点

- ✅ **统一网关鉴权** - 消除各服务重复的鉴权代码，实现一处配置处处生效
- ✅ **分布式权限验证** - 基于 Redis 的 Session 共享，支持跨服务权限验证
- ✅ **多种认证方式** - 支持用户名密码、API Key、签名验证等多种认证方式
- ✅ **完整权限控制** - 支持登录验证、角色校验、权限校验等多层控制
- ✅ **生产级配置** - 包含异常处理、日志配置、测试覆盖等生产环境必需组件

## 📋 项目简介

### 这是什么？
这是一个完整的微服务网关鉴权示例项目，展示了如何使用 Sa-Token 在 Spring Cloud Gateway 中实现统一鉴权。通过网关层的统一验证，消除了传统方式中各服务重复鉴权代码的痛点。

### 适用场景
- **微服务架构** - 需要统一鉴权入口的微服务系统
- **SaaS 平台** - 多租户、多权限级别的应用系统
- **API 网关** - 需要 API Key 认证和签名验证的开放平台
- **权限复杂系统** - 需要细粒度权限控制的企业应用

## 🛠️ 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.0 | 基础框架 |
| Spring Cloud Gateway | 2023.0.3 | 网关服务 |
| Sa-Token | 1.45.0 | 权限认证框架 |
| MyBatis Plus | 3.5.5 | 数据库访问 |
| Redis | - | Session 存储 |
| Maven | 3.6+ | 项目构建 |
| JDK | 17+ | 运行环境 |

## 📖 功能说明

### 功能 1：统一网关鉴权

**说明**：通过 Sa-Token Reactor 过滤器在网关层统一进行 Token 验证，避免各服务重复实现鉴权逻辑。

**使用方法**：
```bash
# 网关自动拦截所有请求，验证 Token
# 无需在每个服务中手动实现鉴权代码
```

**预期效果**：未登录请求统一返回 401，已登录请求自动转发到下游服务。

### 功能 2：分布式权限验证

**说明**：基于 Redis 存储的 Session，实现跨服务的权限和角色信息共享。

**使用方法**：
```java
// 在任何服务中都可以获取当前登录用户信息
Object userId = StpUtil.getLoginId();
List<String> roles = StpUtil.getRoleList();
List<String> permissions = StpUtil.getPermissionList();
```

**预期效果**：在一个服务中登录，在所有服务中都能获取用户信息。

### 功能 3：多种认证方式

**说明**：支持传统用户名密码登录、API Key 认证、签名验证等多种方式。

**使用方法**：
```bash
# 用户名密码登录
curl -X POST "http://localhost:8080/auth/login?username=admin&password=123456"

# API Key 认证
curl -X POST "http://localhost:8080/auth/apiKeyAuth?apiKey=xxx&sign=xxx&timestamp=xxx"
```

**预期效果**：不同的认证方式都可以获取有效的 Token，并享受统一的权限控制。

### 功能 4：细粒度权限控制

**说明**：支持基于注解的权限控制，包括登录验证、角色校验、权限校验。

**使用方法**：
```java
// 需要登录
@SaCheckLogin
@GetMapping("/profile")
public SaResult profile() { }

// 需要指定角色
@SaCheckRole("admin")
@GetMapping("/admin/data")
public SaResult adminData() { }

// 需要指定权限
@SaCheckPermission("user:add")
@PostMapping("/add")
public SaResult add() { }
```

**预期效果**：权限不足的请求自动返回 403，权限正常则正常访问。

## 💡 实战案例

### 完整的认证流程演示

让我们完成一个完整的用户认证和权限验证流程：

**步骤 1：启动 Redis**
```bash
docker run -d -p 6379:6379 redis:latest
```

**步骤 2：启动所有服务**
```bash
# 在 IDEA 中分别启动或使用 Maven
mvn clean package -DskipTests
java -jar gateway-service/target/gateway-service-1.0.0.jar
java -jar user-service/target/user-service-1.0.0.jar
java -jar order-service/target/order-service-1.0.0.jar
```

**步骤 3：用户登录获取 Token**
```bash
curl -X POST "http://localhost:8080/auth/login?username=admin&password=123456"
```

**预期结果**：
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "userId": 10001,
    "username": "admin",
    "token": "xxxx-xxxx-xxxx-xxxx"
  }
}
```

**步骤 4：使用 Token 访问受保护接口**
```bash
TOKEN="你的token"
curl http://localhost:8080/user/list -H "satoken: $TOKEN"
```

**预期结果**：
```json
{
  "code": 200,
  "msg": "ok",
  "data": [...]
}
```

**步骤 5：跨服务权限验证**
```bash
# 在 user-service 中验证权限
curl http://localhost:8080/user/profile -H "satoken: $TOKEN"

# 在 order-service 中验证权限
curl http://localhost:8080/order/my -H "satoken: $TOKEN"
```

**预期结果**：两个服务都能正确获取到用户信息，证明分布式 Session 验证成功！

## 🔬 核心概念/原理

### 解决的痛点

**❌ 传统方式的痛点**：
1. **代码碎片化** - 每个服务都要实现鉴权逻辑，代码重复严重
2. **配置分散** - 各服务独立配置，维护成本高
3. **Session 不共享** - 用户在不同服务间无法保持登录状态
4. **权限管理困难** - 统一权限控制复杂，容易出现权限漏洞

**✅ Sa-Token 解决方案**：
1. **统一网关鉴权** - 一处配置，处处生效
2. **分布式 Session** - 基于 Redis，自动共享
3. **统一权限接口** - 实现一次，全局生效
4. **注解式权限** - 声明式权限控制，代码简洁

### 架构优势

通过网关层统一鉴权，实现了：
- **性能优化** - 无效请求在网关层直接拦截，减少后端压力
- **代码复用** - 鉴权逻辑只在网关层实现一次
- **维护简化** - 权限规则修改只需改一处
- **安全性增强** - 统一的安全策略，减少漏洞风险

## 架构说明

```
┌─────────────────────────────────────────────────────────────────┐
│                          客户端                                  │
│                     Header: satoken=xxx                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Cloud Gateway (8080)                   │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              Sa-Token Reactor 过滤器                        │  │
│  │                                                            │  │
│  │   1. 从 Header 读取 Token                                   │  │
│  │   2. 查询 Redis 验证 Token                                  │  │
│  │   3. Token 无效 → 返回 401                                 │  │
│  │   4. Token 有效 → 放行，携带用户信息到下游                   │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              │ 验证通过                          │
└──────────────────────────────┼──────────────────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
          ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   User Service   │  │  Order Service  │  │  Auth Service   │
│      (8081)       │  │      (8082)       │  │      (8083)     │
│                   │  │                   │  │                 │
│  ┌─────────────┐ │  │  ┌─────────────┐ │  │  ┌───────────┐ │
│  │ 登录接口      │ │  │  │ 业务接口     │ │  │  │ 业务接口   │ │
│  │ StpUtil.    │ │  │  │ @SaCheck    │ │  │  │ @SaCheck  │ │
│  │  login()    │ │  │  │  Permission │ │  │  │  Permission│ │
│  └─────────────┘ │  │  └─────────────┘ │  │  └───────────┘ │
└────────┬──────────┘  └────────┬────────┘  └────────┬────────┘
         │                       │                    │
         └───────────────────────┴────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                           Redis                                 │
│                                                                 │
│   Token:xxxx-xxx  →  {userId: 10001, role: admin}             │
│   satoken:login:session:10001  →  Session 数据                 │
└─────────────────────────────────────────────────────────────────┘
```

## 模块说明

### 1. gateway-service (8080)
网关服务，负责：
- 路由转发
- 统一鉴权
- 异常处理

**关键代码**：`SaTokenConfig.java`

### 2. user-service (8081)
用户服务，负责：
- 用户登录
- 用户管理
- 权限校验

**关键代码**：`AuthController.java`, `UserController.java`

### 3. order-service (8082)
订单服务，负责：
- 订单查询
- 订单创建
- 权限校验

**关键代码**：`OrderController.java`

## 快速开始

### 前置条件

1. **JDK 17+**
2. **Maven 3.6+**
3. **Redis**（本地或远程均可）

### 启动步骤

#### 1. 启动 Redis

```bash
# macOS (使用 Homebrew)
brew install redis
brew services start redis

# Linux
sudo systemctl start redis

# Docker
docker run -d -p 6379:6379 redis:latest
```

#### 2. 启动服务

**方式一：在 IDEA 中分别启动**

1. 启动 `UserApplication` (端口 8081)
2. 启动 `OrderApplication` (端口 8082)
3. 启动 `GatewayApplication` (端口 8080)

**方式二：使用 Maven 打包后启动**

```bash
# 打包所有服务
cd sa-token-gateway-auth
mvn clean package -DskipTests

# 启动服务
java -jar gateway-service/target/gateway-service-1.0.0.jar
java -jar user-service/target/user-service-1.0.0.jar
java -jar order-service/target/order-service-1.0.0.jar
```

## 测试验证

### 测试用例 1：未登录访问（预期：返回 401）

```bash
curl http://localhost:8080/user/list
```

**预期结果**：
```json
{
  "code": 401,
  "msg": "请先登录",
  "data": null
}
```

### 测试用例 2：登录获取 Token（预期：返回 200）

```bash
curl -X POST "http://localhost:8080/auth/login?username=admin&password=123456"
```

**预期结果**：
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "userId": 10001,
    "username": "admin",
    "token": "xxx-xxx-xxx"
  }
}
```

**保存 Token**：
```bash
export TOKEN="你的token"
```

### 测试用例 3：登录后访问（预期：返回 200）

```bash
curl http://localhost:8080/user/list -H "satoken: $TOKEN"
```

**预期结果**：
```json
{
  "code": 200,
  "msg": "ok",
  "data": [...]
}
```

### 测试用例 4：跨服务访问（分布式 Session 验证）

```bash
curl http://localhost:8080/user/profile -H "satoken: $TOKEN"
curl http://localhost:8080/order/my -H "satoken: $TOKEN"
```

**预期结果**：
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "userId": 10001,
    "role": "admin",
    "message": "分布式 Session 验证成功！"
  }
}
```

### 测试用例 5：权限不足（预期：返回 403）

使用普通用户登录：
```bash
USER_TOKEN=$(curl -s -X POST "http://localhost:8080/auth/login?username=user&password=123456" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

# 尝试访问需要 admin 权限的接口
curl http://localhost:8080/user/admin/data -H "satoken: $USER_TOKEN"
```

**预期结果**：
```json
{
  "code": 403,
  "msg": "无权限",
  "data": null
}
```

## 核心代码解析

### 网关层鉴权配置

```java
@Configuration
public class SaTokenConfig {

    @Bean
    public SaReactorFilter saTokenFilter() {
        return new SaReactorFilter()
                .setInclude("/**")
                .addExcludePathPatterns("/auth/login")
                .setAuth(obj -> {
                    StpUtil.checkLogin();
                })
                .setError(e -> {
                    if (e instanceof NotLoginException) {
                        return "{\"code\":401,\"msg\":\"请先登录\",\"data\":null}";
                    }
                    return "{\"code\":403,\"msg\":\"无权限\",\"data\":null}";
                });
    }
}
```

**关键点**：
- `setInclude("/**")`：拦截所有请求
- `addExcludePathPatterns("/auth/login")`：放行登录接口
- `setAuth()`：认证逻辑
- `setError()`：异常处理

### 权限校验注解

```java
// 需要登录
@SaCheckLogin
@GetMapping("/profile")
public SaResult profile() {
    return SaResult.ok().setData(StpUtil.getLoginId());
}

// 需要指定角色
@SaCheckRole("admin")
@GetMapping("/admin/data")
public SaResult adminData() {
    return SaResult.ok("管理员专属数据");
}

// 需要指定权限
@SaCheckPermission("user:add")
@PostMapping("/add")
public SaResult add(@RequestParam String username) {
    return SaResult.ok("添加成功");
}
```

### 权限接口实现

```java
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 从数据库查询用户权限
        Long userId = Long.parseLong(loginId.toString());
        return permissionMapper.selectByUserId(userId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 从数据库查询用户角色
        Long userId = Long.parseLong(loginId.toString());
        return roleMapper.selectByUserId(userId);
    }
}
```

## 常见问题

### Q1: 启动时报错 "Connection refused"

**原因**：Redis 未启动或端口配置错误

**解决**：检查 Redis 是否启动，端口是否为 6379

### Q2: 访问接口时返回 401

**原因**：Token 未传递或 Token 已过期

**解决**：
1. 先登录获取 Token
2. 在 Header 中传递 Token：`satoken: xxx-xxx-xxx`

### Q3: 权限校验不生效

**原因**：未实现 `StpInterface` 接口

**解决**：实现 `StpInterface` 接口，返回用户的权限列表

### Q4: 分布式 Session 不共享

**原因**：未配置 Sa-Token Redis 集成

**解决**：添加 `sa-token-redis-jackson` 依赖，配置 Redis 连接

## 扩展阅读

- [Sa-Token 官方文档](https://sa-token.cc)
- [Spring Cloud Gateway 文档](https://spring.io/projects/spring-cloud-gateway)
- [RBAC 权限模型详解](https://en.wikipedia.org/wiki/Role-based_access_control)

## 许可证

MIT License
