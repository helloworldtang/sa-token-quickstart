# Sa-Token 多模块示例项目

## 项目结构

本项目展示了 Sa-Token 框架的基础使用和优雅实践，采用多模块结构设计：

```
sa-token-quickstart/
├── sa-token-basic/          # 基础示例模块
├── sa-token-elegant-demo/   # 优雅实践模块
└── pom.xml                  # 父POM
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

## 技术栈

- Spring Boot 3.2.0
- Sa-Token 1.37.0
- Java 17
- Lombok
- Knife4j (Swagger)
- Testcontainers
- Redis（可选）

## 快速开始

### 前置要求
- JDK 17+
- Maven 3.6+

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
