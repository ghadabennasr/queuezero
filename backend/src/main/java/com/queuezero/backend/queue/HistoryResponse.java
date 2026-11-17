package com.queuezero.backend.queue;

import java.util.List;

public record HistoryResponse(
        long locationId,
        int hours,
        int bucketMinutes,
        String dataSource,
        String disclaimer,
        List<HistoryPoint> points) {
}
