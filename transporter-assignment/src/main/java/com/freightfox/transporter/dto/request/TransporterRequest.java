package com.freightfox.transporter.dto.request;

import java.util.List;

public class TransporterRequest {

    private Long id;
    private String name;
    private List<LaneQuoteRequest> laneQuotes;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<LaneQuoteRequest> getLaneQuotes() {
        return laneQuotes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLaneQuotes(List<LaneQuoteRequest> laneQuotes) {
        this.laneQuotes = laneQuotes;
    }
}