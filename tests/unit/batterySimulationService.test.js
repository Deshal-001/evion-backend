const batteryService = require("../../src/services/batterySimulationService");

describe("batterySimulationService", () => {
  test("selectConsumption prefers override when provided", () => {
    const value = batteryService.selectConsumption("ECO", 0.33, 0.15, 0.2, 0.25);
    expect(value).toBe(0.33);
  });

  test("computeUsableRangeKm returns Number.MAX_VALUE when inputs are incomplete", () => {
    const range = batteryService.computeUsableRangeKm(null, 80, 0.2, 0.1);
    expect(range).toBe(Number.MAX_VALUE);
  });

  test("simulateBattery flags insufficient range for long trip", () => {
    const response = batteryService.simulateBattery({
      batteryCapacityKwh: 40,
      currentChargePct: 20,
      consumptionPerKm: 0.25,
      tripDistanceKm: 200,
      drivingMode: "NORMAL"
    });

    expect(response.canCompleteTrip).toBe(false);
    expect(response.alertMessage).toMatch(/Insufficient range/i);
  });

  test("simulateBattery warns when charge is below 20 even if trip is feasible", () => {
    const response = batteryService.simulateBattery({
      batteryCapacityKwh: 60,
      currentChargePct: 19,
      consumptionPerKm: 0.15,
      tripDistanceKm: 20,
      drivingMode: "ECO"
    });

    expect(response.canCompleteTrip).toBe(true);
    expect(response.alertMessage).toMatch(/below 20/i);
  });
});
