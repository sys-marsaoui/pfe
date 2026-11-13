package com.example.hiring.exceptions;

public class RGIException extends RuntimeException {
    private String message;

    public RGIException(String message){
        super(message);
    }
}
