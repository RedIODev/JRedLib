package dev.redio.event;

@FunctionalInterface
public interface EventMethod<A extends EventArgs> {
    void fireEvent(Object sender, A e);
}
