package com.tangtang.satoken.elegant.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 踢人请求DTO
 * <p>
 * 优雅点：
 * 1. 支持根据用户ID或会话ID踢人
 * 2. 支持踢人原因记录
 * 3. 参数校验清晰
 *
 * @author Agent唐
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "踢人请求")
public class KickoutRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 踢人原因（可选）
     */
    @Schema(description = "踢人原因")
    private String reason;
}