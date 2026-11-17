package com.queuezero.backend.location;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LocationService {

    private final ServiceLocationRepository locations;
    private final OpeningHoursRepository hours;

    public LocationService(ServiceLocationRepository locations, OpeningHoursRepository hours) {
        this.locations = locations;
        this.hours = hours;
    }

    @Transactional(readOnly = true)
    public List<LocationSummary> search(Long categoryId, String governorate, String q) {
        long category = categoryId == null ? 0L : categoryId;
        String gov = governorate == null ? "" : governorate.trim();
        String text = q == null ? "" : q.trim();
        return locations.search(category, gov, text).stream()
                .map(LocationSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocationDetail getById(Long id) {
        ServiceLocation l = locations.findActiveById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
        List<OpeningHoursResponse> openingHours = hours.findByLocationIdOrderByWeekdayAsc(id).stream()
                .map(OpeningHoursResponse::from)
                .toList();
        return new LocationDetail(
                l.getId(), l.getName(), l.getAddress(), l.getGovernorate(),
                l.getLatitude(), l.getLongitude(),
                l.getCategory().getCode(), l.getCategory().getName(),
                openingHours);
    }
}
