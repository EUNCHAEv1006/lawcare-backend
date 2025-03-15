package com.lawcare.lawcarebackend.domain.auth.dto.request;

import com.lawcare.lawcarebackend.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpRequestDTO {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 잘못되었습니다.")
    @Schema(example = "user@example.com", description = "사용자 이메일")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(example = "password1234!", description = "사용자 비밀번호")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Schema(example = "김은채", description = "사용자 이름")
    private String name;

    @Schema(example = "USER", description = "사용자 역할(USER 또는 COUNSELOR)")
    private Role role;

    @Schema(example = "KR", description = "국적 (ISO 3166-1 alpha-2 코드")
    private String nationality;
}
