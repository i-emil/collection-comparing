package com.onedome.collectioncomparing.service.impl;


import com.dynatrace.hash4j.hashing.Hashing;
import com.dynatrace.hash4j.similarity.ElementHashProvider;
import com.dynatrace.hash4j.similarity.SimilarityHashPolicy;
import com.dynatrace.hash4j.similarity.SimilarityHasher;
import com.dynatrace.hash4j.similarity.SimilarityHashing;
import com.onedome.collectioncomparing.entity.CollectionEntity;
import com.onedome.collectioncomparing.entity.CollectionLshBandEntity;
import com.onedome.collectioncomparing.entity.CollectionLshBandId;
import com.onedome.collectioncomparing.repository.CollectionLshBandRepository;
import com.onedome.collectioncomparing.repository.CollectionRepository;
import com.onedome.collectioncomparing.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private static final int NUM_COMPONENTS = 256;
    private static final int BITS_PER_COMPONENT = 1;
    private static final int BAND_COUNT = 32;

    private static final double COVERAGE_THRESHOLD = 0.30;

    private final CollectionRepository collectionRepository;
    private final CollectionLshBandRepository bandRepository;

    private static final SimilarityHashPolicy POLICY = SimilarityHashing.superMinHash(NUM_COMPONENTS, BITS_PER_COMPONENT);
    private static final SimilarityHasher HASHER = POLICY.createHasher();
    private static final ToLongFunction<Long> ELEM_HASH = v -> Hashing.komihash5_0().hashLongToLong(v);

    @Transactional
    public void createCollectionIfUnique(List<Long> iconIds) {
        final int iconsSize = iconIds.size();

        byte[] signature = HASHER.compute(ElementHashProvider.ofCollection(iconIds, ELEM_HASH));

        List<Long> newBandHashes = computeBandHashes(signature);

        Set<Long> candidateIds = bandRepository.findAllByBandHashIn(newBandHashes).stream().
                map(x -> x.getId().getCollectionId()).
                collect(toSet());

        if (isNotEmpty(candidateIds)) {
            validateSimilarity(iconIds, new ArrayList<>(candidateIds), signature);
        }

        CollectionEntity entity = buildCollection(iconIds, iconsSize, signature);

        List<CollectionLshBandEntity> bands = new ArrayList<>(BAND_COUNT);
        for (int i = 0; i < BAND_COUNT; i++) {
            bands.add(buildBand(entity, i, newBandHashes));
        }

        collectionRepository.save(entity);
        bandRepository.saveAll(bands);
    }

    private static CollectionLshBandEntity buildBand(CollectionEntity entity, int i, List<Long> newBandHashes) {
        CollectionLshBandEntity band = new CollectionLshBandEntity();
        band.setId(new CollectionLshBandId(entity.getId(), i));
        band.setCollection(entity);
        band.setBandHash(newBandHashes.get(i));
        return band;
    }

    private static CollectionEntity buildCollection(List<Long> iconIds, int iconsSize, byte[] signature) {
        CollectionEntity entity = new CollectionEntity();
        entity.setIconIds(iconIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        entity.setSize(iconsSize);
        entity.setMinhash(signature);
        return entity;
    }

    private void validateSimilarity(List<Long> iconIds, List<Long> candidateIds, byte[] currentSignature) {
        var iconsSize = iconIds.size();
        for (Long cid : candidateIds) {
            Optional<CollectionEntity> maybe = collectionRepository.findById(cid);
            if (maybe.isEmpty()) continue;

            CollectionEntity oldCollection = maybe.get();
            double coverage = computeCoverage(iconIds, currentSignature, oldCollection, iconsSize);;

            if (coverage >= COVERAGE_THRESHOLD) {
                throw new IllegalStateException(
                        "Similar collection exists (>=%.2f%% overlap), coverage %.2f%% id=%s"
                                .formatted(COVERAGE_THRESHOLD * 100, coverage * 100, cid)
                );
            }
        }
    }

    private double computeCoverage(
            List<Long> iconIds,
            byte[] currentSignature,
            CollectionEntity oldCollection,
            int iconsSize
    ) {
        double coverage;
        if (Arrays.equals(currentSignature, oldCollection.getMinhash())) {
            coverage = 1.0;
        } else {
            if (iconsSize > 100) {
                coverage = compareBySignatures(oldCollection.getMinhash(), currentSignature);
            } else {
                coverage = compareByElements(iconsSize, oldCollection, iconIds);
            }
        }
        return coverage;
    }

    private double compareByElements(int iconsSize, CollectionEntity maybe, List<Long> newSet) {
        Set<Long> existing = parseIconIds(maybe.getIconIds());
        int intersection = 0;
        for (Long id : newSet) {
            if (existing.contains(id)) intersection++;
        }
        return (double) intersection / iconsSize;
    }

    private double compareBySignatures(byte[] oldCollectionSignature, byte[] currentSignature) {
        double fraction = POLICY.getFractionOfEqualComponents(currentSignature, oldCollectionSignature);
        return (fraction - Math.pow(2.0, -BITS_PER_COMPONENT))
                / (1.0 - Math.pow(2.0, -BITS_PER_COMPONENT));
    }

    private List<Long> computeBandHashes(byte[] signature) {
        int totalBytes = signature.length;
        int base = totalBytes / BAND_COUNT;
        int rem = totalBytes % BAND_COUNT;

        List<Long> result = new ArrayList<>(BAND_COUNT);
        int offset = 0;
        for (int b = 0; b < BAND_COUNT; b++) {
            int len = base + (b < rem ? 1 : 0);
            long h = Hashing.komihash5_0().hashBytesToLong(signature, offset, len);
            result.add(h);
            offset += len;
        }
        return result;
    }

    private Set<Long> parseIconIds(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }
}