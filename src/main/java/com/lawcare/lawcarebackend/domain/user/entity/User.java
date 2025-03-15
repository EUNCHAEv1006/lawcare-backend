package com.lawcare.lawcarebackend.domain.user.entity;

import com.lawcare.lawcarebackend.Role;
import com.lawcare.lawcarebackend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(length = 2)
    private String nationality;

    public User(String email, String password, String name, Role role, String nationality) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.nationality = nationality;
    }
}
