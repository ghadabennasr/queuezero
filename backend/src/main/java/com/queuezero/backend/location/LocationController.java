package com.queuezero.backend.location;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    public List<LocationSummary> list(@RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) String governorate,
                                      @RequestParam(required = false) String q) {
        return service.search(categoryId, governorate, q);
    }

    @GetMapping("/{id}")
    public LocationDetail get(@PathVariable Long id) {
        return service.getById(id);
    }
}
