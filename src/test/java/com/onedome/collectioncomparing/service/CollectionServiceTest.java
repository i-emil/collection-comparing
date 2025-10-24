package com.onedome.collectioncomparing.service;

import com.onedome.collectioncomparing.configuration.BaseIT;
import com.onedome.collectioncomparing.controller.CollectionController;
import com.onedome.collectioncomparing.controller.dto.CollectionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
public class CollectionServiceTest extends BaseIT {

    @Autowired
    private CollectionController collectionController;

    @ParameterizedTest
    @MethodSource("inputs")
    void createCollections_internalValidation(List<List<Long>> validInputs, List<Long> noValidInput) {
        collectionController.createCollections(validInputs.stream().map(CollectionDto::new).toList());

        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> collectionController.createCollections(List.of(new CollectionDto(noValidInput))));
        System.out.println(illegalStateException.getMessage());
    }

    @Test
    void createCollections_externalValidation() {
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> collectionController.createCollections(
                        List.of(
                                new CollectionDto(List.of(1L, 2L)),
                                new CollectionDto(List.of(1L, 2L, 3L, 4L))
                        )
                )
        );
        System.out.println(illegalStateException.getMessage());
    }

    static List<Arguments> inputs() {
        return List.of(
                Arguments.of(
                        List.of(List.of(1L)),
                        List.of(1L)
                ),
                Arguments.of(
                        List.of(List.of(1L, 2L, 3L)),
                        List.of(1L)
                ),
                Arguments.of(
                        List.of(List.of(1L), List.of(1L, 2L, 3L, 4L)),
                        List.of(1L, 2L)
                ),
                Arguments.of(
                        List.of(List.of(1L, 2L, 3L, 4L)),
                        List.of(1L)
                ),
                Arguments.of(
                        List.of(LongStream.rangeClosed(1, 100).boxed().toList()),
                        List.of(1L)
                ),
                Arguments.of(
                        List.of(LongStream.rangeClosed(1, 100).boxed().toList(), LongStream.rangeClosed(50, 200).boxed().toList()),
                        LongStream.rangeClosed(10, 80).boxed().toList()
                ),
                Arguments.of(
                        List.of(LongStream.rangeClosed(10, 10000).boxed().toList(), LongStream.rangeClosed(9000, 9999).boxed().toList()),
                        LongStream.rangeClosed(8000, 10000).boxed().toList()
                ),
                Arguments.of(
                        List.of(List.of(3L, 3L, 2L, 1L)),
                        List.of(2L, 1L)
                )
        );
    }
}
