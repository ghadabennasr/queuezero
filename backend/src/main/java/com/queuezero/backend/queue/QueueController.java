package com.queuezero.backend.queue;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations/{id}/queue")
public class QueueController {

    private final QueueService service;

    public QueueController(QueueService service) {
        this.service = service;
    }

    @GetMapping("/current")
    public CurrentQueueResponse current(@PathVariable long id) {
        return service.current(id);
    }

    @GetMapping("/history")
    public HistoryResponse history(@PathVariable long id,
                                   @RequestParam(defaultValue = "24") int hours,
                                   @RequestParam(defaultValue = "15") int bucketMinutes) {
        return service.history(id, hours, bucketMinutes);
    }
}
