package com.evion.evion_backend.userProfile.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private Long userId;
    private String name;
    private String email;
    private int totalTrips;
    private double totalDistanceKm;
    private double averageEcoScore;
    private double totalCo2SavedKg;
}
