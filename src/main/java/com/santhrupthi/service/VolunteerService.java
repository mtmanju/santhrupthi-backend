package com.santhrupthi.service;

import com.santhrupthi.model.Volunteer;
import com.santhrupthi.repository.VolunteerRepository;
import org.springframework.stereotype.Service;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
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