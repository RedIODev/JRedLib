module dev.redio.container {
    exports dev.redio.delegate;
    exports dev.redio.delegate.exceptions;
    exports dev.redio.event;
    exports dev.redio.span;

    requires transitive dev.redio.internal;

    uses dev.redio.span.SpanFactory;
    provides dev.redio.span.SpanFactory with dev.redio.span.internal.SpanFactoryImpl;
}