package com.fabiolima.e_commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PayPalException extends RuntimeException{
    public PayPalException(String message){
        super(message);
    }
}