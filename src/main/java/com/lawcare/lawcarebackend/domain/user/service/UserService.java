package com.lawcare.lawcarebackend.domain.user.service;

import com.lawcare.lawcarebackend.domain.user.entity.User;
import com.lawcare.lawcarebackend.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }
}
