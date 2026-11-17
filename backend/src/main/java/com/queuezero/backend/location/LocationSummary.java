package com.queuezero.backend.location;

public record LocationSummary(
        Long id,
        String name,
        String address,
        String governorate,
        double latitude,
        double longitude,
        String categoryCode,
        String categoryName) {

    static LocationSummary from(ServiceLocation l) {
        return new LocationSummary(
                l.getId(), l.getName(), l.getAddress(), l.getGovernorate(),
                l.getLatitude(), l.getLongitude(),
                l.getCategory().getCode(), l.getCategory().getName());
    }
}
