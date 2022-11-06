package dev.redio.delegate;

import dev.redio.delegate.exceptions.NotFunctionalInterfaceException;
import dev.redio.internal.GenericTypeResolver;
import dev.redio.internal.exceptions.TypeResolutionException;

import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Predicate;

/**
 * A class that stores method reference objects with a specified type and enables them to be invoked later together.<p>
 * The class is designed to be extended without needing any implementation details in the extending class.
 * The only detail needed is a valid functional interface (representing the method signature of the delegate)
 * as type parameter.<p>
 * The delegate class provides a default implementation for the {@link Delegate#invoke()} method
 * used to invoke all methods contained in it together. <p>
 * This generalized default implementation is achieved using reflection and might not provide the best performance.
 * It is therefore encouraged to provide a more specialized implementation.
 * This can be done by overriding the protected {@link Delegate#generateInvokeMethod()} method.<p>
 * The delegate class also provides an alternative constructor designed for better performance in object creation
 * with the inconvenience of requiring a class object of the functional interface as its parameter.<p>
 *
 * @param <M> The functional interface representing the method signature of this delegate.
 * @see dev.redio.event.EventHandler
 * @see dev.redio.event.EventHandler.Default
 */
public abstract class Delegate<M> implements Cloneable {

    protected final List<M> invocationList = new ArrayList<>();

    private final Class<? super M> clazz;

    private M invokeMethod;

    /**
     * An alternative constructor bypassing checks for Type parameter and
     * replaces the reflective resolution of the Type parameters class with an explicit parameter.<p>
     * Only use this constructor with caution and only in case of poor performance of instantiation of delegates!
     * For example when instantiating a delegate often and repeatedly.
     *
     * @param clazz The class of the functional interface.
     */
    protected Delegate(Class<? super M> clazz) {
        this.clazz = clazz;
    }

    /**
     * The default constructor of a delegate. It validates the Type parameter during object creation at runtime.
     *
     * @throws NotFunctionalInterfaceException If the Type parameter is not a valid functional interface.
     * @throws TypeResolutionException         If the Type parameter class could not be resolved dynamically.
     */
    protected Delegate() {
        clazz = new GenericTypeResolver<M>(getClass()) {}.get();
        if (!clazz.isInterface())
            throw new NotFunctionalInterfaceException(clazz);
        long methodCount = Arrays.stream(clazz.getMethods())
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .count();
        if (methodCount != 1)
            throw new NotFunctionalInterfaceException(clazz);
    }

    /**
     * Creates the method that invokes the invocation list. <p>
     * In case of poor performance of invocation replace this general implementation with a specialized implementation.
     * For an example of such specialized implementation look at the implementation in
     * {@link dev.redio.event.standard.EventHandler}.
     *
     * @return The generated method to invoke this delegate.
     */
    protected M generateInvokeMethod() {

        InvocationHandler handler = (proxy, method, args) -> {
            if (!clazz.getMethods()[0].equals(method))
                throw new WrongMethodTypeException("Unexpected Method: " + method.getName());
            Object result = null;
            for (M listMethod : invocationList)
                result = method.invoke(listMethod, args);
            return result;
        };
        @SuppressWarnings("unchecked")
        M proxyMethod = (M) Proxy.newProxyInstance(Delegate.class.getClassLoader(), new Class[]{clazz}, handler);
        return proxyMethod;
    }

    /**
     * Adds a method to the delegate.
     *
     * @param method The method to be added.
     */
    public final void add(M method) {
        invocationList.add(Objects.requireNonNull(method));
    }

    /**
     * Removes the first occurrence of the specified method from the delegate.
     *
     * @param method The method to be removed if present.
     * @return true if this delegate contained the specified method
     */
    public final boolean remove(M method) {
        return invocationList.remove(method);
    }

    /**
     * Removes all methods not matching the provided predicate.
     * @param predicate The predicate deciding if the method should remain in the delegate.
     */
    public final void filter(Predicate<M> predicate) {
        invocationList.removeIf(predicate.negate());
    }

    /**
     * Creates and returns a method invoking all methods contained in this delegate.
     * If the methods return type is not void the last result of the delegates'
     * methods will be returned by the returned method.
     *
     * @return The generated method to invoke this delegate.
     */
    public final M invoke() {
        if (invokeMethod == null)
            invokeMethod = generateInvokeMethod();
        return invokeMethod;
    }

    /**
     * Returns a copy of the delegates' invocation list as an unmodifiable list.
     *
     * @return The copied list.
     */
    public final List<M> getInvocationList() {
        return List.copyOf(invocationList);
    }

    /**
     * Removes all methods from this delegate and adds the methods of the provided list.
     *
     * @param methodList The provided list.
     */
    public final void setInvocationList(List<? extends M> methodList) {
        this.invocationList.clear();
        this.invocationList.addAll(methodList);
    }

    /**
     * Creates and returns a copy of this delegate.
     * The containing methods are copied to the new delegate but no direct connection to the source delegate are made.
     *
     * @return A clone of this delegate.
     */
    @Override
    public final Delegate<M> clone() {
        try {
            @SuppressWarnings("unchecked")
            Delegate<M> clone = (Delegate<M>) super.clone();
            clone.invocationList.addAll(invocationList);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning should be supported");
        }
    }

    /**
     * Compares the specified object with this delegate for equality.
     * Returns true if and only if the object is also a delegate,
     * both delegates have the same method type and contain the same methods.
     *
     * @param o The object to be compared for equality with this delegate.
     * @return True if the specified object is equal to this delegate.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Delegate<?> delegate = (Delegate<?>) o;

        if (!invocationList.equals(delegate.invocationList))
            return false;
        return clazz.equals(delegate.clazz);
    }

    /**
     * Returns the hash code value of this delegate.
     * The hash code is calculated using the combined hash codes of the method list and the method type.
     *
     * @return The hash code value for this delegate.
     */
    @Override
    public int hashCode() {
        int result = invocationList.hashCode();
        result = 31 * result + clazz.hashCode();
        return result;
    }
}
