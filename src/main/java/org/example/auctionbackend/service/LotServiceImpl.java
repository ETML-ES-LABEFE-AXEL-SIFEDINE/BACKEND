package org.example.auctionbackend.service;

import org.example.auctionbackend.dto.BidDTO;
import org.example.auctionbackend.dto.LotDTO;
import org.example.auctionbackend.dto.LotDetailDTO;
import org.example.auctionbackend.model.Lot;
import org.example.auctionbackend.repository.LotRepository;
import org.example.auctionbackend.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.example.auctionbackend.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;


@Service
public class LotServiceImpl implements LotService {
    private final LotRepository lotRepository;
    private final CategoryRepository categoryRepository;

    public LotServiceImpl(LotRepository lotRepository, CategoryRepository categoryRepository) {
        this.lotRepository = lotRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<LotDTO> listByCategory(Long categoryId, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);

        if (categoryId == null) {
            Page<Lot> lotsPage = lotRepository.findAll(pr);
            return new PageImpl<>(
                    lotsPage.getContent().stream().map(this::toDTO).collect(Collectors.toList()),
                    pr,
                    lotsPage.getTotalElements()
            );
        }

        // On récupère la catégorie demandée + ses enfants
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);

        List<Category> children =   categoryRepository.findByParentId(categoryId);
        categoryIds.addAll(children.stream().map(Category::getId).toList());

        Page<Lot> lotsPage = lotRepository.findByCategoryIdIn(categoryIds, pr);
        return new PageImpl<>(
                lotsPage.getContent().stream().map(this::toDTO).collect(Collectors.toList()),
                pr,
                lotsPage.getTotalElements()
        );
    }

    @Override
    public Optional<LotDetailDTO> findById(Long id) {
        return lotRepository.findById(id)
                .map(this::toDetailDTO);
    }

    private LotDTO toDTO(Lot lot) {
        double current = lot.getInitialPrice();  // temporaire, avant ajout des vraies enchères
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
        double current = lot.getInitialPrice();
        // pour l'instant, pas de vraie liste de bids → liste vide
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

    @Override
    public List<LotDTO> findLatestLots(int count) {
        return lotRepository.findAllByOrderByIdDesc(PageRequest.of(0, count))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


}
