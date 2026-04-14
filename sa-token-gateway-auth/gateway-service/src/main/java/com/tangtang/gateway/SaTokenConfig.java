package com.tangtang.gateway;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 网关鉴权配置
 *
 * @author tangtang
 */
@Configuration
public class SaTokenConfig {

    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                // 认证函数：每次请求进入时执行
                .setAuth(obj -> {
                    // 检查是否登录
                    StpUtil.checkLogin();
                })
                // 异常处理函数：每次认证异常时执行
                .setError(e -> {
                    // 自定义错误返回
                    if (e instanceof cn.dev33.satoken.exception.NotLoginException) {
                        return "{\"code\":401,\"msg\":\"请先登录\",\"data\":null}";
                    } else if (e instanceof cn.dev33.satoken.exception.NotPermissionException) {
                        return "{\"code\":403,\"msg\":\"无权限\",\"data\":null}";
                    } else if (e instanceof cn.dev33.satoken.exception.NotRoleException) {
                        return "{\"code\":403,\"msg\":\"无权限\",\"data\":null}";
                    }
                    return "{\"code\":500,\"msg\":\"系统异常\",\"data\":null}";
                });
    }
}
