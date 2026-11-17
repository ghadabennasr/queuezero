package com.queuezero.backend.queue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class QueueRepository {

    static final String DATA_SOURCE = "SIMULATED";
    static final String DISCLAIMER = "Simulated data. Waiting times are estimates, not guarantees.";

    private final JdbcTemplate jdbc;

    public QueueRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public boolean locationExists(long locationId) {
        Integer n = jdbc.queryForObject(
                "SELECT count(*) FROM service_locations WHERE id = ? AND is_active = true",
                Integer.class, locationId);
        return n != null && n > 0;
    }

    public Optional<CurrentQueueResponse> findLatest(long locationId) {
        List<CurrentQueueResponse> rows = jdbc.query("""
                SELECT captured_at, people_waiting, active_counters,
                       avg_service_minutes, estimated_wait_minutes, source
                FROM queue_snapshots
                WHERE location_id = ?
                ORDER BY captured_at DESC
                LIMIT 1
                """,
                (rs, rowNum) -> new CurrentQueueResponse(
                        locationId,
                        rs.getObject("captured_at", OffsetDateTime.class),
                        rs.getInt("people_waiting"),
                        rs.getInt("active_counters"),
                        round1(rs.getDouble("avg_service_minutes")),
                        round1(rs.getDouble("estimated_wait_minutes")),
                        rs.getInt("active_counters") > 0,
                        rs.getString("source"),
                        DISCLAIMER),
                locationId);
        return rows.stream().findFirst();
    }

    public List<HistoryPoint> history(long locationId, OffsetDateTime since, int bucketMinutes) {
        return jdbc.query("""
                SELECT date_bin(make_interval(mins => ?), captured_at,
                                TIMESTAMPTZ '2000-01-01 00:00:00+00') AS bucket,
                       avg(people_waiting)         AS avg_people,
                       avg(estimated_wait_minutes) AS avg_wait,
                       max(estimated_wait_minutes) AS max_wait
                FROM queue_snapshots
                WHERE location_id = ? AND captured_at >= ?
                GROUP BY bucket
                ORDER BY bucket
                """,
                (rs, rowNum) -> new HistoryPoint(
                        rs.getObject("bucket", OffsetDateTime.class),
                        round1(rs.getDouble("avg_people")),
                        round1(rs.getDouble("avg_wait")),
                        round1(rs.getDouble("max_wait"))),
                bucketMinutes, locationId, since);
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
