package com.onedome.collectioncomparing.controller;

import com.onedome.collectioncomparing.controller.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleException(IllegalStateException ex) {
        return new ResponseEntity<>(
                new ErrorDto(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        return new ResponseEntity<>(
                new ErrorDto(ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
