package com.evion.evion_backend.userProfile.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long userId;

    private String name;

    private String email;

    private int totalTrips;

    private double totalDistanceKm;

    private double totalEcoScore;

    private double totalCo2SavedKg;

}
