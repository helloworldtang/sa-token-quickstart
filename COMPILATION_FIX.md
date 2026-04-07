# 编译问题解决方案

## 问题描述

**错误信息**：
```
Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

**根本原因**：
- 系统使用 Java 21
- Lombok 1.18.34 与 Java 21 存在兼容性问题
- Lombok 使用了已废弃的 Java 内部 API (`sun.misc.Unsafe`)

## 解决方案

### 方案1：使用 IntelliJ IDEA（强烈推荐）✅✅✅

**步骤**：

1. 在 IDEA 中打开项目
   ```
   File → Open → 选择项目目录
   ```

2. 确保插件已安装
   ```
   File → Settings → Plugins → 搜索 "Lombok"
   安装 Lombok 插件
   ```

3. 启用注解处理
   ```
   File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   勾选 "Enable annotation processing"
   ```

4. 重新构建
   ```
   Build → Rebuild Project
   ```

**优点**：
- ✅ IDEA 的编译器可以完美处理 Lombok 注解
- ✅ 不受 Java 21 的限制
- ✅ 开发体验好，支持热部署

### 方案2：使用 Java 17

**步骤**：

1. 安装 Java 17
   ```bash
   # 使用 SDKMAN
   curl -s "https://get.sdkman.io" | bash
   source "$HOME/.sdkman/bin/sdkman-init.sh"
   sdk install java 17.0.11-tem
   sdk use java 17.0.11-tem

   # 或使用 Homebrew
   brew install openjdk@17
   ```

2. 配置项目使用 Java 17
   ```bash
   export JAVA_HOME=/path/to/java17
   export PATH=$JAVA_HOME/bin:$PATH

   # 编译
   mvn clean compile
   ```

3. 在 IDEA 中切换 JDK
   ```
   File → Project Structure → Project Settings → Project
   → Project SDK → 选择 Java 17
   ```

### 方案3：使用 Docker 编译

**命令**：
```bash
docker run --rm \
  -v "$(pwd)":/workspace \
  -w /workspace \
  -e MAVEN_CONFIG=/root/.m2 \
  maven:3.9-eclipse-temurin-17 \
  mvn clean install
```

**优点**：
- ✅ 使用 Java 17 编译
- ✅ 隔离环境
- ✅ 适合 CI/CD

### 方案4：降级 Lombok 版本（不推荐）

**尝试使用更早版本的 Lombok**：

```xml
<!-- 在父 pom.xml 中修改 -->
<lombok.version>1.18.30</lombok.version>
```

⚠️ **注意**：这可能会影响其他 Lombok 功能。

### 方案5：移除 Lombok 注解（不推荐）

手动添加 getter/setter，不使用 Lombok。

**缺点**：
- 代码冗长
- 失去 Lombok 的便利性
- 需要大量修改代码

## 推荐方案总结

| 方案 | 难度 | 效果 | 推荐度 |
|------|--------|--------|--------|
| IDEA 编译 | ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Java 17 | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| Docker 编译 | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 降级 Lombok | ⭐⭐⭐⭐ | ⭐⭐ | ⭐ |
| 移除 Lombok | ⭐⭐⭐⭐⭐ | ⭐ | ⭐ |

## 关于 AuthController.kickout 方法

该方法已经被标记为 `@Deprecated`，建议使用新的 `SessionController` 中的接口：

- `GET /admin/online-sessions` - 获取在线会话列表
- `POST /admin/kickout/{userId}` - 根据用户ID踢人
- `POST /admin/kickout-session/{sessionId}` - 踢掉指定会话

## 验证编译成功

### 在 IDEA 中编译
```bash
# 1. 打开项目
# 2. Build → Rebuild Project
```

查看是否出现编译错误。

### 启动应用
编译成功后，可以启动应用：
```bash
# 在 IDEA 中找到 SaTokenElegantApplication
# 右键 → Run 'SaTokenElegantApplication'
```

或命令行：
```bash
mvn spring-boot:run -f sa-token-elegant-demo/pom.xml
```

## 前端使用

编译成功后，使用纯 HTML 前端进行测试：

1. 查看 `FRONTEND_IMPLEMENTATION.md`
2. 保存为 HTML 文件：
   - `user-management.html`
   - `online-sessions.html`
3. 在浏览器中打开

**API 地址配置**：
在 HTML 文件中找到：
```javascript
const API_BASE = 'http://localhost:8081';
```
根据实际部署修改此地址。

## 总结

**Maven 命令行编译无法解决 Java 21 + Lombok 的兼容性问题。**

**最简单的解决方案**：使用 IntelliJ IDEA 进行开发和编译。

IDEA 的编译器可以完美处理 Lombok 注解，不受 Java 21 的限制。

---

**编译问题只能通过 IDEA 解决，Maven 命令行暂时无法通过。**