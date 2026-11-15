
package com.freightfox.transporter.service;

import com.freightfox.transporter.dto.request.AssignmentRequest;
import com.freightfox.transporter.dto.response.AssignmentResponse;
import com.freightfox.transporter.dto.response.LaneAssignment;
import com.freightfox.transporter.entity.Lane;
import com.freightfox.transporter.entity.LaneQuote;
import com.freightfox.transporter.entity.Transporter;
import com.freightfox.transporter.repository.LaneQuoteRepository;
import com.freightfox.transporter.repository.LaneRepository;
import com.freightfox.transporter.repository.TransporterRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssignmentService {

    private final LaneRepository laneRepository;
    private final TransporterRepository transporterRepository;
    private final LaneQuoteRepository laneQuoteRepository;

    public AssignmentService(
            LaneRepository laneRepository,
            TransporterRepository transporterRepository,
            LaneQuoteRepository laneQuoteRepository) {

        this.laneRepository = laneRepository;
        this.transporterRepository = transporterRepository;
        this.laneQuoteRepository = laneQuoteRepository;
    }

    @Transactional(readOnly = true)
    public AssignmentResponse assignTransporters(AssignmentRequest request) {

        // 1. Validate request
        if (request == null || request.getMaxTransporters() == null) {
            throw new IllegalArgumentException(
                    "maxTransporters is required."
            );
        }

        int maxTransporters = request.getMaxTransporters();

        if (maxTransporters <= 0) {
            throw new IllegalArgumentException(
                    "maxTransporters must be greater than zero."
            );
        }

        // 2. Fetch data from database
        List<Lane> lanes = new ArrayList<>(laneRepository.findAll());
        List<Transporter> transporters = new ArrayList<>(transporterRepository.findAll());
        List<LaneQuote> quotes = laneQuoteRepository.findAll();

        if (lanes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No lanes found. Please submit input data first."
            );
        }

        if (transporters.isEmpty()) {
            throw new IllegalArgumentException(
                    "No transporters found. Please submit input data first."
            );
        }

        // 3. Sort lanes and transporters by ID
        lanes.sort(Comparator.comparing(Lane::getId));
        transporters.sort(Comparator.comparing(Transporter::getId));

        // 4. Limit transporter count for bitmask DP
        int transporterCount = transporters.size();

        if (transporterCount > 20) {
            throw new IllegalArgumentException(
                    "The current optimization algorithm supports at most 20 transporters."
            );
        }

        // 5. Create transporter ID -> index mapping
        Map<Long, Integer> transporterIndex = new HashMap<>();

        for (int i = 0; i < transporterCount; i++) {
            transporterIndex.put(transporters.get(i).getId(), i);
        }

        // 6. Store quotes as:
        // laneId -> transporterId -> quote
        Map<Long, Map<Long, BigDecimal>> quoteMap = new HashMap<>();

        for (LaneQuote laneQuote : quotes) {

            Long laneId = laneQuote.getLane().getId();
            Long transporterId = laneQuote.getTransporter().getId();
            BigDecimal quote = laneQuote.getQuote();

            quoteMap
                    .computeIfAbsent(laneId, key -> new HashMap<>())
                    .put(transporterId, quote);
        }

        // 7. DP initialization
        Map<Integer, State> dp = new HashMap<>();

        dp.put(0, new State(BigDecimal.ZERO, new ArrayList<>()));

        // 8. Process every lane
        for (Lane lane : lanes) {

            Map<Integer, State> nextDp = new HashMap<>();

            Map<Long, BigDecimal> laneQuotes =
                    quoteMap.getOrDefault(lane.getId(), new HashMap<>());

            for (Map.Entry<Integer, State> entry : dp.entrySet()) {

                int currentMask = entry.getKey();
                State currentState = entry.getValue();

                // Try assigning this lane to each transporter
                for (Map.Entry<Long, BigDecimal> quoteEntry : laneQuotes.entrySet()) {

                    Long transporterId = quoteEntry.getKey();
                    BigDecimal quote = quoteEntry.getValue();

                    Integer index = transporterIndex.get(transporterId);

                    // Ignore transporter if not present in current transporter list
                    if (index == null) {
                        continue;
                    }

                    int newMask = currentMask | (1 << index);

                    // Do not exceed maximum allowed transporters
                    if (Integer.bitCount(newMask) > maxTransporters) {
                        continue;
                    }

                    BigDecimal newCost =
                            currentState.totalCost.add(quote);

                    List<LaneAssignment> newAssignments =
                            new ArrayList<>(currentState.assignments);

                    newAssignments.add(
                            new LaneAssignment(lane.getId(), transporterId)
                    );

                    State existingState = nextDp.get(newMask);

                    // Keep the lowest-cost state for this transporter combination
                    if (existingState == null
                            || newCost.compareTo(existingState.totalCost) < 0) {

                        nextDp.put(
                                newMask,
                                new State(newCost, newAssignments)
                        );
                    }
                }
            }

            // No possible assignment for this lane
            if (nextDp.isEmpty()) {
                throw new IllegalArgumentException(
                        "No feasible assignment exists for lane ID: "
                                + lane.getId()
                );
            }

            dp = nextDp;
        }

        // 9. Select final state:
        // First maximize transporter usage up to K.
        // Then minimize total cost.
        State bestState = null;
        int bestTransporterCount = -1;
        int bestMask = 0;

        for (Map.Entry<Integer, State> entry : dp.entrySet()) {

            int mask = entry.getKey();
            State state = entry.getValue();

            int usedTransporterCount = Integer.bitCount(mask);

            if (usedTransporterCount > bestTransporterCount) {

                bestTransporterCount = usedTransporterCount;
                bestState = state;
                bestMask = mask;

            } else if (usedTransporterCount == bestTransporterCount
                    && (bestState == null
                    || state.totalCost.compareTo(bestState.totalCost) < 0)) {

                bestState = state;
                bestMask = mask;
            }
        }

        if (bestState == null) {
            throw new IllegalArgumentException(
                    "No feasible assignment could be found."
            );
        }

        // 10. Find selected transporter IDs
        List<Long> selectedTransporters = new ArrayList<>();

        for (int i = 0; i < transporterCount; i++) {

            if ((bestMask & (1 << i)) != 0) {
                selectedTransporters.add(transporters.get(i).getId());
            }
        }

        // 11. Return response
        return new AssignmentResponse(
                "success",
                bestState.totalCost,
                bestState.assignments,
                selectedTransporters
        );
    }

    // Represents one DP state
    private static class State {

        private final BigDecimal totalCost;
        private final List<LaneAssignment> assignments;

        public State(
                BigDecimal totalCost,
                List<LaneAssignment> assignments) {

            this.totalCost = totalCost;
            this.assignments = assignments;
        }
    }
}
