
package com.freightfox.transporter.service;

import com.freightfox.transporter.dto.request.InputRequest;
import com.freightfox.transporter.dto.request.LaneQuoteRequest;
import com.freightfox.transporter.dto.request.LaneRequest;
import com.freightfox.transporter.dto.request.TransporterRequest;
import com.freightfox.transporter.entity.Lane;
import com.freightfox.transporter.entity.LaneQuote;
import com.freightfox.transporter.entity.Transporter;
import com.freightfox.transporter.repository.LaneQuoteRepository;
import com.freightfox.transporter.repository.LaneRepository;
import com.freightfox.transporter.repository.TransporterRepository;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InputService {

    private final LaneRepository laneRepository;
    private final TransporterRepository transporterRepository;
    private final LaneQuoteRepository laneQuoteRepository;
    private final EntityManager entityManager;

    public InputService(
            LaneRepository laneRepository,
            TransporterRepository transporterRepository,
            LaneQuoteRepository laneQuoteRepository,
            EntityManager entityManager) {

        this.laneRepository = laneRepository;
        this.transporterRepository = transporterRepository;
        this.laneQuoteRepository = laneQuoteRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public String saveInput(InputRequest request) {

        // 1. Validate request
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be empty.");
        }

        List<LaneRequest> laneRequests = request.getLanes();
        List<TransporterRequest> transporterRequests =
                request.getTransporters();

        if (laneRequests == null || laneRequests.isEmpty()) {
            throw new IllegalArgumentException("At least one lane is required.");
        }

        if (transporterRequests == null || transporterRequests.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one transporter is required.");
        }

        // 2. Delete old data in foreign-key order
        laneQuoteRepository.deleteAll();
        transporterRepository.deleteAll();
        laneRepository.deleteAll();

        // Execute deletions before inserting records with the same IDs
        entityManager.flush();

        // 3. Save lanes
        Map<Long, Lane> laneMap = new HashMap<>();

        for (LaneRequest laneRequest : laneRequests) {

            if (laneRequest.getId() == null) {
                throw new IllegalArgumentException("Lane ID is required.");
            }

            if (laneRequest.getOrigin() == null
                    || laneRequest.getDestination() == null) {
                throw new IllegalArgumentException(
                        "Lane origin and destination are required.");
            }

            if (laneMap.containsKey(laneRequest.getId())) {
                throw new IllegalArgumentException(
                        "Duplicate lane ID: " + laneRequest.getId());
            }

            Lane lane = new Lane(
                    laneRequest.getId(),
                    laneRequest.getOrigin(),
                    laneRequest.getDestination()
            );

            laneMap.put(laneRequest.getId(), lane);
        }

        for (Lane lane : laneMap.values()) {
            entityManager.persist(lane);
        }

        // 4. Save transporters and their quotes
        for (TransporterRequest transporterRequest : transporterRequests) {

            if (transporterRequest.getId() == null) {
                throw new IllegalArgumentException(
                        "Transporter ID is required.");
            }

            if (transporterRequest.getName() == null
                    || transporterRequest.getName().isBlank()) {
                throw new IllegalArgumentException(
                        "Transporter name is required.");
            }

            Transporter transporter = new Transporter(
                    transporterRequest.getId(),
                    transporterRequest.getName()
            );

            entityManager.persist(transporter);

            // 5. Save quotes for this transporter
            List<LaneQuoteRequest> quotes =
                    transporterRequest.getLaneQuotes();

            if (quotes == null) {
                continue;
            }

            for (LaneQuoteRequest quoteRequest : quotes) {

                if (quoteRequest.getLaneId() == null) {
                    throw new IllegalArgumentException(
                            "Quote lane ID is required.");
                }

                if (quoteRequest.getQuote() == null
                        || quoteRequest.getQuote().signum() < 0) {
                    throw new IllegalArgumentException(
                            "Quote must be a non-negative value.");
                }

                Lane lane = laneMap.get(quoteRequest.getLaneId());

                if (lane == null) {
                    throw new IllegalArgumentException(
                            "Lane not found: " + quoteRequest.getLaneId());
                }

                LaneQuote laneQuote = new LaneQuote();
                laneQuote.setLane(lane);
                laneQuote.setTransporter(transporter);
                laneQuote.setQuote(quoteRequest.getQuote());

                laneQuoteRepository.save(laneQuote);
            }
        }

        // 6. Flush pending database operations
        entityManager.flush();

        return "Input data saved successfully.";
    }
}
