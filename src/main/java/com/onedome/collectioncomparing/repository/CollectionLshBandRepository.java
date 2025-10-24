package com.onedome.collectioncomparing.repository;

import com.onedome.collectioncomparing.entity.CollectionLshBandEntity;
import com.onedome.collectioncomparing.entity.CollectionLshBandId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionLshBandRepository extends JpaRepository<CollectionLshBandEntity, CollectionLshBandId> {
    List<CollectionLshBandEntity> findAllByBandHashIn(List<Long> ids);
}
