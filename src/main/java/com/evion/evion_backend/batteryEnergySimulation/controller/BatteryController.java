package com.evion.evion_backend.batteryEnergySimulation.controller;

import org.springframework.web.bind.annotation.*;

import com.evion.evion_backend.batteryEnergySimulation.dto.BatteryStatusRequest;
import com.evion.evion_backend.batteryEnergySimulation.dto.BatteryStatusResponse;
import com.evion.evion_backend.batteryEnergySimulation.service.BatterySimulationService;

@RestController
@RequestMapping("/api/battery")
public class BatteryController {

    private final BatterySimulationService batteryService;

    public BatteryController(BatterySimulationService batteryService) {
        this.batteryService = batteryService;
    }

    @PostMapping("/status")
    public BatteryStatusResponse getBatteryStatus(@RequestBody BatteryStatusRequest req) {
        return batteryService.simulateBattery(req);
    }
}
