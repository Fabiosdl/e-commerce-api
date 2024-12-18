package com.fabiolima.online_shop.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<NotFoundException> handleNotFound(NotFoundException ex){
        return new ResponseEntity<>(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BadRequestException> handleBadRequest(BadRequestException ex){
        return new ResponseEntity<>(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ForbiddenException> handleForbidden(ForbiddenException ex){
        return new ResponseEntity<>(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<InsufficientStockException> handleInsufficientStockException(InsufficientStockException ex){
        return new ResponseEntity<>(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<OrderStatusException> handleForbidden(OrderStatusException ex){
        return new ResponseEntity<>(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PaymentMethodException> handleForbidden(PaymentMethodException ex){
        return new ResponseEntity<>(ex, HttpStatus.BAD_REQUEST);
    }
}
