package com.freightfox.transporter.repository;

import com.freightfox.transporter.entity.LaneQuote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaneQuoteRepository
        extends JpaRepository<LaneQuote, Long> {
}