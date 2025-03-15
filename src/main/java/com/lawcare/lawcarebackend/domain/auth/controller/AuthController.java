package com.lawcare.lawcarebackend.domain.auth.controller;

import com.lawcare.lawcarebackend.common.dto.SuccessResponse;
import com.lawcare.lawcarebackend.domain.auth.dto.request.SignUpRequestDTO;
import com.lawcare.lawcarebackend.domain.auth.dto.response.SignUpResponseDTO;
import com.lawcare.lawcarebackend.domain.auth.service.AuthService;
import com.lawcare.lawcarebackend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "회원가입",
        description = "회원가입 요청을 처리합니다. 이메일, 비밀번호, 이름 등 필수 필드를 입력받습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = SignUpResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 누락 또는 형식 오류)")
    })
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<SignUpResponseDTO>> signUp(
        @Valid @RequestBody SignUpRequestDTO requestDTO,
        HttpServletRequest request
    ) {
        User user = authService.signUp(requestDTO);
        SignUpResponseDTO responseData = new SignUpResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole().name()
        );
        SuccessResponse<SignUpResponseDTO> response = SuccessResponse.of(
            HttpStatus.OK.value(),
            "회원가입 성공",
            responseData,
            request.getRequestURI()
        );
        return ResponseEntity.ok(response);
    }
}