
package com.freightfox.transporter.controller;

import com.freightfox.transporter.dto.request.InputRequest;
import com.freightfox.transporter.dto.request.AssignmentRequest;
import com.freightfox.transporter.service.InputService;
import com.freightfox.transporter.service.AssignmentService;
import com.freightfox.transporter.service.LaneService;
import com.freightfox.transporter.service.TransporterService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transporters")
public class TransporterController {

    private final InputService inputService;
    private final AssignmentService assignmentService;
    private final LaneService laneService;
    private final TransporterService transporterService;

    public TransporterController(
            InputService inputService,
            AssignmentService assignmentService,
            LaneService laneService,
            TransporterService transporterService) {

        this.inputService = inputService;
        this.assignmentService = assignmentService;
        this.laneService = laneService;
        this.transporterService = transporterService;
    }

    // 1. Save lanes, transporters and quotes
    @PostMapping("/input")
    public ResponseEntity<Map<String, String>> saveInput(
            @RequestBody InputRequest request) {

        String message = inputService.saveInput(request);

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "message", message
                )
        );
    }

    // 2. Assign transporters to lanes
    @PostMapping("/assignment")
    public ResponseEntity<?> assignTransporters(
            @RequestBody AssignmentRequest request) {

        return ResponseEntity.ok(
                assignmentService.assignTransporters(request)
        );
    }

    // 3. Get all lanes
    @GetMapping("/lanes")
    public ResponseEntity<?> getAllLanes() {

        return ResponseEntity.ok(
                laneService.getAllLanes()
        );
    }

    // 4. Get all transporters
    @GetMapping
    public ResponseEntity<?> getAllTransporters() {

        return ResponseEntity.ok(
                transporterService.getAllTransporters()
        );
    }
}
