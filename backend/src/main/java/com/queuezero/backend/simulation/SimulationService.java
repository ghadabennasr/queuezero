package com.queuezero.backend.simulation;

import com.queuezero.backend.simulation.SimulationRepository.LastSnapshot;
import com.queuezero.backend.simulation.SimulationRepository.LocationSimulation;
import com.queuezero.backend.simulation.SimulationRepository.OpeningWindow;
import com.queuezero.backend.simulation.SimulationRepository.SnapshotRow;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Generates queue snapshots on a fixed 5-minute grid and stores them as SIMULATED data.
 * Safe to call repeatedly: it continues from the last stored snapshot, and the database
 * ignores any slot that already exists.
 */
@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);
    private static final int STEP_MINUTES = 5;
    private static final long STEP_SECONDS = STEP_MINUTES * 60L;

    private final SimulationRepository repository;
    private final ZoneId zone;
    private final int backfillDays;
    private final Random rng = new Random();
    private final ReentrantLock lock = new ReentrantLock();

    public SimulationService(SimulationRepository repository,
                             @Value("${queuezero.simulation.zone:Africa/Tunis}") String zoneId,
                             @Value("${queuezero.simulation.backfill-days:14}") int backfillDays) {
        this.repository = repository;
        this.zone = ZoneId.of(zoneId);
        this.backfillDays = backfillDays;
    }

    /** Fills every missing 5-minute slot up to "now". Returns the number of rows inserted. */
    public int catchUp() {
        lock.lock();
        try {
            return catchUp(Instant.now());
        } finally {
            lock.unlock();
        }
    }

    int catchUp(Instant now) {
        Instant newest = Instant.ofEpochSecond(now.getEpochSecond() / STEP_SECONDS * STEP_SECONDS);
        Instant earliest = newest.minus(Duration.ofDays(backfillDays));
        Map<Long, Map<Integer, OpeningWindow>> openingHours = repository.findOpeningHours();

        int total = 0;
        for (LocationSimulation location : repository.findActiveProfiles()) {
            try {
                Map<Integer, OpeningWindow> hours = openingHours.getOrDefault(location.locationId(), Map.of());
                total += simulateLocation(location, hours, earliest, newest);
            } catch (RuntimeException e) {
                // One broken location must never stop the others.
                log.error("Simulation failed for location {}", location.locationId(), e);
            }
        }
        log.info("Simulation catch-up finished: {} snapshots inserted", total);
        return total;
    }

    private int simulateLocation(LocationSimulation location, Map<Integer, OpeningWindow> hours,
                                 Instant earliest, Instant newest) {
        SimulationProfile profile = location.profile();

        Optional<LastSnapshot> last = repository.findLastSnapshot(location.locationId());
        Instant start;
        int queue;
        if (last.isPresent() && !last.get().capturedAt().toInstant().isBefore(earliest)) {
            start = last.get().capturedAt().toInstant().plusSeconds(STEP_SECONDS);
            queue = last.get().peopleWaiting();
        } else {
            // No history (or too old): start from an empty queue at the beginning of the window.
            start = earliest;
            queue = 0;
        }

        List<SnapshotRow> rows = new ArrayList<>();
        for (Instant t = start; !t.isAfter(newest); t = t.plusSeconds(STEP_SECONDS)) {
            ZonedDateTime local = t.atZone(zone);
            int weekday = local.getDayOfWeek().getValue() - 1; // Monday = 0
            boolean open = isOpen(hours.get(weekday), local.toLocalTime());

            int counters;
            double wait;
            if (open) {
                queue = QueueSimulator.step(profile, queue, weekday, local.getHour(), true,
                        STEP_MINUTES, rng).queueLength();
                counters = profile.activeCounters();
                wait = QueueSimulator.estimateWaitMinutes(queue, counters, profile.avgServiceMinutes());
            } else {
                // Closed: nobody waits, no counters are staffed.
                queue = 0;
                counters = 0;
                wait = 0.0;
            }
            rows.add(new SnapshotRow(location.locationId(),
                    OffsetDateTime.ofInstant(t, ZoneOffset.UTC),
                    queue, counters, profile.avgServiceMinutes(), wait));
        }
        return repository.insertSnapshots(rows);
    }

    static boolean isOpen(OpeningWindow window, LocalTime time) {
        return window != null
                && !window.closed()
                && window.open() != null
                && window.close() != null
                && !time.isBefore(window.open())
                && time.isBefore(window.close());
    }
}
