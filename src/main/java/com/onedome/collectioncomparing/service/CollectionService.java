package com.onedome.collectioncomparing.service;

import java.util.List;

public interface CollectionService {
    void createCollectionIfUnique(List<Long> iconIds);
}
