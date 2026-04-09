# Sa-Token API 接口参数签名模块

基于 Sa-Token Sign 插件实现的 API 接口参数签名示例，展示如何防止请求参数被篡改和重放攻击。

## 模块简介

本模块通过 Sa-Token 的签名插件 (`sa-token-sign`) 实现了一套完整的 API 签名机制，用于：

- **防止参数篡改**：确保请求参数在传输过程中未被修改
- **防止重放攻击**：通过时间戳和随机数确保请求的唯一性
- **接口安全保护**：为敏感接口（如转账、支付）提供签名验证

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.6+

### 启动应用

```bash
cd sa-token-apisign
mvn spring-boot:run
```

应用启动后访问：
- 签名帮助接口：http://localhost:8083/sign/help
- API 文档：http://localhost:8083/doc.html

## 核心功能

### 1. 签名校验

业务接口只需一行代码即可完成签名校验：

```java
// 在 Controller 方法中调用
SaSignUtil.checkRequest(SaHolder.getRequest());
```

校验失败会自动抛出 `SaSignException`，由全局异常处理器统一处理。

### 2. 签名生成帮助

访问 `/sign/help` 接口可以：

- 查看签名生成的完整步骤
- 获取可直接使用的带签名 URL
- 理解客户端如何构造签名参数

示例请求：
```
GET /sign/help?userId=1001&amount=5000&remark=测试
```

响应示例：
```json
{
  "code": 200,
  "msg": "签名示例生成成功",
  "data": {
    "step1_bizParams": {"userId": 1001, "amount": 5000, "remark": "测试"},
    "step2_signedParams": {
      "amount": 5000,
      "nonce": "abc123...",
      "remark": "测试",
      "sign": "md5值",
      "timestamp": "1744118400000",
      "userId": 1001
    },
    "step3_queryString": "amount=5000&nonce=xxx&remark=xxx&sign=xxx&timestamp=xxx&userId=1001",
    "step4_fullUrl": "http://localhost:8083/api/transfer/submit?amount=5000&nonce=xxx&remark=xxx&sign=xxx&timestamp=xxx&userId=1001",
    "tip": "复制 step4_fullUrl 到浏览器/Postman 即可发起合法请求（15分钟内有效）"
  }
}
```

### 3. 业务接口示例

模块提供了两个模拟业务接口：

#### 转账接口

```
GET /api/transfer/submit?userId=1001&amount=5000&remark=测试&timestamp=xxx&nonce=xxx&sign=xxx
```

#### 查询余额接口

```
GET /api/transfer/balance?userId=1001&timestamp=xxx&nonce=xxx&sign=xxx
```

**注意**：即使是查询接口也应进行签名校验，防止参数被篡改（如修改 userId 查询他人信息）。

## 签名机制说明

### 签名参数

每个签名请求必须包含以下参数：

| 参数 | 说明 | 示例 |
|------|------|------|
| timestamp | 当前毫秒时间戳，误差不超过 15 分钟 | 1744118400000 |
| nonce | 随机字符串，每次请求不同（防重放） | abc123def456 |
| sign | 对所有参数（含密钥）按字典序排序后 MD5 的值 | 098f6bcd4621d373cade4e832627b4f6 |

### 签名生成步骤

1. **收集业务参数**：`userId=1001&amount=5000&remark=测试`
2. **追加时间戳**：添加 `timestamp`（毫秒）
3. **追加随机数**：添加 `nonce`（UUID）
4. **排序拼接**：按字典序排序所有参数，拼接成 `k1=v1&k2=v2&...&key=secretKey`
5. **计算签名**：对拼接后的字符串进行 MD5 摘要
6. **发送请求**：将 `timestamp`、`nonce`、`sign` 一并放入请求参数

### 客户端实现

#### 使用 Sa-Token 官方工具（推荐）

```java
import cn.dev33.satoken.sign.template.SaSignUtil;

// 方法一：自动生成并拼接为 query string
String queryString = SaSignUtil.addSignParamsAndJoin(params);

// 方法二：只生成签名参数，手动拼接
Map<String, Object> signedParams = SaSignUtil.addSignParams(params);
String queryString = toQueryString(signedParams);
```

#### 手动实现（学习用）

