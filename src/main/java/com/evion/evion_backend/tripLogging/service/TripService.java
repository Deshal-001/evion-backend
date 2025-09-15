package com.evion.evion_backend.tripLogging.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.evion.evion_backend.tripLogging.model.Trip;
import com.evion.evion_backend.tripLogging.repository.TripRepository;
import com.evion.evion_backend.utils.helper.EcoScoreCalculator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    // Save a trip
    public Trip logTrip(Long userId, double distanceKm, double durationSec, double energyUsedKwh, String drivingMode, double startBatteryPct, double endBatteryPct, List<Long> suggestedStationIds) {
        double avgSpeed = (durationSec > 0) ? (distanceKm / (durationSec / 3600.0)) : 0.0;
        int ecoScore = EcoScoreCalculator.calculateEcoScore(distanceKm, energyUsedKwh, durationSec, drivingMode, startBatteryPct, endBatteryPct);

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
                .build();

        return tripRepository.save(trip);
    }

    public List<Trip> getTripHistory(Long userId) {
        return tripRepository.findByUserIdOrderByTripDateDesc(userId);
    }

}
