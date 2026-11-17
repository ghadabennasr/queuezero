package com.queuezero.backend.simulation;

/**
 * Parameters of one location's simulation (mirrors the simulation_profiles table).
 * weekdayFactors: 7 values, index 0 = Monday. hourlyFactors: 24 values, index = hour of day.
 */
public record SimulationProfile(
        int activeCounters,
        double avgServiceMinutes,
        double baseArrivalsPerHour,
        double[] weekdayFactors,
        double[] hourlyFactors,
        double noiseLevel) {

    public SimulationProfile {
        if (activeCounters < 1) {
            throw new IllegalArgumentException("activeCounters must be >= 1");
        }
        if (avgServiceMinutes <= 0) {
            throw new IllegalArgumentException("avgServiceMinutes must be > 0");
        }
        if (baseArrivalsPerHour < 0) {
            throw new IllegalArgumentException("baseArrivalsPerHour must be >= 0");
        }
        if (noiseLevel < 0) {
            throw new IllegalArgumentException("noiseLevel must be >= 0");
        }
        if (weekdayFactors == null || weekdayFactors.length != 7) {
            throw new IllegalArgumentException("weekdayFactors must contain exactly 7 values");
        }
        if (hourlyFactors == null || hourlyFactors.length != 24) {
            throw new IllegalArgumentException("hourlyFactors must contain exactly 24 values");
        }
        weekdayFactors = weekdayFactors.clone();
        hourlyFactors = hourlyFactors.clone();
        for (double f : weekdayFactors) {
            if (f < 0) {
                throw new IllegalArgumentException("weekday factors must be >= 0");
            }
        }
        for (double f : hourlyFactors) {
            if (f < 0) {
                throw new IllegalArgumentException("hourly factors must be >= 0");
            }
        }
    }

    /** Expected number of arrivals per hour, before random noise. */
    public double expectedArrivalsPerHour(int weekdayIndex, int hour) {
        return baseArrivalsPerHour * weekdayFactors[weekdayIndex] * hourlyFactors[hour];
    }
}
