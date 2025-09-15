package com.evion.evion_backend.tripLogging.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.evion.evion_backend.tripLogging.dto.TripRequest;
import com.evion.evion_backend.tripLogging.model.Trip;
import com.evion.evion_backend.tripLogging.service.TripService;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping("/log")
    public ResponseEntity<Trip> logTrip(@RequestBody TripRequest request) {
        Trip trip = tripService.logTrip(
                request.getUserId(),
                request.getDistanceKm(),
                request.getDurationSec(),
                request.getEnergyUsedKwh(),
                request.getDrivingMode(),
                request.getStartBatteryPct(),
                request.getEndBatteryPct(),
                request.getSuggestedStationIds()
        );
        return ResponseEntity.ok(trip);
    }

    @GetMapping
    public ResponseEntity<List<Trip>> getTripHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(tripService.getTripHistory(userId));
    }

}
