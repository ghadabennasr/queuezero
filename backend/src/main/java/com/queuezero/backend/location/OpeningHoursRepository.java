package com.queuezero.backend.location;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpeningHoursRepository extends JpaRepository<OpeningHours, Long> {

    List<OpeningHours> findByLocationIdOrderByWeekdayAsc(Long locationId);
}
