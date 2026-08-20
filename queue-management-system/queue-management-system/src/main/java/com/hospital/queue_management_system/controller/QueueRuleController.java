package com.hospital.queue_management_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.model.QueueRule;
import com.hospital.queue_management_system.service.QueueRuleService;

@RestController
@RequestMapping("/queue-rules")
@CrossOrigin(origins = "http://localhost:5173")
public class QueueRuleController {

    private final QueueRuleService service;

    public QueueRuleController(QueueRuleService service) {
        this.service = service;
    }

    @GetMapping
    public List<QueueRule> getAll() {
        return service.getAll();
    }

    @PutMapping("/{id}")
    public QueueRule update(
            @PathVariable Long id,
            @RequestBody QueueRule rule
    ) {
        rule.setRuleId(id);

        return service.save(rule);
    }
}