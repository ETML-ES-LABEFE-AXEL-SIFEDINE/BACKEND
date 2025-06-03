package org.example.auctionbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.auctionbackend.dto.CreateLotRequestDTO;
import org.example.auctionbackend.dto.LotDTO;
import org.example.auctionbackend.dto.LotDetailDTO;
import org.example.auctionbackend.model.Category;
import org.example.auctionbackend.model.Lot;
import org.example.auctionbackend.model.LotStatus;
import org.example.auctionbackend.model.UserFollowedLot;
import org.example.auctionbackend.repository.CategoryRepository;
import org.example.auctionbackend.repository.LotRepository;
import org.example.auctionbackend.repository.UserFollowedLotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Override
    public Page<LotDTO> listByCategory(Long categoryId, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);

        Page<Lot> lotsPage = (categoryId == null)
                ? lotRepository.findAll(pr)
                : lotRepository.findByCategoryIdIn(collectCategoryIds(categoryId), pr);

        // Met à jour le status avant conversion
        List<LotDTO> dtos = lotsPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pr, lotsPage.getTotalElements());
    }

    private List<Long> collectCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        List<Category> children = categoryRepository.findByParentId(categoryId);
        ids.addAll(children.stream().map(Category::getId).toList());
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
    @Transactional
    public LotDTO createLot(CreateLotRequestDTO req) {
        // 1) Charger la catégorie
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found: " + req.getCategoryId())
                );

        // 2) Construire l'entité Lot (status initial = PENDING)
        Lot lot = Lot.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .initialPrice(req.getInitialPrice())
                .currentPrice(req.getInitialPrice())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(LotStatus.PENDING)
                .category(category)
                .build();

        // 3) Sauvegarder en base
        Lot saved = lotRepository.save(lot);

        // 4) Retourner le DTO mis à jour
        return toDTO(saved);
    }

    /**
     * Vérifie startDate/endDate et met à jour `lot.status` en base si nécessaire.
     */
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

    // === mapping en LotDTO ===

    private LotDTO toDTO(Lot lot) {
        // S’assurer que le status est à jour avant conversion
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
                leader
        );
    }
}
