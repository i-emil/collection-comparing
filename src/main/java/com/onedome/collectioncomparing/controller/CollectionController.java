package com.onedome.collectioncomparing.controller;


import com.onedome.collectioncomparing.controller.dto.CollectionDto;
import com.onedome.collectioncomparing.service.CollectionService;
import com.onedome.collectioncomparing.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class CollectionController {

    private final ValidationService validationService;
    private final CollectionService collectionService;
    
    @PostMapping("/collections")
    public void createCollections(@RequestBody List<CollectionDto> collections) {
        validationService.validate(collections);
        collectionService.saveCollection(collections);
    }
}