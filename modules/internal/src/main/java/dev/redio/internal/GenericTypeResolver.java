package dev.redio.internal;

import dev.redio.internal.exceptions.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;


public abstract class GenericTypeResolver<T> {

    private static final Map<TypeVariable<?>, Class<?>> translator = new HashMap<>();
    private final Class<?> selectedParameter;

    protected GenericTypeResolver(Class<?> provider) {
        final var type = this.getClass();
        if (!type.isAnonymousClass() || !type.getSuperclass().equals(GenericTypeResolver.class))
            throw new IllegalSubClassError(type, GenericTypeResolver.class);
        Type typeT = findT();
        if (typeT instanceof Class<?> clazz) {
            selectedParameter = clazz;
            return;
        }

        Class<?> classT = getSelectedClass(typeT);
        if (classT != null) {
            selectedParameter = classT;
            return;
        }
        addAllTypesToTranslator(provider);
        classT = getSelectedClass(typeT);
        if (classT != null)
            selectedParameter = classT;

        throw new TypeResolutionException("Selected Parameter " + typeT + " is not present or resolved in " +
                provider + ".");
    }


    private Type findT() {
        if (getClass().getGenericSuperclass() instanceof ParameterizedType parameterizedType)
            return parameterizedType.getActualTypeArguments()[0];
        throw new TypeResolutionException("Unreachable Exception witch should never be thrown.");
    }

    private static Class<?> getSelectedClass(Type typeT) {
        if (typeT instanceof TypeVariable<?> typeVariable)
            return translator.get(typeVariable);
        return null;
    }


    //
    //Matcher
    //
    private static void addAllTypesToTranslator(Class<?> clazz) {
        translator.putAll(matchClassAndTypeVariable(getTypesWithTypeParameter(clazz),
                getAllSuperTypeParameter(clazz)));
    }


    private static List<TypeVariable<?>> getAllSuperTypeParameter(Class<?> clazz) {
        List<TypeVariable<?>> result = new ArrayList<>();
        if (clazz == null || Object.class.equals(clazz))
            return result;
        result.addAll(List.of(clazz.getTypeParameters()));

        Class<?> superClass = clazz.getSuperclass();
        result.addAll(getAllSuperTypeParameter(superClass));

        for (Class<?> superInterface : clazz.getInterfaces())
            result.addAll(getAllSuperTypeParameter(superInterface));
        return result;
    }

    private static List<ParameterizedType> getTypesWithTypeParameter(Class<?> clazz) {
        List<ParameterizedType> result = new ArrayList<>();
        if (clazz == null || Object.class.equals(clazz))
            return result;

        Type genericSuperClass = clazz.getGenericSuperclass();
        if (genericSuperClass instanceof ParameterizedType parameterizedType)
            result.add(parameterizedType);

        for (Type genericSuperInterface : clazz.getGenericInterfaces())
            if (genericSuperInterface instanceof ParameterizedType parameterizedType)
                result.add(parameterizedType);

        Class<?> superClass = clazz.getSuperclass();
        result.addAll(getTypesWithTypeParameter(superClass));

        for (Class<?> superInterface : clazz.getInterfaces())
            result.addAll(getTypesWithTypeParameter(superInterface));

        return result;
    }

    private static Map<TypeVariable<?>, Class<?>> matchClassAndTypeVariable(List<ParameterizedType> container,
                                                                            List<TypeVariable<?>> typeVariables) {
        Map<TypeVariable<?>, Class<?>> result = new HashMap<>();
        for (ParameterizedType type : container) {
            for (TypeVariable<?> variable : typeVariables) {
                int index = getIndexOfActualTypeArgument(type, variable);
                if (index != -1)
                    result.put(variable, getClassFromParametrizedType(type, index, container));
            }
        }
        return result;
    }


    private static int getIndexOfActualTypeArgument(ParameterizedType container, TypeVariable<?> typeVariable) {
        if (container.getRawType() instanceof Class<?> rawClass) {
            TypeVariable<?>[] rawVariables = rawClass.getTypeParameters();
            for (int i = 0; i < rawVariables.length; i++)
                if (rawVariables[i] == typeVariable)
                    return i;
        }
        return -1;
    }

    private static Class<?> getClassFromParametrizedType(ParameterizedType actualTypeContainer, int index,
                                                         List<ParameterizedType> container)
            throws IndexOutOfBoundsException, TypeResolutionException {
        if (actualTypeContainer.getActualTypeArguments().length <= index)
            throw new IndexOutOfBoundsException(index);
        Type typeArgument = actualTypeContainer.getActualTypeArguments()[index];
        if (typeArgument instanceof Class<?> clazz)
            return clazz;
        if (typeArgument instanceof TypeVariable<?> typeVariable)
            return resolveInheritance(typeVariable, container);
        if (typeArgument instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() instanceof Class<?> clazz)
            return clazz;
        throw new TypeResolutionException(typeArgument);
    }


    private static Class<?> resolveInheritance(TypeVariable<?> typeArgument, List<ParameterizedType> container)
            throws TypeResolutionException {
        for (ParameterizedType type : container) {
            int index = getIndexOfActualTypeArgument(type, typeArgument);
            if (index != -1) {
                return getClassFromParametrizedType(type, index, container);
            }
        }
        throw new TypeResolutionException(typeArgument);
    }
    //
    //end Matcher


    @SuppressWarnings("unchecked")
    public final Class<T> get() {
        return (Class<T>) selectedParameter;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof GenericTypeResolver<?> resolver)
            return selectedParameter.equals(resolver.selectedParameter);
        return false;
    }

    @Override
    public int hashCode() {
        return selectedParameter.hashCode();
    }
}