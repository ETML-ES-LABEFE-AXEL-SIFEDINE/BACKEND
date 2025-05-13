package org.example.auctionbackend.service;

import org.example.auctionbackend.dto.TransactionDTO;
import org.example.auctionbackend.repository.UserTransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final UserTransactionRepository txRepo;

    public TransactionServiceImpl(UserTransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    @Override
    public List<TransactionDTO> getHistory(String username) {
        return txRepo.findByUserUsernameOrderByTimestampDesc(username).stream()
                .map(tx -> new TransactionDTO(tx.getAmount(), tx.getTimestamp()))
                .collect(Collectors.toList());
    }
}
