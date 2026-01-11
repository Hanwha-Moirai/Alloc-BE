package com.moirai.alloc.calendar.command.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "public_events_member",
        indexes = {
                @Index(name = "idx_pem_event", columnList = "event_id"),
                @Index(name = "idx_pem_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_user", columnNames = {"event_id", "user_id"})
        }
)
public class PublicEventsMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "public_event_id")
    private Long id;

    /** FK: events.event_id */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /** FK: employee.user_id */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
    private PublicEventsMember(Long eventId, Long userId) {
        this.eventId = eventId;
        this.userId = userId;
    }
}