package com.evion.evion_backend.chargingStationManagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.evion.evion_backend.chargingStationManagement.dto.ChargingStationRequest;
import com.evion.evion_backend.chargingStationManagement.dto.ChargingStationResponse;
import com.evion.evion_backend.chargingStationManagement.service.ChargingStationService;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class ChargingStationController {

    private final ChargingStationService service;

    @GetMapping
    public ResponseEntity<List<ChargingStationResponse>> getAllStations() {
        return ResponseEntity.ok(service.getAllStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargingStationResponse> getStation(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStation(id));
    }

    @PostMapping
    public ResponseEntity<ChargingStationResponse> addStation(@RequestBody ChargingStationRequest request) {
        return ResponseEntity.ok(service.addStation(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChargingStationResponse> updateStation(
            @PathVariable Long id, @RequestBody ChargingStationRequest request) {
        return ResponseEntity.ok(service.updateStation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        service.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
}
