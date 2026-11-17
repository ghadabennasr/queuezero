package com.queuezero.backend.simulation;

import java.util.random.RandomGenerator;

/**
 * Pure queue simulation: no database, no Spring. Given the queue length at the start of a
 * time step, computes the queue length at the end of it.
 */
public final class QueueSimulator {

    /** Safety cap so a badly configured profile can never create an absurd queue. */
    public static final int MAX_QUEUE = 300;

    private QueueSimulator() {
    }

    public static StepResult step(SimulationProfile profile, int queueBefore, int weekdayIndex,
                                  int hour, boolean open, int stepMinutes, RandomGenerator rng) {
        if (queueBefore < 0) {
            throw new IllegalArgumentException("queueBefore must be >= 0");
        }
        if (weekdayIndex < 0 || weekdayIndex > 6) {
            throw new IllegalArgumentException("weekdayIndex must be 0..6");
        }
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("hour must be 0..23");
        }
        if (stepMinutes <= 0) {
            throw new IllegalArgumentException("stepMinutes must be > 0");
        }

        // 1) Arrivals: Poisson with a noisy rate. Nobody arrives when the place is closed.
        int arrivals = 0;
        if (open) {
            double jitter = Math.max(0.0, 1.0 + profile.noiseLevel() * rng.nextGaussian());
            double lambda = profile.expectedArrivalsPerHour(weekdayIndex, hour)
                    * jitter * stepMinutes / 60.0;
            arrivals = poisson(lambda, rng);
        }

        // 2) Capacity: how many people the counters can serve during this step.
        double expectedCapacity = profile.activeCounters() * (double) stepMinutes
                / profile.avgServiceMinutes();
        double capacityJitter = Math.max(0.0, 1.0 + 0.5 * profile.noiseLevel() * rng.nextGaussian());
        int capacity = stochasticRound(expectedCapacity * capacityJitter, rng);

        // 3) New queue, bounded between 0 and MAX_QUEUE.
        int waiting = queueBefore + arrivals;
        int served = Math.min(waiting, capacity);
        int queueAfter = Math.min(MAX_QUEUE, waiting - served);
        return new StepResult(queueAfter, arrivals, served);
    }

    /** Basic estimate: waiting time = people x average service duration / active counters. */
    public static double estimateWaitMinutes(int peopleWaiting, int activeCounters, double avgServiceMinutes) {
        if (activeCounters < 1) {
            throw new IllegalArgumentException("activeCounters must be >= 1");
        }
        if (peopleWaiting < 0 || avgServiceMinutes <= 0) {
            throw new IllegalArgumentException("invalid input");
        }
        return peopleWaiting * avgServiceMinutes / activeCounters;
    }

    // Knuth's algorithm for small lambda, normal approximation for large lambda.
    static int poisson(double lambda, RandomGenerator rng) {
        if (lambda <= 0) {
            return 0;
        }
        if (lambda >= 30) {
            long n = Math.round(lambda + Math.sqrt(lambda) * rng.nextGaussian());
            return (int) Math.max(0, n);
        }
        double limit = Math.exp(-lambda);
        int k = 0;
        double p = 1.0;
        do {
            k++;
            p *= rng.nextDouble();
        } while (p > limit);
        return k - 1;
    }

    // 3.3 becomes 3 (70% of the time) or 4 (30% of the time), so the average stays 3.3.
    static int stochasticRound(double value, RandomGenerator rng) {
        int whole = (int) Math.floor(value);
        double fraction = value - whole;
        return rng.nextDouble() < fraction ? whole + 1 : whole;
    }
}
