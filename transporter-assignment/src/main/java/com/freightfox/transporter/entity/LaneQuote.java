package com.freightfox.transporter.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "lane_quotes",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"lane_id", "transporter_id"}
                )
        }
)
public class LaneQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lane_id", nullable = false)
    private Lane lane;

    @ManyToOne
    @JoinColumn(name = "transporter_id", nullable = false)
    private Transporter transporter;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quote;

    public LaneQuote() {
    }

    public LaneQuote(Lane lane, Transporter transporter,
                     BigDecimal quote) {
        this.lane = lane;
        this.transporter = transporter;
        this.quote = quote;
    }

    public Long getId() {
        return id;
    }

    public Lane getLane() {
        return lane;
    }

    public void setLane(Lane lane) {
        this.lane = lane;
    }

    public Transporter getTransporter() {
        return transporter;
    }

    public void setTransporter(Transporter transporter) {
        this.transporter = transporter;
    }

    public BigDecimal getQuote() {
        return quote;
    }

    public void setQuote(BigDecimal quote) {
        this.quote = quote;
    }
}