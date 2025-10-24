package com.onedome.collectioncomparing.model;

import lombok.Builder;

import java.util.List;

@Builder
public record Signature(
        List<Long> collection,
        byte[] hash,
        List<Long> bandHashes
) {
}
