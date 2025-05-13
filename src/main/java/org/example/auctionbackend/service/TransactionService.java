package org.example.auctionbackend.service;

import org.example.auctionbackend.dto.TransactionDTO;
import java.util.List;

public interface TransactionService {
    List<TransactionDTO> getHistory(String username);
}
