const AVERAGE_PETROL_CAR_EMISSIONS_KG_PER_KM = 0.12;
const AVERAGE_EV_EMISSIONS_KG_PER_KM = 0.04;

function calculateCo2Saved(distanceKm) {
  return (
    (AVERAGE_PETROL_CAR_EMISSIONS_KG_PER_KM - AVERAGE_EV_EMISSIONS_KG_PER_KM) *
    distanceKm
  );
}

function calculateEvCo2(distanceKm, evEmissionsKgPerKm) {
  const emissions =
    evEmissionsKgPerKm && evEmissionsKgPerKm > 0
      ? evEmissionsKgPerKm
      : AVERAGE_EV_EMISSIONS_KG_PER_KM;
  return distanceKm * emissions;
}

function calculateEnergyUsed(distanceKm, consumptionPerKm) {
  return distanceKm * consumptionPerKm;
}

module.exports = {
  calculateCo2Saved,
  calculateEvCo2,
  calculateEnergyUsed
};
