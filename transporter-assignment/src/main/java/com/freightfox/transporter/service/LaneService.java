package com.freightfox.transporter.service;

import com.freightfox.transporter.repository.LaneRepository;
import org.springframework.stereotype.Service;

@Service
public class LaneService {

    private final LaneRepository laneRepository;

    public LaneService(LaneRepository laneRepository) {
        this.laneRepository = laneRepository;
    }

    public Object getAllLanes() {
        return laneRepository.findAll();
    }
}