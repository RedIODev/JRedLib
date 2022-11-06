package dev.redio.event;

import dev.redio.delegate.Delegate;

public sealed class EventHandler<A extends EventArgs> extends Delegate<EventMethod<A>> {
    
    public EventHandler() {
        super(EventMethod.class);
    }

    @Override
    protected final EventMethod<A> generateInvokeMethod() {
        return (sender, args) -> {
            for (var eventMethod : invocationList) {
                eventMethod.fireEvent(sender, args);
            }
        };
    }

    public static final class Default extends EventHandler<EventArgs> {}
}
