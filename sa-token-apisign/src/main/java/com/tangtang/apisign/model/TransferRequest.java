package com.tangtang.apisign.model;

/**
 * 转账请求参数
 */
public class TransferRequest {

    /** 用户 ID */
    private Long userId;

    /** 转账金额（分） */
    private Long amount;

    /** 备注 */
    private String remark;

    public TransferRequest() {
    }

    public TransferRequest(Long userId, Long amount, String remark) {
        this.userId = userId;
        this.amount = amount;
        this.remark = remark;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
