package org.example.auctionbackend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_followed_lots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowedLot {

    @EmbeddedId
    private UserFollowedLotId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("lotId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id")
    private Lot lot;

    @Column(name = "followed_at", nullable = false)
    private java.time.LocalDateTime followedAt;

    @PrePersist
    public void prePersist() {
        if (followedAt == null) {
            followedAt = java.time.LocalDateTime.now();
        }
    }
}
