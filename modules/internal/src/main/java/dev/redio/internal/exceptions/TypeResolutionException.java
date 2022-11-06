package dev.redio.internal.exceptions;

import java.lang.reflect.Type;

public class TypeResolutionException extends RuntimeException {

    public TypeResolutionException(String message) {
        super(message);
    }

    public TypeResolutionException(Type typeVariable) {
        super("Type " + typeVariable + " could not be resolved to a Class.");
    }
}
