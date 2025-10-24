package com.onedome.collectioncomparing.service.impl;

import com.onedome.collectioncomparing.controller.dto.CollectionDto;
import com.onedome.collectioncomparing.entity.CollectionEntity;
import com.onedome.collectioncomparing.model.HashData;
import com.onedome.collectioncomparing.repository.CollectionLshBandRepository;
import com.onedome.collectioncomparing.repository.CollectionRepository;
import com.onedome.collectioncomparing.service.HashService;
import com.onedome.collectioncomparing.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private static final double COVERAGE_THRESHOLD = 0.30;

    private final CollectionLshBandRepository bandRepository;
    private final CollectionRepository collectionRepository;
    private final HashService hashService;

    @Override
    public void validate(List<CollectionDto> collections) {
        externalValidation(collections);
        internalValidation(collections);
    }

    private void externalValidation(List<CollectionDto> collections) {
        List<HashData> hashData = collections.stream()
                .map(CollectionDto::getIconIds)
                .map(hashService::createHash)
                .toList();

        for (int i = 1; i < hashData.size(); i++) {
            HashData current = hashData.get(i);
            for (int j = 0; j < i; j++) {
                HashData prev = hashData.get(j);
                double coverage = computeCoverage(
                        current.collection(), current.hash(),
                        prev.collection(), prev.hash()
                );
                if (coverage > COVERAGE_THRESHOLD) {
                    throw new IllegalStateException(
                            "Similar collections in request (>%.2f%% overlap), coverage %.2f%% between collections %d and %d"
                                    .formatted(COVERAGE_THRESHOLD * 100, coverage * 100, j, i)
                    );
                }
            }
        }

    }

    private void internalValidation(List<CollectionDto> collections) {
        collections.forEach(collection -> {
            HashData hashData = hashService.createHash(collection.getIconIds());

            Set<Long> candidateIds = bandRepository.findAllByBandHashIn(hashData.bandHashes()).stream().
                    map(x -> x.getId().getCollectionId()).
                    collect(toSet());

            if (isNotEmpty(candidateIds)) {
                validateSimilarity(collection.getIconIds(), new ArrayList<>(candidateIds), hashData.hash());
            }
        });
    }

    private void validateSimilarity(List<Long> iconIds, List<Long> candidateIds, byte[] currentHash) {
        for (Long cid : candidateIds) {
            Optional<CollectionEntity> maybe = collectionRepository.findById(cid);
            if (maybe.isEmpty()) continue;

            CollectionEntity oldCollection = maybe.get();
            double coverage = computeCoverage(iconIds, currentHash, parseIconIds(oldCollection.getIconIds()), oldCollection.getMinhash());

            if (coverage >= COVERAGE_THRESHOLD) {
                throw new IllegalStateException(
                        "Similar collection exists (>=%.2f%% overlap), coverage %.2f%% id=%s"
                                .formatted(COVERAGE_THRESHOLD * 100, coverage * 100, cid)
                );
            }
        }
    }

    private double computeCoverage(
            List<Long> currentIds,
            byte[] currentCollectionHash,
            List<Long> oldIds,
            byte[] oldCollectionHash
    ) {
        var iconsSize = currentIds.size();
        double coverage;
        if (Arrays.equals(currentCollectionHash, oldCollectionHash)) {
            coverage = 1.0;
        } else {
            if (iconsSize > 100) {
                coverage = hashService.compareHashes(oldCollectionHash, currentCollectionHash);
            } else {
                coverage = compareByElements(oldIds, currentIds);
            }
        }
        return coverage;
    }

    private double compareByElements(List<Long> oldSet, List<Long> newSet) {
        int iconsSize = newSet.size();
        int intersection = 0;
        for (Long id : newSet) {
            if (oldSet.contains(id)) intersection++;
        }
        return (double) intersection / iconsSize;
    }

    private List<Long> parseIconIds(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .map(Long::parseLong)
                .toList();
    }
}
