package com.queuezero.backend.location;

import java.time.LocalTime;

public record OpeningHoursResponse(int weekday, boolean closed, LocalTime openTime, LocalTime closeTime) {

    static OpeningHoursResponse from(OpeningHours h) {
        return new OpeningHoursResponse(h.getWeekday(), h.isClosed(), h.getOpenTime(), h.getCloseTime());
    }
}
