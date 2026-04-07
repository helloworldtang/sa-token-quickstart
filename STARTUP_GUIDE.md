# 启动指南

## 快速启动

### 1. sa-token-basic（基础示例）
```bash
cd sa-token-basic
mvn spring-boot:run
```
访问 Knife4j 文档: http://localhost:8080/doc.html

### 2. sa-token-elegant-demo（优雅实践）

**重要提示**：由于系统使用 Java 21，Maven 命令行编译时 Lombok 存在兼容性问题。

**推荐使用 IntelliJ IDEA 进行开发**：

1. 在 IDEA 中打开项目
2. 确保已安装 Lombok 插件（File → Settings → Plugins）
3. 确保注解处理已启用（File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors）
4. 点击运行按钮启动应用

访问 Knife4j 文档: http://localhost:8081/doc.html

### 3. 使用 Docker 编译（备选方案）
如果需要在命令行编译，可以使用 Docker 提供的 Java 17 环境：

```bash
docker run --rm -v "$(pwd)":/workspace -w /workspace \
  -e MAVEN_CONFIG=/root/.m2 \
  maven:3.9-eclipse-temurin-17 \
  mvn clean install
```

## Knife4j 功能

### sa-token-basic (8080)
- **文档地址**: http://localhost:8080/doc.html
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 文档**: http://localhost:8080/v3/api-docs

### sa-token-elegant-demo (8081)
- **文档地址**: http://localhost:8081/doc.html
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API 文档**: http://localhost:8081/v3/api-docs

## 测试接口

### 1. 登录接口
- `POST /auth/login`
- Body: `{"username": "admin", "password": "123456"}`
- 返回: Token 信息

### 2. 使用 Token
- 将返回的 Token 复制
- 点击右上角「Authorize」按钮
- 输入: `Bearer <你的Token>`
- 点击 Authorize 确认

### 3. 测试需要认证的接口
- `GET /auth/current` - 获取当前用户信息（需要登录）
- `GET /user/list` - 获取用户列表（需要登录）
- `GET /user/1` - 查看用户详情（需要 user:view 权限）

## IDE 配置建议

### IntelliJ IDEA
1. 安装 Lombok 插件
2. 启用注解处理
3. 设置 SDK 为 Java 17（可选）

### VS Code
1. 安装 "Lombok" 插件
2. 安装 "Spring Boot Extension Pack"
3. 配置 Java 版本

## 常见问题

### Q1: Maven 编译失败，提示 Lombok 错误
A: 使用 IDEA 打开项目，使用 IDE 的编译功能，或者使用 Docker 提供的 Java 17 环境。

### Q2: Redis 连接失败
A: 确保 Redis 已启动，或者修改 `application.yml` 中的 Redis 配置：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379  # 注意：默认是 6379，Redis 默认是 6379
```

### Q3: Token 过期
A: 默认 Token 有效期 30 天，可以在 `application.yml` 中修改：
```yaml
sa-token:
  timeout: 2592000  # 30天，单位：秒
```

## 开发建议

1. **推荐使用 IDEA**：对 Lombok 支持更好
2. **使用 Hot Reload**：Spring Boot DevTools 支持热部署
3. **使用 Knife4j 调试**：在线调试 API，无需 Postman
4. **查看日志**：Sa-Token 配置了详细日志，便于调试

## 关于 "Picked up JAVA_TOOL_OPTIONS" 提示

这个提示**不是错误**，只是一个信息提示：

1. **出现原因**：Maven 编译器读取了 `JAVA_TOOL_OPTIONS` 环境变量
2. **常见值**：通常是 `-Dfile.encoding=UTF-8`
3. **影响**：不影响编译和运行，可以忽略
4. **如何移除**：如果想要移除这个提示
   ```bash
   # 临时移除
   unset JAVA_TOOL_OPTIONS
   ```

**如果没有实际编译错误，这个提示可以忽略**。

真正的编译错误是 Lombok 与 Java 21 的兼容性问题，详见 [JAVA21_LOMBOK_ISSUE.md](JAVA21_LOMBOK_ISSUE.md)。