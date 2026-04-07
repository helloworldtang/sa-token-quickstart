package com.tangtang.satoken.apikey.config;

import cn.dev33.satoken.apikey.template.SaApiKeyTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public SaApiKeyTemplate saApiKeyTemplate() {
        return new SaApiKeyTemplate();
    }
}
