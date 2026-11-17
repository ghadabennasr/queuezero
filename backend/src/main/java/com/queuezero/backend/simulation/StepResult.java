package com.queuezero.backend.simulation;

/** Outcome of one simulation step. */
public record StepResult(int queueLength, int arrivals, int served) {
}
