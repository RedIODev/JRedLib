module dev.redio.memory {
    requires dev.redio.container;
    provides dev.redio.span.SpanFactory with dev.redio.memory.SpanFactoryImpl;
}
