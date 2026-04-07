# Lombok 编译问题说明

## 问题描述

在Maven编译时，可能会遇到Lombok注解无法生效的问题。这是由于Lombok的注解处理器配置问题导致的。

## 解决方案

### 方案1：使用IDEA编译（推荐）

1. 在IntelliJ IDEA中打开项目
2. 安装Lombok插件：File → Settings → Plugins → 搜索"Lombok" → 安装
3. 启用注解处理：File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors → 勾选"Enable annotation processing"
4. 重新构建项目：Build → Rebuild Project

IDEA会自动处理Lombok注解，无需额外配置。

### 方案2：使用Maven编译

需要在父POM中添加maven-compiler-plugin配置：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 方案3：跳过测试快速验证

如果只是为了快速验证项目结构，可以跳过测试：

```bash
mvn clean package -DskipTests
```

## 验证编译

编译成功后，运行测试：

```bash
cd sa-token-elegant-demo
mvn test
```

## 推荐开发方式

推荐使用IntelliJ IDEA进行开发，IDEA对Lombok的支持更好，可以：
- 自动补全Lombok生成的getter/setter
- 代码提示更准确
- 调试时可以查看字段值
- 避免编译问题

## 相关资源

- [Lombok官方文档](https://projectlombok.org/features/all)
- [Spring Boot + Lombok集成指南](https://www.baeldung.com/lombok-ide)
