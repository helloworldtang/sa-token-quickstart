# 踢人功能验证指南

## 功能概述

踢人功能允许管理员强制指定用户下线。该功能通过 `POST /auth/kickout` 接口实现，需要管理员权限。

## 为什么需要传 token 参数？

### 当前实现的问题

在当前的实现中，踢人接口需要传递**被踢用户的 Token**作为参数：

```java
@PostMapping("/kickout")
@SaCheckRole("admin")
public Result<Void> kickout(@RequestParam String token) {
    authService.kickout(token);
    return Result.success("踢人成功");
}
```

### 使用场景分析

从管理员的角度看，要获取"被踢用户的 Token"存在以下问题：

#### 问题 1：无法直接获取其他用户的 Token
- Token 是私密的，不会公开暴露给其他用户
- 管理员无法通过正常渠道获取用户的登录 Token

#### 问题 2：用户体验差
- 管理员需要先知道被踢用户的 Token 才能操作
- 无法通过用户ID或用户名进行操作
- 与常见的后台管理系统使用习惯不符

### 更好的实现方式

#### 方式1：根据用户ID踢人（推荐）✅

**接口设计**：
```java
@PostMapping("/kickout/{userId}")
@SaCheckRole("admin")
@Operation(summary = "踢人下线", description = "根据用户ID踢掉用户所有会话（需要admin角色）")
public Result<Void> kickout(@PathVariable Long userId) {
    authService.kickoutByUserId(userId);
    return Result.success("踢人成功");
}
```

**优点**：
- ✅ 通过用户ID操作，符合常规后台管理习惯
- ✅ 可以踢掉用户的所有登录会话（多设备登录时）
- ✅ 无需知道用户的 Token
- ✅ 操作日志更清晰（记录用户ID而非 Token）

**服务实现**：
```java
@Override
public void kickoutByUserId(Long userId) {
    log.info("踢人下线: userId={}", userId);
    // 根据用户ID踢掉该用户的所有会话
    StpUtil.kickout(userId);
}
```

#### 方式2：提供在线用户列表

**场景**：管理员需要先查看在线用户，然后选择要踢掉的用户

**实现方式**：
1. 创建在线用户列表接口
2. 返回在线用户的列表（不含敏感的 Token）
3. 管理员选择用户后点击踢人
4. 踢人时传递用户ID或用户名

**接口示例**：
```java
@GetMapping("/online-users")
@SaCheckRole("admin")
public Result<List<OnlineUserDTO>> getOnlineUsers() {
    return Result.success(authService.getOnlineUsers());
}
```

**OnlineUserDTO**：
```java
@Data
@Schema(description = "在线用户信息")
public class OnlineUserDTO {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色")
    private String roles;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveTime;

    @Schema(description = "设备信息")
    private String device;
}
```

## 当前实现的验证步骤

### 前提条件
- 已启动 sa-token-elegant-demo 应用
- 已配置 Redis（如果需要分布式会话管理）

### 步骤1：管理员登录

```bash
# 请求
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "admin-token-xxx...",
    "tokenName": "Authorization",
    "userId": 1,
    "username": "admin",
    "nickname": "管理员"
  }
}
```

**获取管理员 Token**：复制 `data.token` 的值

### 步骤2：普通用户登录

打开新的浏览器或使用不同的用户（模拟另一个用户）：

```bash
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "123456"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "user-token-yyy...",
    "tokenName": "Authorization",
    "userId": 2,
    "username": "user",
    "nickname": "普通用户"
  }
}
```

**注意**：需要记录普通用户的 Token 值

### 步骤3：管理员踢人

使用管理员的 Token 进行操作：

```bash
POST http://localhost:8081/auth/kickout
Authorization: Bearer admin-token-xxx...
Content-Type: application/x-www-form-urlencoded

token=user-token-yyy...
```

**说明**：
- `Authorization` header：管理员的认证 Token
- `token` 参数：被踢用户的 Token

**响应**：
```json
{
  "code": 200,
  "message": "踢人成功"
}
```

### 步骤4：验证被踢用户无法访问

使用被踢用户的 Token 访问需要登录的接口：

```bash
GET http://localhost:8081/auth/current
Authorization: Bearer user-token-yyy...
```

**期望响应**：
```json
{
  "code": 401,
  "message": "Token已失效",
  "data": null
}
```

### 步骤5：权限验证 - 普通用户无法踢人

使用普通用户的 Token 尝试踢人：

```bash
POST http://localhost:8081/auth/kickout
Authorization: Bearer user-token-yyy...
Content-Type: application/x-www-form-urlencoded

token=admin-token-xxx...
```

**期望响应**：
```json
{
  "code": 403,
  "message": "角色不足: admin",
  "data": null
}
```

## 使用 Knife4j 进行测试

### 1. 打开文档
访问：http://localhost:8081/doc.html

### 2. 找到踢人接口
在 **认证管理** 分组中找到：
- 接口名称：踢人下线
- 路径：POST /auth/kickout

### 3. 点击"调试"

### 4. 先登录管理员
在 Knife4j 右上角点击 📷 **Authorize** 按钮，输入：
```
Bearer admin-token-xxx...
```

### 5. 调用踢人接口
在 `token` 参数框中输入普通用户的 Token，点击"执行"。

## Sa-Token 踢人方法对比

| 方法 | 说明 | 使用场景 |
|------|------|---------|
| `StpUtil.kickout(userId)` | 根据用户ID踢掉所有会话 | 推荐实现，适合管理后台 |
| `StpUtil.kickoutByTokenValue(token)` | 根据Token踢掉特定会话 | 当前实现，使用不便 |
| `StpUtil.kickout(tokenId)` | 根据Token ID踢掉特定会话 | 需要先获取 Token ID |
| `StpUtil.logout(userId)` | 登出指定用户 | 同 kickout(userId) |
| `StpUtil.logout()` | 登出当前用户 | 用户主动登出 |

## 建议改进

### 1. 修改接口为根据用户ID踢人

**理由**：
- 更符合后台管理系统的使用习惯
- 可以踢掉用户的所有设备（多设备登录场景）
- 无需知道用户的 Token

### 2. 添加在线用户管理接口

**理由**：
- 管理员可以查看在线用户列表
- 选择性踢人，操作更灵活
- 提供更多会话管理功能

### 3. 添加操作日志

**理由**：
- 记录谁踢了谁
- 记录踢人时间和原因
- 便于审计和追溯

### 4. 添加踢人原因参数

**理由**：
- 在后台管理系统中，通常需要填写踢人原因
- 通知用户为什么被踢
- 提升系统透明度

## 总结

当前踢人功能**技术上可行**，但从**使用体验**角度看：

❌ **需要改进的地方**：
1. 需要知道被踢用户的 Token（获取困难）
2. 不符合常规后台管理习惯
3. 缺少在线用户列表支持
4. 无法踢掉用户的多设备登录

✅ **建议改进方案**：
1. 改为根据用户ID踢人
2. 添加在线用户列表接口
3. 添加操作日志
4. 提供踢人原因参数

如需实现改进方案，请参考上述代码示例。