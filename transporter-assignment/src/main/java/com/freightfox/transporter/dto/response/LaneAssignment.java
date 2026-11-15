package com.freightfox.transporter.dto.response;

public class LaneAssignment {

    private Long laneId;
    private Long transporterId;

    public LaneAssignment() {
    }

    public LaneAssignment(Long laneId, Long transporterId) {
        this.laneId = laneId;
        this.transporterId = transporterId;
    }

    public Long getLaneId() {
        return laneId;
    }

    public void setLaneId(Long laneId) {
        this.laneId = laneId;
    }

    public Long getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(Long transporterId) {
        this.transporterId = transporterId;
    }
}