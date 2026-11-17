package com.queuezero.backend.queue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QueueService {

    private static final int MAX_HOURS = 168; // 7 days

    private final QueueRepository repository;

    public QueueService(QueueRepository repository) {
        this.repository = repository;
    }

    public CurrentQueueResponse current(long locationId) {
        requireLocation(locationId);
        return repository.findLatest(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No queue data yet for this location"));
    }

    public HistoryResponse history(long locationId, int hours, int bucketMinutes) {
        if (hours < 1 || hours > MAX_HOURS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "hours must be between 1 and " + MAX_HOURS);
        }
        if (bucketMinutes < 5 || bucketMinutes > 240 || bucketMinutes % 5 != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "bucketMinutes must be a multiple of 5 between 5 and 240");
        }
        requireLocation(locationId);

        OffsetDateTime since = OffsetDateTime.now(ZoneOffset.UTC).minusHours(hours);
        List<HistoryPoint> points = repository.history(locationId, since, bucketMinutes);
        return new HistoryResponse(locationId, hours, bucketMinutes,
                QueueRepository.DATA_SOURCE, QueueRepository.DISCLAIMER, points);
    }

    private void requireLocation(long locationId) {
        if (!repository.locationExists(locationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found");
        }
    }
}
