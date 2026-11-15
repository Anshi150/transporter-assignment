package com.freightfox.transporter.dto.request;

import java.util.List;

public class InputRequest {

    private List<LaneRequest> lanes;
    private List<TransporterRequest> transporters;

    public List<LaneRequest> getLanes() {
        return lanes;
    }

    public List<TransporterRequest> getTransporters() {
        return transporters;
    }

    public void setLanes(List<LaneRequest> lanes) {
        this.lanes = lanes;
    }

    public void setTransporters(List<TransporterRequest> transporters) {
        this.transporters = transporters;
    }
}