package com.lawcare.lawcarebackend.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 잘못되었습니다.")
    @Schema(example = "user@example.com", description = "사용자 이메일")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(example = "password1234!", description = "사용자 비밀번호")
    private String password;
}
