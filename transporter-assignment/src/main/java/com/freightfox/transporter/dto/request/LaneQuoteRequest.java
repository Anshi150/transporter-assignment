package com.freightfox.transporter.dto.request;

import java.math.BigDecimal;

public class LaneQuoteRequest {

    private Long laneId;
    private BigDecimal quote;

    public Long getLaneId() {
        return laneId;
    }

    public BigDecimal getQuote() {
        return quote;
    }

    public void setLaneId(Long laneId) {
        this.laneId = laneId;
    }

    public void setQuote(BigDecimal quote) {
        this.quote = quote;
    }
}