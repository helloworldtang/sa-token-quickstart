package com.tangtang.satoken.apikey.service;

import cn.dev33.satoken.apikey.model.ApiKeyModel;
import java.util.List;

public interface ApiKeyService {

    /** 创建 API Key */
    ApiKeyModel createApiKey(Object loginId, String name);

    /** 获取用户所有 API Key */
    List<ApiKeyModel> listApiKeys(Object loginId);

    /** 删除 API Key */
    void deleteApiKey(Object loginId, String apiKey);
}
