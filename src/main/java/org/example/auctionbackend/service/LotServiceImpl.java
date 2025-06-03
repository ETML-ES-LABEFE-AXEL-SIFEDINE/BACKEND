package org.example.auctionbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.auctionbackend.dto.CreateLotRequestDTO;
import org.example.auctionbackend.dto.LotDTO;
import org.example.auctionbackend.dto.LotDetailDTO;
import org.example.auctionbackend.dto.UpdateLotRequestDTO;
import org.example.auctionbackend.model.*;
import org.example.auctionbackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LotServiceImpl implements LotService {

    private final LotRepository lotRepository;
    private final CategoryRepository categoryRepository;
    private final UserFollowedLotRepository followedLotRepository;
    private final UserRepository userRepository;
    private final UserTransactionRepository transactionRepository;
    private final BidRepository bidRepository;

    @Override
    public Page<LotDTO> listByCategory(Long categoryId, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);

        Page<Lot> lotsPage = (categoryId == null)
                ? lotRepository.findAll(pr)
                : lotRepository.findByCategoryIdIn(collectCategoryIds(categoryId), pr);

        List<LotDTO> dtos = lotsPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pr, lotsPage.getTotalElements());
    }

    private List<Long> collectCategoryIds(Long categoryId) {
        List<Long> ids = categoryRepository.findByParentId(categoryId).stream()
                .map(Category::getId)
                .collect(Collectors.toList());
        ids.add(categoryId);
        return ids;
    }

    @Override
    public Optional<LotDetailDTO> findById(Long id) {
        return lotRepository.findById(id)
                .map(lot -> {
                    refreshLotStatus(lot);
                    return toDetailDTO(lot);
                });
    }

    @Override
    public List<LotDTO> findLatestLots(int count) {
        return lotRepository.findAllByOrderByIdDesc(PageRequest.of(0, count))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LotDTO> getFollowedLots(String username) {
        List<UserFollowedLot> suivis = followedLotRepository
                .findByUserUsernameOrderByLotEndDateAscLotCurrentPriceDesc(username);

        return suivis.stream()
                .map(ufl -> toDTO(ufl.getLot()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LotDTO> getUserLots(String username) {
        return lotRepository.findByOwnerUsername(username)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LotDTO createLot(CreateLotRequestDTO req) {
        String username = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found: " + req.getCategoryId())
                );

        Lot lot = Lot.builder()
                .owner(owner)
                .title(req.getTitle())
                .description(req.getDescription())
                .initialPrice(req.getInitialPrice())
                .currentPrice(req.getInitialPrice())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(LotStatus.PENDING)
                .category(category)
                .build();

        Lot saved = lotRepository.save(lot);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void cancelLot(String username, Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found: " + lotId));

        // Vérifier que c'est bien le propriétaire qui tente l'arrêt
        if (!lot.getOwner().getUsername().equals(username)) {
            throw new IllegalStateException("Not the owner of this lot");
        }

        // Mettre à jour le statut en fonction de la date courante
        refreshLotStatus(lot);
        if (lot.getStatus() != LotStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot cancel a lot that is not in progress");
        }

        // 1. Rembourser l’ancien leader (s'il existe)
        User prev = lot.getCurrentLeader();
        if (prev != null) {
            double prevAmt = lot.getCurrentPrice();
            prev.setBalance(prev.getBalance() + prevAmt);
            transactionRepository.save(UserTransaction.builder()
                    .user(prev)
                    .amount(prevAmt)
                    .type(TransactionType.BID_REFUND)
                    .build());
            userRepository.save(prev);
        }

        // 2. Supprimer toutes les enchères associées à ce lot
        bidRepository.deleteAllByLotId(lotId);

        // 3. Supprimer tous les suivis (user_followed_lots) pour ce lot
        followedLotRepository.deleteByLotId(lotId);

        // 4. Enfin, supprimer le lot lui‐même
        lotRepository.delete(lot);
    }

    @Override
    @Transactional
    public LotDTO updateLot(String username, Long lotId, UpdateLotRequestDTO req) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found: " + lotId));

        if (!lot.getOwner().getUsername().equals(username)) {
            throw new IllegalStateException("Not the owner of this lot");
        }

        refreshLotStatus(lot);
        if (!(lot.getStatus() == LotStatus.UNSOLD || lot.getStatus() == LotStatus.PENDING)) {
            throw new IllegalStateException("Only UNSOLD or PENDING lots can be updated");
        }

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.getCategoryId()));

        lot.setTitle(req.getTitle());
        lot.setDescription(req.getDescription());
        lot.setInitialPrice(req.getInitialPrice());
        lot.setCurrentPrice(req.getInitialPrice());
        lot.setStartDate(req.getStartDate());
        lot.setEndDate(req.getEndDate());
        lot.setStatus(LotStatus.PENDING);
        lot.setCategory(category);
        lot.setCurrentLeader(null);

        Lot updated = lotRepository.save(lot);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public LotDTO relistLot(String username, Long lotId, UpdateLotRequestDTO req) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found: " + lotId));

        if (!lot.getOwner().getUsername().equals(username)) {
            throw new IllegalStateException("Not the owner of this lot");
        }

        refreshLotStatus(lot);
        if (lot.getStatus() != LotStatus.UNSOLD) {
            throw new IllegalStateException("Only UNSOLD lots can be relisted");
        }

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.getCategoryId()));

        lot.setTitle(req.getTitle());
        lot.setDescription(req.getDescription());
        lot.setInitialPrice(req.getInitialPrice());
        lot.setCurrentPrice(req.getInitialPrice());
        lot.setStartDate(req.getStartDate());
        lot.setEndDate(req.getEndDate());
        lot.setStatus(LotStatus.PENDING);
        lot.setCategory(category);
        lot.setCurrentLeader(null);

        Lot relisted = lotRepository.save(lot);
        return toDTO(relisted);
    }

    @Transactional
    public void refreshLotStatus(Lot lot) {
        LocalDateTime now = LocalDateTime.now();
        LotStatus computed;
        if (now.isBefore(lot.getStartDate())) {
            computed = LotStatus.PENDING;
        } else if (now.isAfter(lot.getEndDate())) {
            computed = (lot.getCurrentLeader() != null)
                    ? LotStatus.SOLD
                    : LotStatus.UNSOLD;
        } else {
            computed = LotStatus.IN_PROGRESS;
        }

        if (lot.getStatus() != computed) {
            lot.setStatus(computed);
            lotRepository.save(lot);
        }
    }

    // === Mapping en LotDTO ===

    private LotDTO toDTO(Lot lot) {
        refreshLotStatus(lot);

        double current = (lot.getCurrentPrice() != null)
                ? lot.getCurrentPrice()
                : lot.getInitialPrice();

        return new LotDTO(
                lot.getId(),
                lot.getTitle(),
                lot.getDescription(),
                lot.getCategory().getId(),
                lot.getInitialPrice(),
                current,
                lot.getStatus().name()
        );
    }

    private LotDetailDTO toDetailDTO(Lot lot) {
        double current = (lot.getCurrentPrice() != null)
                ? lot.getCurrentPrice()
                : lot.getInitialPrice();

        String leader = (lot.getCurrentLeader() != null)
                ? lot.getCurrentLeader().getUsername()
                : null;
        String ownerUsername = (lot.getOwner() != null)
                ? lot.getOwner().getUsername()
                : null;

        return new LotDetailDTO(
                lot.getId(),
                lot.getTitle(),
                lot.getDescription(),
                lot.getCategory().getId(),
                lot.getInitialPrice(),
                current,
                lot.getStatus().name(),
                lot.getStartDate(),
                lot.getEndDate(),
                Collections.emptyList(),
                leader,
                ownerUsername
        );
    }
}
