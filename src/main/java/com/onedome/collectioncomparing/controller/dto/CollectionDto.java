package com.onedome.collectioncomparing.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CollectionDto {
    private List<Long> iconIds;
}
