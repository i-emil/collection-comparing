package com.onedome.collectioncomparing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CollectionLshBandId implements Serializable {

    @Column(name = "collection_id")
    private Long collectionId;

    @Column(name = "band_index")
    private Integer bandIndex;
}
