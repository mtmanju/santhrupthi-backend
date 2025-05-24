package com.santhrupthi.controller;

import com.santhrupthi.model.Volunteer;
import com.santhrupthi.service.VolunteerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {
    private final VolunteerService service;

    public VolunteerController(VolunteerService service) {
        this.service = service;
    }

    @PostMapping
    public Volunteer createVolunteer(@RequestBody Volunteer volunteer) {
        return service.save(volunteer);
    }
} 