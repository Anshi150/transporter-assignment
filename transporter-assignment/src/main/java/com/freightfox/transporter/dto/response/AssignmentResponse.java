package com.freightfox.transporter.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class AssignmentResponse {

    private String status;
    private BigDecimal totalCost;
    private List<LaneAssignment> assignments;
    private List<Long> selectedTransporters;

    public AssignmentResponse() {
    }

    public AssignmentResponse(
            String status,
            BigDecimal totalCost,
            List<LaneAssignment> assignments,
            List<Long> selectedTransporters) {

        this.status = status;
        this.totalCost = totalCost;
        this.assignments = assignments;
        this.selectedTransporters = selectedTransporters;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public List<LaneAssignment> getAssignments() {
        return assignments;
    }

    public List<Long> getSelectedTransporters() {
        return selectedTransporters;
    }
}