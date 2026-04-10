# Sa-Token 搞定 WebSocket 鉴权

> 用最少的代码，给 WebSocket 加上登录认证

---

## 背景

WebSocket 长连接的鉴权是个老大难问题。

传统 HTTP 请求有 Filter、Interceptor，每次请求带上 Token 就行。但 WebSocket 是持久连接，握手只有一次。你需要在「那一次握手」里搞定身份验证，后续消息才能放心处理。

本文基于 **Sa-Token + Spring WebSocket**，从零实现一个完整的 WebSocket 鉴权方案。

---

## 核心思路

WebSocket 鉴权分两个阶段：

```
第一阶段：HTTP 握手
  客户端 ──[带 Token 的 WebSocket 握手请求]──▶ 服务端
  服务端  ──[验证 Token，通过则升级为 WS 连接]──▶ 客户端

第二阶段：WebSocket 通信
  后续消息无需再验证 Token（握手时已存入 session）
```

关键在于：**握手拦截器（HandshakeInterceptor）**。

---

## 快速上手

### 1. 依赖

```xml
<!-- Sa-Token Spring Boot 3 整合 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot3-starter</artifactId>
    <version>1.45.0</version>
</dependency>

<!-- Spring WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. 握手拦截器（核心）

这是整个方案的关键——在 WebSocket 握手时验证 Token：

```java
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Value("${sa-token.token-name:satoken}")
    private String tokenName;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 1. 从请求中获取 Token
        String token = getToken(request);
        if (token == null || token.isEmpty()) {
            response.getBody().write("{\"code\":401,\"msg\":\"请先登录获取 Token\"}".getBytes());
            return false;  // 拒绝握手
        }

        try {
            // 2. 用 Sa-Token 验证 Token，无效会抛出异常
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) throw new NotLoginException("Token 不存在或已过期", token, null);

            // 3. 把用户 ID 存入 attributes，后续 Handler 里直接用
            attributes.put("loginId", loginId);
            attributes.put("token", token);

            return true;  // 允许握手
        } catch (NotLoginException e) {
            response.getBody().write("{\"code\":401,\"msg\":\"Token 无效或已过期\"}".getBytes());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}

    /**
     * 支持 3 种 Token 传递方式
     */
    private String getToken(ServerHttpRequest request) {
        // 方式一：URL 参数 ws://host/ws?satoken=xxx
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith(tokenName + "=")) return param.substring(tokenName.length() + 1);
                if (param.startsWith("token=")) return param.substring(6);
            }
        }
        // 方式二：Header satoken: xxx
        String satoken = request.getHeaders().getFirst(tokenName);
        if (satoken != null && !satoken.isEmpty()) return satoken;

        // 方式三：Header Authorization: Bearer xxx
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);

        return null;
    }
}
```

> **为什么用 `StpUtil.getLoginIdByToken(token)` 而不是 `StpUtil.checkLogin()`？**
>
> 因为 `checkLogin()` 依赖 ThreadLocal 上下文（从当前 HTTP 请求里自动取 Token），而 WebSocket 握手时 Sa-Token 的上下文还没建立。必须手动传入 token 去验证。

### 3. 消息处理器

握手通过后，用户 ID 已经存在 session attributes 里，Handler 里直接取：

```java
@Component
public class AuthWebSocketHandler extends TextWebSocketHandler {

