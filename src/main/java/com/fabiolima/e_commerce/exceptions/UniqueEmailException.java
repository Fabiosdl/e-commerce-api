package com.fabiolima.e_commerce.exceptions;


public class UniqueEmailException extends RuntimeException {

    public UniqueEmailException(String message){
        super(message);
    }
}
