package com.lawcare.lawcarebackend.domain.auth.service;

import com.lawcare.lawcarebackend.Role;
import com.lawcare.lawcarebackend.domain.auth.dto.request.SignUpRequestDTO;
import com.lawcare.lawcarebackend.domain.user.entity.User;
import com.lawcare.lawcarebackend.domain.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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
}
