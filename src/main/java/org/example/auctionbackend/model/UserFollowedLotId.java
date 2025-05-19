package org.example.auctionbackend.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowedLotId implements Serializable {
    private Long userId;
    private Long lotId;
}
