package com.example.demo.exceptions;

public class ResourceAlreadyExistsException extends RuntimeException{

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String resourceName, String field, Object value) {
        super(String.format("%s existe déjà avec %s: '%s'", resourceName, field, value));
    }
}
