package dev.redio.span;

import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

import dev.redio.span.internal.SpanFactoryImpl;

public interface SpanFactory {
    
    static SpanFactory INSTANCE = getInstance();

    private static SpanFactory getInstance() {

        var loader = ServiceLoader.load(SpanFactory.class);
        return loader.stream()
                .filter(SpanFactory::isMemoryImpl)
                .findFirst()
                .map(Provider::get)
                .orElse(SpanFactoryImpl.INSTANCE);
        
    }

    private static boolean isMemoryImpl(Provider<SpanFactory> provider) {
        return provider.type()
                .getModule()
                .getName()
                .equals("dev.redio.memory");
    }
}
