# Java 21 + Lombok 编译问题说明

## 问题描述

在 Java 21 环境下使用 Maven 编译时，Lombok 1.18.34 会报以下错误：

```
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by lombok.permit.Permit
WARNING: Please consider reporting this to maintainers of class lombok.permit.Permit
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release

Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

## 原因分析

1. **根本原因**：Lombok 使用了 Java 内部 API（`sun.misc.Unsafe`），在 Java 21 中这些 API 被进一步限制
2. **兼容性问题**：Lombok 1.18.34 虽然声称支持 Java 21，但在某些配置下仍有问题
3. **"Picked up JAVA_TOOL_OPTIONS"**：这只是一个提示信息，不是错误，可以忽略

## 解决方案

### 方案 1：使用 IntelliJ IDEA（强烈推荐）✅

IDEA 的编译器可以更好地处理 Lombok 注解，不受此限制。

**步骤**：
1. 在 IDEA 中打开项目
2. 确保 Lombok 插件已安装（Settings → Plugins）
3. 确保注解处理已启用（Settings → Build, Execution, Deployment → Compiler → Annotation Processors）
4. 使用 IDEA 的 Build → Rebuild Project 或直接运行应用

**优点**：
- Lombok 支持完美
- 开发体验好
- 热部署支持好

### 方案 2：使用 Docker + Java 17 🐳

使用 Docker 提供的 Java 17 环境进行编译。

**步骤**：
```bash
docker run --rm \
  -v "$(pwd)":/workspace \
  -w /workspace \
  -e MAVEN_CONFIG=/root/.m2 \
  maven:3.9-eclipse-temurin-17 \
  mvn clean install
```

**优点**：
- 隔离环境
- Java 17 + Lombok 兼容性完美
- 适合 CI/CD

**缺点**：
- 需要安装 Docker
- 每次编译都需要 Docker

### 方案 3：安装 Java 17 并切换 ☕

在本地安装 Java 17，编译时使用 Java 17。

**步骤**：
```bash
# 使用 SDKMAN 安装 Java 17
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.11-tem
sdk use java 17.0.11-tem

# 或使用 Homebrew
brew install openjdk@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# 编译项目
mvn clean install
```

**优点**：
- 本地环境
- 一次配置，永久使用

**缺点**：
- 需要管理多个 Java 版本

### 方案 4：移除 Lombok 注解（不推荐）❌

手动添加 getter/setter，不使用 Lombok。

**缺点**：
- 代码冗长
- 失去 Lombok 的便利性
- 需要大量修改代码

## "Picked up JAVA_TOOL_OPTIONS" 说明

这个提示通常来自：

1. **Maven 编译器**：读取 `JAVA_TOOL_OPTIONS` 环境变量
2. **默认值**：通常是 `-Dfile.encoding=UTF-8`
3. **影响**：这只是一个提示，不影响编译

**如果你的系统设置了这个变量**，可以通过以下方式移除：

```bash
# 临时取消设置
unset JAVA_TOOL_OPTIONS

# 永久取消设置（根据你的 shell）
# ~/.zshrc 或 ~/.bashrc
```

**但如果没有任何实际编译错误，这个提示可以忽略**。

## 推荐方案总结

| 方案 | 难度 | 效果 | 推荐度 |
|------|--------|--------|--------|
| IDEA | ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Docker + Java 17 | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| 安装 Java 17 | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| 移除 Lombok | ⭐⭐⭐⭐ | ⭐ | ⭐ |

## 相关资源

- [Lombok 官方文档](https://projectlombok.org)
- [Java 21 发布说明](https://openjdk.org/projects/jdk/21)
- [Spring Boot + Lombok 集成](https://www.baeldung.com/lombok-ide)