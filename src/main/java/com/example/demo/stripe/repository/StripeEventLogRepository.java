package com.example.demo.stripe.repository;

import com.example.demo.entites.StripeEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeEventLogRepository extends JpaRepository<StripeEventLog, Long> {
    boolean existsByEventId(String eventId);
}
