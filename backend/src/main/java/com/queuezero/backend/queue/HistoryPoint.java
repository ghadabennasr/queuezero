package com.queuezero.backend.queue;

import java.time.OffsetDateTime;

public record HistoryPoint(
        OffsetDateTime time,
        double avgPeopleWaiting,
        double avgWaitMinutes,
        double maxWaitMinutes) {
}
