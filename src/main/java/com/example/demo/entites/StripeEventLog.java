package com.example.demo.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "stripe_event_log", uniqueConstraints = {
        @UniqueConstraint(columnNames = "eventId")
})
@Entity
public class StripeEventLog extends BaseEntity {

    private String eventId;

    private String eventType;

    private LocalDateTime processedAt;

    public StripeEventLog(Object o, String eventId, String eventType, LocalDateTime processedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }
}
