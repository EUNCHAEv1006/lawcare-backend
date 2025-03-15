package com.lawcare.lawcarebackend.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpResponseDTO {

    @Schema(example = "1", description = "생성된 사용자 ID")
    private Long userId;

    @Schema(example = "user@example.com", description = "사용자 이메일")
    private String email;

    @Schema(example = "김은채", description = "사용자 이름")
    private String name;

    @Schema(example = "USER", description = "사용자 역할(USER 또는 COUNSELOR)")
    private String role;
}
