package com.onedome.collectioncomparing.service;

import com.onedome.collectioncomparing.model.HashData;

import java.util.List;

public interface HashService {

    HashData createHash(List<Long> dataIds);

    double compareHashes(byte[] hashA, byte[] hashB);
}
