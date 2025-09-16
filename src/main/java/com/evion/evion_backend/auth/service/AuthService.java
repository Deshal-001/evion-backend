package com.evion.evion_backend.auth.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.evion.evion_backend.auth.dto.LoginRequest;
import com.evion.evion_backend.auth.dto.RegisterRequest;
import com.evion.evion_backend.auth.model.Role;
import com.evion.evion_backend.auth.model.User;
import com.evion.evion_backend.auth.repository.UserRepository;
import com.evion.evion_backend.auth.security.JwtService;
import com.evion.evion_backend.userProfile.model.UserProfile;
import com.evion.evion_backend.userProfile.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {
        //Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        //Build user entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        //Save user
        userRepository.save(user);

        // Build and save user profile
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setName(user.getFirstName() + " " + user.getLastName());
        profile.setEmail(user.getEmail());
        profile.setTotalTrips(0);
        profile.setTotalDistanceKm(0.0);
        profile.setTotalEcoScore(0.0);
        profile.setTotalEnergyUsedKwh(0.0);
        profile.setTotalCo2EmittedKg(0.0);
        userProfileRepository.save(profile);

        //Generate JWT token
        return jwtService.generateToken(user.getEmail(),
                java.util.List.of(() -> user.getRole().name())); // Convert enum to GrantedAuthority
    }

    public String login(LoginRequest request) {
        //Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        //Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalStateException("Incorrect password");
        }

        //Generate JWT token
        return jwtService.generateToken(user.getEmail(),
                java.util.List.of(() -> user.getRole().name()));
    }

    // READ: Get user by email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    // READ: Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // UPDATE: Update user details
    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setRole(updatedUser.getRole());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        profile.setName(updatedUser.getFirstName() + " " + updatedUser.getLastName());
        userProfileRepository.save(profile);

        return userRepository.save(user);

    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("User not found");
        }
        userRepository.deleteById(id);
    }

    // Change password for logged-in user
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalStateException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Forgot password (reset password by email)
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
