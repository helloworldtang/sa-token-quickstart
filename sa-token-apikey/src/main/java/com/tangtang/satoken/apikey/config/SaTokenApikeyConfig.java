package com.tangtang.satoken.apikey.config;

import cn.dev33.satoken.apikey.config.SaApiKeyConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SaTokenApikeyConfig {

    @Value("${apikey.prefix:sk-}")
    private String prefix;

    @Value("${apikey.timeout:2592000}")
    private long timeout;

    @Value("${apikey.is-record-index:true}")
    private boolean isRecordIndex;

    @Bean
    @Primary  // ★ 解决与 starter 自动注册 Bean 的冲突
    public SaApiKeyConfig saApiKeyConfig() {
        SaApiKeyConfig config = new SaApiKeyConfig();
        config.setPrefix(prefix);
        config.setTimeout(timeout);
        config.setIsRecordIndex(isRecordIndex);
        return config;
    }
}
