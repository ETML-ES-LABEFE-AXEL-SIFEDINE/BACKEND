package org.example.auctionbackend.repository;

import org.example.auctionbackend.model.UserFollowedLot;
import org.example.auctionbackend.model.UserFollowedLotId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface UserFollowedLotRepository extends JpaRepository<UserFollowedLot, UserFollowedLotId> {
    boolean existsById(UserFollowedLotId id);

    List<UserFollowedLot> findByUserUsernameOrderByLotEndDateAscLotCurrentPriceDesc(String username);

}
