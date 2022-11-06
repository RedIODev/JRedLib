package dev.redio.internal.exceptions;

public class IllegalSubClassError extends Error {
    
    public IllegalSubClassError(String message) {
        super(message);
    }

    public IllegalSubClassError(Class<?> subClass, Class<?> superClass) {
        super(subClass + " is not a direct anonymous class of " + superClass + ".");
    }
}
