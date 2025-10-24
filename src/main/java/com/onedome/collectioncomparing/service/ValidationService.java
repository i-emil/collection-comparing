package com.onedome.collectioncomparing.service;

import com.onedome.collectioncomparing.controller.dto.CollectionDto;

import java.util.List;

public interface ValidationService {
    void validate(List<CollectionDto> collections);
}
