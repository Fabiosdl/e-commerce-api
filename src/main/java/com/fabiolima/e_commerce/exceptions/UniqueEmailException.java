package com.fabiolima.e_commerce.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UniqueEmailException extends IllegalArgumentException {

    public UniqueEmailException(String message){
        super(message);
    }
}
