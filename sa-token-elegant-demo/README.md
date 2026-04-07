# Sa-Token 优雅实践示例

## 项目说明

本模块展示了 Sa-Token 框架的优雅设计与工程实践，旨在通过简洁的代码展示如何优雅地使用 Sa-Token 实现权限认证。

## 优雅实践维度

### 1. 框架的优雅设计

#### 1.1 API设计简洁
```java
// 登录 - 一行代码完成
StpUtil.login(userId);

// 登出 - 一行代码完成
StpUtil.logout();

// 获取当前登录ID
Long userId = StpUtil.getLoginIdAsLong();

// 踢人下线
StpUtil.kickout(tokenValue);
```

#### 1.2 配置灵活
```yaml
sa-token:
  token-name: Authorization        # 自定义Token名称
  timeout: 2592000                # 自定义有效期
  token-style: uuid               # 多种Token风格
  is-concurrent: true             # 灵活控制并发登录
```

#### 1.3 开箱即用
- 实现StpInterface接口即可完成权限认证
- 无需手动管理Token生命周期
- 自动处理登录状态、Session管理

### 2. 工程实践中的优雅

#### 2.1 声明式权限控制
```java
@SaCheckLogin                           // 要求登录
@SaCheckRole("admin")                   // 要求角色
@SaCheckPermission("user:add")         // 要求权限
@GetMapping("/user/{id}")
public Result<User> getById(@PathVariable Long id) {
    // 业务逻辑
}
```

#### 2.2 统一异常处理
```java
@ExceptionHandler(NotLoginException.class)
public Result<?> handleNotLoginException(NotLoginException e) {
    // 返回401
}

@ExceptionHandler(NotPermissionException.class)
public Result<?> handleNotPermissionException(NotPermissionException e) {
    // 返回403
}
```

#### 2.3 统一响应封装
```java
@Data
@Builder
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
}
```

#### 2.4 优雅的代码组织
```
├── common/          # 公共类
│   └── Result.java
├── config/          # 配置类
│   └── StpInterfaceImpl.java
├── controller/      # 控制器
│   ├── AuthController.java
│   └── UserController.java
├── exception/       # 异常处理
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── model/           # 模型
│   ├── User.java
│   └── dto/
└── service/         # 服务层
    ├── AuthService.java
    └── UserService.java
```

## 核心特性展示

### 登录认证
```java
@PostMapping("/login")
public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    User user = userService.validateUser(request.getUsername(), request.getPassword());
    StpUtil.login(user.getId());
    StpUtil.getSession().set("user", user);
    return Result.success(buildResponse(user));
}
```

### 权限认证
```java
@GetMapping("/user/{id}")
@SaCheckPermission("user:view")
public Result<User> getById(@PathVariable Long id) {
    return Result.success(userService.getById(id));
}
```

### 角色认证
```java
@PostMapping("/kickout")
@SaCheckRole("admin")
public Result<Void> kickout(@RequestParam String token) {
    authService.kickout(token);
    return Result.success("踢人成功");
}
```

### 踢人下线
```java
@Override
public void kickout(String token) {
    StpUtil.kickoutByTokenValue(token);
}
```

### Redis集成
```xml
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-redis-jackson</artifactId>
</dependency>
```

## 测试

### 运行所有测试
```bash
cd sa-token-elegant-demo
mvn test
```

### 测试覆盖率
项目包含完整的单元测试和集成测试：
- UserServiceTest: 用户服务测试
- AuthServiceTest: 认证服务测试
- AuthControllerTest: 认证控制器集成测试
- UserControllerTest: 用户控制器集成测试

## API文档

项目集成了 Knife4j（Swagger），启动后访问：
```
http://localhost:8081/doc.html
```

## 快速开始

### 1. 启动Redis（可选）
如需使用Redis分布式存储，请先启动Redis：
```bash
docker run -d -p 6379:6379 redis
```

### 2. 启动应用
```bash
mvn spring-boot:run
```

### 3. 测试登录
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

## 测试账号

| 用户名 | 密码   | 角色   | 权限                                    |
|--------|--------|--------|-----------------------------------------|
| admin  | 123456 | admin  | user:add,user:edit,user:delete,user:view |
| user   | 123456 | user   | user:view                               |

## 最佳实践总结

1. **使用注解进行权限控制**：代码更简洁，可读性更强
2. **统一异常处理**：避免重复的try-catch，前端处理更优雅
3. **统一响应格式**：前端处理更方便，维护更容易
4. **Service层分离认证逻辑**：Controller保持简洁，职责单一
5. **使用DTO进行参数校验**：JSR-303注解自动校验，减少手动判断
6. **完整的单元测试**：保证代码质量，重构更放心
7. **配置灵活**：根据不同环境调整配置

## 技术栈

- Spring Boot 3.2.0
- Sa-Token 1.37.0
- Java 17
- Lombok
- Knife4j (Swagger)
- Testcontainers

## 学习资源

- [Sa-Token 官网](https://sa-token.cc/)
- [Sa-Token GitHub](https://github.com/dromara/sa-token)
- [Sa-Token 文档](https://sa-token.cc/doc.html)
