package com.evion.evion_backend.userProfile.service;

import com.evion.evion_backend.userProfile.dto.UpdateUserProfileRequest;
import com.evion.evion_backend.userProfile.dto.UserProfileResponse;
import com.evion.evion_backend.userProfile.model.UserProfile;
import com.evion.evion_backend.userProfile.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileResponse getUserProfile(Long userId) {
        UserProfile profile = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        double avgEcoScore = profile.getTotalTrips() > 0
                ? profile.getTotalEcoScore() / profile.getTotalTrips()
                : 0;
        return UserProfileResponse.builder()
                .userId(profile.getUserId())
                .name(profile.getName())
                .email(profile.getEmail())
                .totalTrips(profile.getTotalTrips())
                .totalDistanceKm(profile.getTotalDistanceKm())
                .averageEcoScore(avgEcoScore)
                .totalCo2EmittedKg(profile.getTotalCo2EmittedKg())
                .build();
    }

    public UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest req) {
        UserProfile profile = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        profile.setName(req.getName());
        repository.save(profile);
        return getUserProfile(userId);
    }

    public void recordTrip(Long userId, double distanceKm, int ecoScore, double energyUsedKwh, double co2EmittedKg) {
        UserProfile profile = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        profile.setTotalTrips(profile.getTotalTrips() + 1);
        profile.setTotalDistanceKm(profile.getTotalDistanceKm() + distanceKm);
        profile.setTotalEcoScore(profile.getTotalEcoScore() + ecoScore);
        profile.setTotalEnergyUsedKwh(profile.getTotalEnergyUsedKwh() + energyUsedKwh);
        profile.setTotalCo2EmittedKg(profile.getTotalCo2EmittedKg() + co2EmittedKg);
        repository.save(profile);
    }
}
