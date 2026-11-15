package com.freightfox.transporter.repository;

import com.freightfox.transporter.entity.Lane;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaneRepository extends JpaRepository<Lane, Long> {
}