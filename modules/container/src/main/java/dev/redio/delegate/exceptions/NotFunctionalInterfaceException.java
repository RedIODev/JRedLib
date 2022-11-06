package dev.redio.delegate.exceptions;

public class NotFunctionalInterfaceException extends IllegalArgumentException {

    public NotFunctionalInterfaceException(String message) {
        super(message);
    }

    public NotFunctionalInterfaceException(Class<?> clazz) {
        super(clazz.getName() + "is not functional");
    }
}
