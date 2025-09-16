package com.evion.evion_backend.vehicle.service;

import com.evion.evion_backend.vehicle.model.Vehicle;
import com.evion.evion_backend.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    public List<Vehicle> getVehiclesByUser(Long userId) {
        return vehicleRepository.findAllByUserId(userId);
    }

    public Vehicle updateVehicle(Long id, Vehicle updatedVehicle) {
        Vehicle vehicle = getVehicle(id);
        vehicle.setName(updatedVehicle.getName());
        vehicle.setModel(updatedVehicle.getModel());
        vehicle.setLicensePlate(updatedVehicle.getLicensePlate());
        vehicle.setBatteryCapacityKwh(updatedVehicle.getBatteryCapacityKwh());
        vehicle.setEvEmissionsKgPerKm(updatedVehicle.getEvEmissionsKgPerKm());
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
}
