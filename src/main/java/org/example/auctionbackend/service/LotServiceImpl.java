package org.example.auctionbackend.service;

import lombok.RequiredArgsConstructor;
import org.example.auctionbackend.dto.LotDTO;
import org.example.auctionbackend.dto.LotDetailDTO;
import org.example.auctionbackend.model.Category;
import org.example.auctionbackend.model.Lot;
import org.example.auctionbackend.model.UserFollowedLot;
import org.example.auctionbackend.repository.CategoryRepository;
import org.example.auctionbackend.repository.LotRepository;
import org.example.auctionbackend.repository.UserFollowedLotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return new PageImpl<>(
                lotsPage.getContent().stream().map(this::toDTO).collect(Collectors.toList()),
                pr,
                lotsPage.getTotalElements()
        );
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
                .map(this::toDetailDTO);
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

    // === mapping ===

    private LotDTO toDTO(Lot lot) {
        double current = lot.getCurrentPrice() != null
                ? lot.getCurrentPrice()
                : lot.getInitialPrice();

        return new LotDTO(
                lot.getId(),
                lot.getTitle(),
                lot.getDescription(),
                lot.getCategory().getId(),
                lot.getInitialPrice(),
                current
        );
    }

    private LotDetailDTO toDetailDTO(Lot lot) {
        double current = lot.getCurrentPrice() != null
                ? lot.getCurrentPrice()
                : lot.getInitialPrice();

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
                Collections.emptyList()
        );
    }
}
