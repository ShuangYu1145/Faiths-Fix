package dev.faiths.event;

import dev.faiths.Faiths;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public class EventManager {
    private final Map<Class<? extends Event>, List<EventHook<Event>>> events = new HashMap<>();

    public void registerEventHook(final Class<? extends Event> eventClass, final EventHook<Event> eventHook) {
        final List<EventHook<Event>> handlers = events.computeIfAbsent(eventClass, ignored -> new ArrayList<>());
        if (!handlers.contains(eventHook)) {
            handlers.add(eventHook);
        }
    }

    public void callEvent(final Event event) {
        final List<EventHook<Event>> target = events.get(event.getClass());
        if (target != null) {
            target.stream().filter(hook -> hook.getListener().isAccessible()).forEach(hook -> {
                try {
                    hook.getHandler().invoke(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void registerEvent(final Listener target) {
        Arrays.stream(target.getClass().getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            try {
                final Object object = field.get(target);
                if (object instanceof Handler) {
                    ParameterizedType handlerType = ((ParameterizedType) field.getGenericType());
                    Faiths.INSTANCE.getEventManager().registerEventHook((Class<? extends Event>) handlerType.getActualTypeArguments()[0],
                            new EventHook<>(target, (Handler<Event>) object));
                }
            } catch (final Exception ignored) {

            }
        });
    }
}
