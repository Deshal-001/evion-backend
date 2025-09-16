package com.evion.evion_backend.utils.helper;

public class EvionCalculations {

    // Average CO2 emissions per km for a typical petrol car (in kg)
    private static final double AVERAGE_PETROL_CAR_EMISSIONS_KG_PER_KM = 0.120; // 120 g/km
    // Average CO2 emissions per km for a typical electric vehicle (in kg)
    private static final double AVERAGE_EV_EMISSIONS_KG_PER_KM = 0.040;    // 40 g/km

    /**
     * Calculate CO2 saved by using eco-friendly transportation.
     *
     * @param distanceKm Distance traveled in kilometers.
     * @return CO2 saved in kilograms.
     */
    public static double calculateCo2Saved(double distanceKm) {
        return (AVERAGE_PETROL_CAR_EMISSIONS_KG_PER_KM - AVERAGE_EV_EMISSIONS_KG_PER_KM) * distanceKm;
    }

    /**
     * Calculate CO2 emitted by an electric vehicle trip.
     *
     * @param distanceKm Distance traveled in kilometers.
     * @param evEmissionsKgPerKm Vehicle-specific EV emissions factor (kg/km)
     * @return CO2 emitted in kilograms.
     */
    public static double calculateEvCo2(double distanceKm, double evEmissionsKgPerKm) {
        double emissions = evEmissionsKgPerKm > 0 ? evEmissionsKgPerKm : AVERAGE_EV_EMISSIONS_KG_PER_KM;
        return distanceKm * emissions;
    }

    /**
     * Calculate energy used for a trip.
     *
     * @param distanceKm Distance traveled in kilometers.
     * @param consumptionPerKm Vehicle consumption in kWh/km
     * @return Energy used in kWh
     */
    public static double calculateEnergyUsed(double distanceKm, double consumptionPerKm) {
        return distanceKm * consumptionPerKm;
    }

}
