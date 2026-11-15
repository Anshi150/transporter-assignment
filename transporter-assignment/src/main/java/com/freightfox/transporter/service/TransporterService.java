package com.freightfox.transporter.service;

import com.freightfox.transporter.repository.TransporterRepository;
import org.springframework.stereotype.Service;

@Service
public class TransporterService {

    private final TransporterRepository transporterRepository;

    public TransporterService(
            TransporterRepository transporterRepository) {
        this.transporterRepository = transporterRepository;
    }

    public Object getAllTransporters() {
        return transporterRepository.findAll();
    }
}