    // 在线会话表：userId -> session
    private final Map<Object, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Object loginId = session.getAttributes().get("loginId");  // 从 attributes 取用户 ID
        onlineSessions.put(loginId, session);
        // 发送欢迎消息
        session.sendMessage(new TextMessage("{\"type\":\"system\",\"content\":\"连接成功\"}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Object loginId = session.getAttributes().get("loginId");
        // ... 正常处理消息，loginId 随时可取
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object loginId = session.getAttributes().get("loginId");
        onlineSessions.remove(loginId);
    }
}
```

### 4. 注册拦截器

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketAuthInterceptor authInterceptor;

    @Autowired
    private AuthWebSocketHandler wsHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsHandler, "/ws")
                .addInterceptors(authInterceptor)    // 注册鉴权拦截器
                .setAllowedOrigins("*");             // 生产环境改为具体域名
    }
}
```

---

## 两种 Token 传递方式对比

WebSocket 协议的 JavaScript API 不支持自定义 Header（浏览器限制），所以最常用的是 URL 参数方式：

| 方式 | 示例 | 适用场景 |
|------|------|---------|
| **URL 参数**（推荐） | `ws://host/ws?satoken=xxx` | 浏览器原生 WebSocket |
| **自定义 Header** | `Header: satoken: xxx` | 非浏览器客户端（如 App、小程序、测试工具） |
| **Authorization** | `Authorization: Bearer xxx` | 标准 OAuth2 风格 |

URL 参数方式连接代码：
```javascript
// 先登录拿到 token
const { data } = await fetch('/auth/login', { method: 'POST', ... });
const token = data.token;

// 带 token 建立 WebSocket 连接
const ws = new WebSocket(`ws://localhost:8084/ws?satoken=${token}`);
ws.onopen = () => console.log('连接成功');
ws.onmessage = (e) => console.log('收到消息:', JSON.parse(e.data));
```

---

## 完整流程图

```
1. 用户 HTTP 登录
   POST /auth/login → StpUtil.login(userId) → 返回 Token

2. 客户端建立 WebSocket 连接
   ws://host/ws?satoken={token}

3. 服务端握手校验（WebSocketAuthInterceptor）
   - 提取 Token
   - StpUtil.getLoginIdByToken(token) 验证
   - 通过：loginId 存入 session attributes，允许连接
   - 失败：返回 401，拒绝连接

4. WebSocket 消息通信（AuthWebSocketHandler）
   - 每条消息处理时从 session.getAttributes() 取 loginId
   - 无需再验证 Token
```

---

## 运行测试

```bash
# 启动服务（需要本地 Redis）
mvn spring-boot:run

# 运行全部测试（无需 Redis，使用内存模式）
mvn test
```

测试结果：13 个用例全部通过 ✅

```
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
```

### 测试覆盖范围

- **登录认证**：正确/错误密码测试
- **状态检查**：登录状态、用户信息获取
- **Token 管理**：登出后 Token 失效验证、Token 无效处理
- **多用户隔离**：不同用户 Token 互不干扰
- **权限控制**：未登录访问受保护接口返回 401
- **WebSocket 功能**：在线用户查询、消息广播、服务状态

---

## 项目结构

```
sa-token-websocket-authentication/
├── src/main/java/com/tangtang/satoken/websocket/
│   ├── config/
│   │   ├── WebSocketConfig.java           # WebSocket 配置 + 拦截器注册
│   │   └── SaTokenConfig.java             # Sa-Token 配置
│   ├── interceptor/
│   │   └── WebSocketAuthInterceptor.java  # ⭐ 握手鉴权拦截器
│   ├── handler/
│   │   └── AuthWebSocketHandler.java      # ⭐ WebSocket 消息处理器
│   ├── controller/
│   │   ├── AuthController.java            # 登录/登出/获取 WS 地址
│   │   └── MessageController.java         # 消息广播接口
│   └── exception/
│       └── GlobalExceptionHandler.java    # 全局异常处理
└── src/test/                              # 集成测试
```

---

## 小结

整个方案就两个核心类：

1. **`WebSocketAuthInterceptor`** - 握手时用 `StpUtil.getLoginIdByToken(token)` 验证 Token，通过则把 `loginId` 存入 `session.attributes`
2. **`AuthWebSocketHandler`** - 从 `session.getAttributes().get("loginId")` 取用户 ID，处理消息

其他的都是配套代码。

---

完整代码：[sa-token-quickstart](https://github.com/tangtang-quickstart/sa-token-quickstart)
