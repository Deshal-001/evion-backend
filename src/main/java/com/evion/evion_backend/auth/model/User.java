package com.evion.evion_backend.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role; // ROLE_USER, ROLE_ADMIN

    @Column(nullable = false)
    private boolean enabled = true;

    private String firstName;
    private String lastName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // public String getEmail() {
    //     return email;
    // }
    // public String getPassword() {
    //     return password;
    // }
    // public Role getRole() {
    //     return role;
    // }
    // public String getFirstName() {
    //     return firstName;
    // }
    // public String getLastName() {
    //     return lastName;
    // }
}
