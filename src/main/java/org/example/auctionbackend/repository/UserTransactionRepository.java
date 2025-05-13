package org.example.auctionbackend.repository;

import org.example.auctionbackend.model.UserTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserTransactionRepository extends JpaRepository<UserTransaction, Long> {
    List<UserTransaction> findByUserUsernameOrderByTimestampDesc(String username);

}
