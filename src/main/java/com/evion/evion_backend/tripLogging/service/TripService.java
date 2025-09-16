package com.evion.evion_backend.tripLogging.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.evion.evion_backend.tripLogging.model.Trip;
import com.evion.evion_backend.tripLogging.repository.TripRepository;
import com.evion.evion_backend.userProfile.service.UserProfileService;
import com.evion.evion_backend.utils.helper.EcoScoreCalculator;
import com.evion.evion_backend.utils.helper.EvionCalculations;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserProfileService userProfileService; // Inject UserProfileService

    // Save a trip
    public Trip logTrip(Long userId, double distanceKm, double durationSec, double energyUsedKwh, String drivingMode, double startBatteryPct, double endBatteryPct, List<Long> suggestedStationIds) {
        double avgSpeed = (durationSec > 0) ? (distanceKm / (durationSec / 3600.0)) : 0.0;
        int ecoScore = EcoScoreCalculator.calculateEcoScore(distanceKm, energyUsedKwh, durationSec, drivingMode, startBatteryPct, endBatteryPct);

        double co2Saved = EvionCalculations.calculateCo2Saved(distanceKm);

        Trip trip = Trip.builder()
                .userId(userId)
                .distanceKm(distanceKm)
                .avgSpeedKmh(avgSpeed)
                .energyUsedKwh(energyUsedKwh)
                .ecoScore(ecoScore)
                .startBatteryPct(startBatteryPct)
                .endBatteryPct(endBatteryPct)
                .tripDate(LocalDateTime.now())
                .suggestedStationIds(suggestedStationIds)
                .co2SavedKg(co2Saved)
                .build();

        Trip savedTrip = tripRepository.save(trip);

        // Update user profile with new trip data
        userProfileService.recordTrip(userId, distanceKm, ecoScore, co2Saved);

        return savedTrip;
    }

    public List<Trip> getTripHistory(Long userId) {
        return tripRepository.findByUserIdOrderByTripDateDesc(userId);
    }

}
