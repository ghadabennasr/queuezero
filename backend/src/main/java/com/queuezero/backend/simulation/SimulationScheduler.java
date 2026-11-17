package com.queuezero.backend.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Triggers the simulation: once at startup (catch-up) and then every 5 minutes. */
@Component
@ConditionalOnProperty(name = "queuezero.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SimulationScheduler.class);

    private final SimulationService service;

    public SimulationScheduler(SimulationService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Startup catch-up: filling missing queue snapshots");
        run();
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void tick() {
        run();
    }

    private void run() {
        try {
            service.catchUp();
        } catch (RuntimeException e) {
            // Never let an exception kill the scheduled job; the next tick will retry.
            log.error("Simulation run failed", e);
        }
    }
}
