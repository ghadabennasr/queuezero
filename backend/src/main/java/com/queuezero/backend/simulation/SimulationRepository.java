package com.queuezero.backend.simulation;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

@Repository
public class SimulationRepository {

    public record LocationSimulation(long locationId, SimulationProfile profile) {
    }

    public record OpeningWindow(boolean closed, LocalTime open, LocalTime close) {
    }

    public record LastSnapshot(OffsetDateTime capturedAt, int peopleWaiting) {
    }

    public record SnapshotRow(long locationId, OffsetDateTime capturedAt, int peopleWaiting,
                              int activeCounters, double avgServiceMinutes, double estimatedWaitMinutes) {
    }

    private static final String INSERT_SNAPSHOT = """
            INSERT INTO queue_snapshots
              (location_id, captured_at, people_waiting, active_counters,
               avg_service_minutes, estimated_wait_minutes, source)
            VALUES (?, ?, ?, ?, ?, ?, 'SIMULATED')
            ON CONFLICT (location_id, captured_at) DO NOTHING
            """;

    private final JdbcTemplate jdbc;

    public SimulationRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public List<LocationSimulation> findActiveProfiles() {
        return jdbc.query("""
                SELECT p.location_id, p.active_counters, p.avg_service_minutes,
                       p.base_arrivals_per_hour, p.weekday_factors, p.hourly_factors, p.noise_level
                FROM simulation_profiles p
                JOIN service_locations l ON l.id = p.location_id
                WHERE p.enabled = true AND l.is_active = true
                ORDER BY p.location_id
                """,
                (rs, rowNum) -> new LocationSimulation(
                        rs.getLong("location_id"),
                        new SimulationProfile(
                                rs.getInt("active_counters"),
                                rs.getDouble("avg_service_minutes"),
                                rs.getDouble("base_arrivals_per_hour"),
                                toDoubles(rs.getArray("weekday_factors")),
                                toDoubles(rs.getArray("hourly_factors")),
                                rs.getDouble("noise_level"))));
    }

    public Map<Long, Map<Integer, OpeningWindow>> findOpeningHours() {
        Map<Long, Map<Integer, OpeningWindow>> result = new HashMap<>();
        jdbc.query("SELECT location_id, weekday, open_time, close_time, is_closed FROM opening_hours",
                (RowCallbackHandler) rs -> result
                        .computeIfAbsent(rs.getLong("location_id"), k -> new HashMap<>())
                        .put(rs.getInt("weekday"), new OpeningWindow(
                                rs.getBoolean("is_closed"),
                                rs.getObject("open_time", LocalTime.class),
                                rs.getObject("close_time", LocalTime.class))));
        return result;
    }

    public Optional<LastSnapshot> findLastSnapshot(long locationId) {
        List<LastSnapshot> rows = jdbc.query("""
                SELECT captured_at, people_waiting
                FROM queue_snapshots
                WHERE location_id = ?
                ORDER BY captured_at DESC
                LIMIT 1
                """,
                (rs, rowNum) -> new LastSnapshot(
                        rs.getObject("captured_at", OffsetDateTime.class),
                        rs.getInt("people_waiting")),
                locationId);
        return rows.stream().findFirst();
    }

    /** Returns how many rows were really inserted (rows that already existed are skipped). */
    public int insertSnapshots(List<SnapshotRow> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        int[] counts = jdbc.batchUpdate(INSERT_SNAPSHOT, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SnapshotRow r = rows.get(i);
                ps.setLong(1, r.locationId());
                ps.setObject(2, r.capturedAt());
                ps.setInt(3, r.peopleWaiting());
                ps.setInt(4, r.activeCounters());
                ps.setFloat(5, (float) r.avgServiceMinutes());
                ps.setFloat(6, (float) r.estimatedWaitMinutes());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
        int inserted = 0;
        for (int c : counts) {
            if (c > 0) {
                inserted += c;
            }
        }
        return inserted;
    }

    private static double[] toDoubles(Array array) throws SQLException {
        Object[] values = (Object[]) array.getArray();
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = ((Number) values[i]).doubleValue();
        }
        return result;
    }
}
