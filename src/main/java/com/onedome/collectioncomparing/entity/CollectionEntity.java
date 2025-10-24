package com.onedome.collectioncomparing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "collection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "icon_ids",  columnDefinition = "TEXT")
    private String iconIds;

    @Column(name = "size")
    private Integer size;

    @Column(name = "minhash", columnDefinition = "BYTEA")
    private byte[] minhash;
}
