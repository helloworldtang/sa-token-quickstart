# Sa-Token API Key 分发模块

## 项目介绍

这是一个基于 Sa-Token 官方 API Key 插件的实战示例项目，展示了如何快速构建一个具有 API Key 认证和限流功能的 RESTful API 服务。

## 功能特性

### API Key 管理
- ✅ 管理员登录与登出（基于 Sa-Token Session）
- ✅ API Key 创建（支持自定义名称）
- ✅ API Key 查询列表
- ✅ API Key 删除
- ✅ API Key 有效期管理

### API Key 认证
- ✅ Bearer sk-xxx 格式认证
- ✅ 基于 Sa-Token 官方 API Key 插件验证
- ✅ 支持 Authorization Header 请求
- ✅ 支持多种 API Key 传递方式（GET 参数、Header）

### 限流功能
- ✅ **IP 限流**：每分钟每个 IP 最多 60 次请求
- ✅ **API Key 限流**：每分钟最多 100 次请求，每天最多 10000 次
- ✅ 双重限流机制（IP + API Key）
- ✅ Redis 存储限流计数
- ✅ 自动过期时间计算

### 统计查询
- ✅ API Key 使用统计
- ✅ 实时显示限流配额
- ✅ 分钟级别和每日级别统计

### API 接口
- ✅ `/api/v1/chat` - 模拟 AI Chat 接口
- ✅ `/api/v1/models` - 模型列表接口
- ✅ `/api/v1/status` - 服务状态接口
- ✅ `/admin/login` - 管理员登录
- ✅ `/admin/info` - 获取管理员信息
- ✅ `/admin/logout` - 管理员登出
- ✅ `/admin/apikey/create` - 创建 API Key
- ✅ `/admin/apikey/list` - 获取 API Key 列表
- ✅ `/admin/apikey/{apiKey}` - 删除 API Key
- ✅ `/admin/apikey/{apiKey}/stats` - 获取 API Key 统计
- ✅ `/admin/apikey/{apiKey}/reset-rate-limit` - 重置限流

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.3.0
- **Sa-Token**: 1.45.0（官方 API Key 插件）
- **Redis**: 用于 API Key 持久化和限流计数
- **MockMvc**: 集成测试框架

## 项目结构

```
sa-token-apikey/
├── pom.xml
└── src/main/
    ├── java/com/tangtang/satoken/apikey/
    │   ├── SaTokenApikeyApplication.java     ← 启动类
    │   ├── config/
    │   │   ├── SaTokenApikeyConfig.java      ← API Key 插件配置
    │   │   ├── WebMvcConfig.java            ← 拦截器注册 + CORS
    │   │   └── BeanConfig.java                ← SaApiKeyTemplate Bean
    │   ├── controller/
    │   │   ├── AdminController.java            ← 管理接口（需登录）
    │   │   └── ApiController.java             ← 开放接口（需 API Key）
    │   ├── service/
    │   │   ├── ApiKeyService.java             ← 接口
    │   │   └── impl/ApiKeyServiceImpl.java  ← 实现
    │   ├── interceptor/
    │   │   └── ApiKeyInterceptor.java         ← 认证 + 限流拦截器
    │   └── limiter/
    │       ├── RateLimiter.java                ← IP 限流
    │       └── ApiKeyRateLimiter.java         ← API Key 限流
    └── resources/
        ├── application.yml
        └── static/index.html                 ← 可视化后台页面
```

## 快速开始

### 前置要求
- JDK 17+
- Maven 3.6+
- Redis 6.0+（用于 API Key 持久化和限流计数）

### 启动应用
```bash
# 构建
mvn clean install

# 运行
mvn spring-boot:run

# 访问
http://localhost:8082/
```

### API 使用示例

#### 1. 管理员登录
```bash
curl -X POST http://localhost:8082/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

#### 2. 创建 API Key
```bash
curl -X POST http://localhost:8082/admin/apikey/create \
  -H "Content-Type: application/json" \
  -H "Authorization: <token>" \
  -d '{"name":"测试 API Key"}'
```

#### 3. 使用 API Key 调用聊天接口
```bash
curl -X POST http://localhost:8082/api/v1/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: sk-xxx" \
  -d '{"message":"你好"}'
```

#### 4. 获取 API Key 使用统计
```bash
curl -X GET http://localhost:8082/admin/apikey/<apiKey>/stats \
  -H "Authorization: <token>"
```

## 注意事项

1. **限流说明**
   - IP 限流：限制每个 IP 每分钟最多 60 次请求
   - API Key 限流：限制每个 API Key 每分钟最多 100 次请求，每天最多 10000 次
   - 限流计数存储在 Redis 中，过期时间为 60 秒或当天结束

2. **API Key 格式**
   - 前缀：sk-
   - 示例：`sk-test1234567890abcdef`
   - 认证方式：Bearer sk-xxx

3. **响应头**
   - 限流信息通过 HTTP 响应头返回：
     - `X-RateLimit-Limit` / `X-RateLimit-Remaining`

4. **CORS 配置**
   - 已开放所有来源、方法和请求头，支持第三方调用

## 测试

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
cd sa-token-apikey
mvn test
```

## License

MIT License

## 参考资源

- Sa-Token 官方文档：https://sa-token.cc/
- Sa-Token GitHub：https://github.com/dromara/Sa-Token
- 完整项目代码：https://github.com/helloworldtang/sa-token-quickstart
