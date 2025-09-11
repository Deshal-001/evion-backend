package com.evion.evion_backend.chargingStationManagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.evion.evion_backend.chargingStationManagement.dto.ChargingStationRequest;
import com.evion.evion_backend.chargingStationManagement.dto.ChargingStationResponse;
import com.evion.evion_backend.chargingStationManagement.model.ChargingStation;
import com.evion.evion_backend.chargingStationManagement.repository.ChargingStationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargingStationService {

    private final ChargingStationRepository repository;

    public List<ChargingStationResponse> getAllStations() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ChargingStationResponse getStation(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalStateException("Station not found"));
    }

    public ChargingStationResponse addStation(ChargingStationRequest request) {
        ChargingStation station = toEntity(request);
        return toResponse(repository.save(station));
    }

    public ChargingStationResponse updateStation(Long id, ChargingStationRequest request) {
        ChargingStation existing = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Station not found"));

        existing.setName(request.getName());
        existing.setLatitude(request.getLatitude());
        existing.setLongitude(request.getLongitude());
        existing.setChargingType(request.getChargingType());
        existing.setChargingSpeed(request.getChargingSpeed());
        existing.setCostPerKwh(request.getCostPerKwh());
        existing.setStatus(request.getStatus());

        return toResponse(repository.save(existing));
    }

    public void deleteStation(Long id) {
        repository.deleteById(id);
    }

    // 🔄 Helper methods
    private ChargingStation toEntity(ChargingStationRequest request) {
        return ChargingStation.builder()
                .name(request.getName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .chargingType(request.getChargingType())
                .chargingSpeed(request.getChargingSpeed())
                .costPerKwh(request.getCostPerKwh())
                .status(request.getStatus())
                .build();
    }

    private ChargingStationResponse toResponse(ChargingStation station) {
        return ChargingStationResponse.builder()
                .id(station.getId())
                .name(station.getName())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .chargingType(station.getChargingType())
                .chargingSpeed(station.getChargingSpeed())
                .costPerKwh(station.getCostPerKwh())
                .status(station.getStatus())
                .build();
    }
}