参见 `SignClientHelper` 类的实现，理解签名生成的完整逻辑。

### 配置说明

在 `application.yml` 中配置密钥：

```yaml
sa-token:
  sign:
    secret-key: your-secret-key-here  # 必须与客户端保持一致
```

**安全提示**：生产环境中密钥应通过环境变量或配置中心管理，不应硬编码。

## 测试

### 运行测试

```bash
mvn test
```

### 测试用例

模块包含完整的单元测试：

- `SignClientHelperTest`：测试签名生成工具类
- `TransferControllerTest`：测试转账接口的签名校验

### 快速验证签名

1. 访问 `http://localhost:8083/sign/help`
2. 复制响应中的 `step4_fullUrl`
3. 在浏览器或 Postman 中访问该 URL
4. 观察到签名校验通过，请求成功

4. 修改 URL 中的任意参数或签名
5. 再次访问，会收到签名验证失败的错误

## 防护机制

### 1. 防参数篡改

签名基于所有请求参数和密钥生成，任何参数的变化都会导致签名不匹配，服务器会拒绝请求。

### 2. 防重放攻击

- **时间戳**：请求有效期 15 分钟，超时请求被拒绝
- **随机数**：每次请求使用不同的 nonce，防止相同请求被重复提交

### 3. 密钥保护

- 密钥只在签名计算时使用，不在网络中传输
- 服务端和客户端各自保存密钥，第三方无法伪造签名

## 最佳实践

### 1. 签名校验位置

**推荐**：在 Controller 方法第一行手动调用 `SaSignUtil.checkRequest()`

```java
@GetMapping("/transfer")
public ApiResponse transfer(@RequestParam Long userId, @RequestParam Long amount) {
    SaSignUtil.checkRequest(SaHolder.getRequest());
    // 业务逻辑...
}
```

**原因**：代码简洁，逻辑清晰，适合不同接口需要不同签名规则的场景。

### 2. 例外处理

签名校验失败会抛出 `SaSignException`，建议通过全局异常处理器统一处理：

```java
@ExceptionHandler(SaSignException.class)
public ApiResponse handleSignException(SaSignException e) {
    return ApiResponse.error("签名验证失败: " + e.getMessage());
}
```

### 3. 密钥管理

- 开发环境：使用固定密钥
- 测试环境：每个环境使用不同密钥
- 生产环境：通过配置中心或环境变量管理密钥
- 密钥定期轮换：建议每 3-6 个月更换一次密钥

### 4. 安全注意事项

- ❌ 不应在日志中打印完整签名参数（含密钥）
- ❌ 不应在错误响应中泄露签名生成细节
- ✅ 生产环境应关闭 `/sign/help` 等调试接口
- ✅ 重要接口建议结合 HTTPS 使用

## 常见问题

### Q1: 签名一直失败，如何排查？

1. 检查客户端和服务端的密钥是否一致
2. 检查时间戳是否在有效期内（当前时间 ±15 分钟）
3. 确认所有参数都参与了签名计算
4. 确认参数排序规则（按字典序升序）
5. 使用 `/sign/help` 接口生成示例 URL 进行对比

### Q2: 如何在不同环境使用不同密钥？

```yaml
# application-dev.yml
sa-token:
  sign:
    secret-key: ${SIGN_SECRET:dev-secret-key}

# application-prod.yml
sa-token:
  sign:
    secret-key: ${SIGN_SECRET:prod-secret-key}
```

### Q3: 签名对性能有影响吗？

签名计算涉及 MD5 哈希和字符串操作，性能损耗极小（<1ms）。对于大多数业务场景，签名带来的安全性提升远大于性能开销。

### Q4: 所有接口都需要签名吗？

不是。建议：
- ✅ 敏感接口（转账、支付、数据修改）：必须签名
- ✅ 涉及隐私数据的查询接口：必须签名
- ⚠️ 公开数据接口：可选签名
- ❌ 健康检查、静态资源：无需签名

## 参考资料

- [Sa-Token 文档 - API 接口参数签名](https://sa-token.cc/doc.html#/use/sign)
- [Sa-Token Sign 源码](https://github.com/dromara/sa-token/tree/dev/sa-token-plugin/sa-token-plugin-sign)

## License

MIT License