package com.queuezero.backend.location;

import java.util.List;

public record LocationDetail(
        Long id,
        String name,
        String address,
        String governorate,
        double latitude,
        double longitude,
        String categoryCode,
        String categoryName,
        List<OpeningHoursResponse> openingHours) {
}
