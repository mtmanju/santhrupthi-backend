package com.santhrupthi.service;

import com.santhrupthi.model.Volunteer;
import com.santhrupthi.repository.VolunteerRepository;
import org.springframework.stereotype.Service;

@Service
public class VolunteerService {
    private final VolunteerRepository repo;

    public VolunteerService(VolunteerRepository repo) {
        this.repo = repo;
    }

    public Volunteer save(Volunteer volunteer) {
        return repo.save(volunteer);
    }
} 