package com.tangtang.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * API Key 实体
 *
 * @author tangtang
 */
@Data
@Accessors(chain = true)
@TableName("sys_api_key")
public class ApiKey {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("api_key")
    private String apiKey;

    @TableField("secret_key")
    private String secretKey;

    @TableField("role_id")
    private Long roleId;

    @TableField("limit_count")
    private Integer limitCount;

    @TableField("app_name")
    private String appName;

    @TableField("contact_email")
    private String contactEmail;

    @TableField("status")
    private Integer status;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // 手动添加 getter/setter 方法
    public Integer getLimitCount() {
        return limitCount;
    }

    public void setLimitCount(Integer limitCount) {
        this.limitCount = limitCount;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}