package com.onedome.collectioncomparing.service.impl;


import com.onedome.collectioncomparing.controller.dto.CollectionDto;
import com.onedome.collectioncomparing.entity.CollectionEntity;
import com.onedome.collectioncomparing.entity.CollectionLshBandEntity;
import com.onedome.collectioncomparing.entity.CollectionLshBandId;
import com.onedome.collectioncomparing.model.Signature;
import com.onedome.collectioncomparing.repository.CollectionLshBandRepository;
import com.onedome.collectioncomparing.repository.CollectionRepository;
import com.onedome.collectioncomparing.service.CollectionService;
import com.onedome.collectioncomparing.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final SignatureService signatureService;
    private final CollectionRepository collectionRepository;
    private final CollectionLshBandRepository bandRepository;

    @Override
    public void saveCollection(List<CollectionDto> collections) {
        List<CollectionLshBandEntity> bandEntities = new ArrayList<>();
        List<CollectionEntity> collectionEntities = new ArrayList<>();

        collections.forEach(collection -> {
            Signature signature = signatureService.createSignature(collection.getIconIds());
            CollectionEntity entity = buildCollection(collection.getIconIds(), collection.getIconIds().size(), signature.hash());
            List<CollectionLshBandEntity> bands = IntStream.range(0, signature.bandHashes().size())
                    .mapToObj(i -> buildBand(entity, i, signature.bandHashes().get(i)))
                    .toList();
            collectionEntities.add(entity);
            bandEntities.addAll(bands);
        });


        collectionRepository.saveAll(collectionEntities);
        bandRepository.saveAll(bandEntities);
    }

    private static CollectionLshBandEntity buildBand(CollectionEntity entity, int i, Long bandHash) {
        CollectionLshBandEntity band = new CollectionLshBandEntity();
        band.setId(new CollectionLshBandId(entity.getId(), i));
        band.setCollection(entity);
        band.setBandHash(bandHash);
        return band;
    }

    private static CollectionEntity buildCollection(List<Long> iconIds, int iconsSize, byte[] signature) {
        CollectionEntity entity = new CollectionEntity();
        entity.setIconIds(iconIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        entity.setSize(iconsSize);
        entity.setMinhash(signature);
        return entity;
    }
}