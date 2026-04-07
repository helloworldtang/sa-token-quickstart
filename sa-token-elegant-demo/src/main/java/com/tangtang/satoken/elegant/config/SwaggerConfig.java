package com.tangtang.satoken.elegant.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置类
 * <p>
 * 优雅点：
 * 1. 使用 OpenAPI 规范，与 Knife4j 完美集成
 * 2. 配置信息集中管理
 * 3. 支持在线调试，提升开发效率
 *
 * @author Agent唐
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sa-Token 优雅实践 API")
                        .description("Sa-Token 权限认证框架 - 优雅实践示例项目接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Agent唐")
                                .url("https://github.com/helloworldtang"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
