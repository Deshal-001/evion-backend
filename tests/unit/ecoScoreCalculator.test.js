const { calculateEcoScore } = require("../../src/utils/ecoScoreCalculator");

describe("ecoScoreCalculator", () => {
  test("returns fallback score when invalid inputs are provided", () => {
    const score = calculateEcoScore(0, 0, 0, "ECO", null, null);
    expect(score).toBe(50);
  });

  test("gives higher score for ECO than AGGRESSIVE for same trip", () => {
    const ecoScore = calculateEcoScore(50, 8, 3600, "ECO", 80, 60);
    const aggressiveScore = calculateEcoScore(50, 8, 3600, "AGGRESSIVE", 80, 60);
    expect(ecoScore).toBeGreaterThan(aggressiveScore);
  });

  test("always clamps score to 0..100", () => {
    const score = calculateEcoScore(100, 0.1, 3600, "ECO", 100, 99);
    expect(score).toBeGreaterThanOrEqual(0);
    expect(score).toBeLessThanOrEqual(100);
  });
});
