package com.onedome.collectioncomparing.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "collection_lsh_band")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionLshBandEntity {

    @EmbeddedId
    private CollectionLshBandId id;

    @MapsId("collectionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id")
    private CollectionEntity collection;

    @Column(name = "band_hash")
    private Long bandHash;
}
