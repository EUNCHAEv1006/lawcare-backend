package com.lawcare.lawcarebackend.domain.auth.service;

import com.lawcare.lawcarebackend.Role;
import com.lawcare.lawcarebackend.common.security.TokenProvider;
import com.lawcare.lawcarebackend.domain.auth.dto.request.LoginRequestDTO;
import com.lawcare.lawcarebackend.domain.auth.dto.request.SignUpRequestDTO;
import com.lawcare.lawcarebackend.domain.user.entity.User;
import com.lawcare.lawcarebackend.domain.user.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 회원가입 로직
     *
     * @param requestDTO 회원가입 요청 DTO(Bean Validation 통해 필수값 및 이메일 형식 검사 완료)
     * @return 가입된 User 엔티티
     */
    public User signUp(SignUpRequestDTO requestDTO) {

        if (userService.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일: " + requestDTO.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(requestDTO.getPassword());
        Role role = (requestDTO.getRole() != null) ? requestDTO.getRole() : Role.USER;

        User newUser = new User(
            requestDTO.getEmail(),
            hashedPassword,
            requestDTO.getName(),
            role,
            requestDTO.getNationality()
        );

        return userService.createUser(newUser);
    }

    /**
     * 로그인 로직: 사용자 인증 후 JWT 토큰을 생성하여 반환
     *
     * @param requestDTO 로그인 요청 DTO
     * @return 생성된 JWT 토큰 문자열
     */
    public String login(LoginRequestDTO requestDTO) {
        User user = userService.findByEmail(requestDTO.getEmail())
                               .orElseThrow(() -> new UsernameNotFoundException("찾을 수 없는 사용자: " + requestDTO.getEmail()));

        if (!passwordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호 불일치");
        }

        return tokenProvider.createToken(user.getId(), user.getRole());
    }
}
