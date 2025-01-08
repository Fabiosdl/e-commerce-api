package com.fabiolima.online_shop.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class InvalidIdException extends RuntimeException{
    public InvalidIdException (String message){
        super(message);
    }
}
