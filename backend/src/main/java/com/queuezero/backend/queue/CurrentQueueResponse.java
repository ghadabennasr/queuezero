package com.queuezero.backend.queue;

import java.time.OffsetDateTime;

public record CurrentQueueResponse(
        long locationId,
        OffsetDateTime capturedAt,
        int peopleWaiting,
        int activeCounters,
        double avgServiceMinutes,
        double estimatedWaitMinutes,
        boolean open,
        String dataSource,
        String disclaimer) {
}
