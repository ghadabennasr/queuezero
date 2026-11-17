package com.queuezero.backend.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class QueueSimulatorTest {

    private static SimulationProfile office() {
        double[] weekdays = {1.3, 1.0, 0.9, 0.9, 1.1, 0.4, 0.0};
        double[] hours = {0, 0, 0, 0, 0, 0, 0, 0, 0.6, 1.0, 1.4, 1.3, 0.8, 0.7, 1.1, 0.9, 0.6, 0.2, 0, 0, 0, 0, 0, 0};
        return new SimulationProfile(3, 4.5, 25.0, weekdays, hours, 0.15);
    }

    @Test
    void queueStaysWithinBounds() {
        Random rng = new Random(1);
        SimulationProfile profile = office();
        int queue = 0;
        for (int i = 0; i < 20000; i++) {
            StepResult r = QueueSimulator.step(profile, queue, (i / 288) % 7, (i / 12) % 24, true, 5, rng);
            queue = r.queueLength();
            assertTrue(queue >= 0 && queue <= QueueSimulator.MAX_QUEUE, "queue out of bounds: " + queue);
        }
    }

    @Test
    void closedMeansNoArrivalsAndQueueDrains() {
        Random rng = new Random(2);
        SimulationProfile profile = office();
        int queue = 20;
        for (int i = 0; i < 30; i++) {
            StepResult r = QueueSimulator.step(profile, queue, 0, 18, false, 5, rng);
            assertEquals(0, r.arrivals());
            assertTrue(r.queueLength() <= queue);
            queue = r.queueLength();
        }
        assertEquals(0, queue);
    }

    @Test
    void sameSeedGivesSameSequence() {
        List<Integer> first = run(42);
        List<Integer> second = run(42);
        assertEquals(first, second);
    }

    private List<Integer> run(long seed) {
        Random rng = new Random(seed);
        SimulationProfile profile = office();
        List<Integer> queues = new ArrayList<>();
        int queue = 0;
        for (int i = 0; i < 200; i++) {
            queue = QueueSimulator.step(profile, queue, 0, 8 + (i / 12) % 10, true, 5, rng).queueLength();
            queues.add(queue);
        }
        return queues;
    }

    @Test
    void averageArrivalsMatchExpectation() {
        Random rng = new Random(3);
        SimulationProfile profile = office();
        // Monday 10:00 -> 25 * 1.3 * 1.4 = 45.5 per hour -> about 3.79 per 5 minutes
        double expected = profile.expectedArrivalsPerHour(0, 10) * 5 / 60.0;
        long total = 0;
        int steps = 20000;
        for (int i = 0; i < steps; i++) {
            total += QueueSimulator.step(profile, 0, 0, 10, true, 5, rng).arrivals();
        }
        assertEquals(expected, (double) total / steps, 0.1);
    }

    @Test
    void waitFormulaMatchesBlueprint() {
        assertEquals(15.0, QueueSimulator.estimateWaitMinutes(10, 3, 4.5), 1e-9);
        assertEquals(0.0, QueueSimulator.estimateWaitMinutes(0, 3, 4.5), 1e-9);
    }

    @Test
    void invalidProfileIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new SimulationProfile(3, 4.5, 25.0, new double[6], new double[24], 0.15));
        assertThrows(IllegalArgumentException.class,
                () -> new SimulationProfile(0, 4.5, 25.0, new double[7], new double[24], 0.15));
    }
}
