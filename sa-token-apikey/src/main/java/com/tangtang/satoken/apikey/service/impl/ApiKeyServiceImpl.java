package com.tangtang.satoken.apikey.service.impl;

import cn.dev33.satoken.apikey.model.ApiKeyModel;
import cn.dev33.satoken.apikey.template.SaApiKeyTemplate;
import com.tangtang.satoken.apikey.service.ApiKeyService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private final SaApiKeyTemplate apikeyTemplate = new SaApiKeyTemplate();

    @Override
    public ApiKeyModel createApiKey(Object loginId, String name) {
        ApiKeyModel ak = apikeyTemplate.createApiKeyModel(loginId);
        // 将名称存储到 extraData 中
        if (name != null && !name.isEmpty()) {
            ak.setExtraData(Map.of("name", name));
        } else {
            ak.setExtraData(Map.of("name", "未命名"));
        }
        apikeyTemplate.saveApiKey(ak);  // ★ 持久化到 Redis
        return ak;
    }

    @Override
    public List<ApiKeyModel> listApiKeys(Object loginId) {
        return apikeyTemplate.getApiKeyList(loginId);
    }

    @Override
    public void deleteApiKey(Object loginId, String apiKey) {
        apikeyTemplate.deleteApiKey(apiKey);
    }
}
