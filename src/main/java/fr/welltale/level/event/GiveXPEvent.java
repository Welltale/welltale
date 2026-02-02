package fr.welltale.level.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public record GiveXPEvent(
        @Nonnull Ref<EntityStore> playerRef,
        long amount
) implements IEvent<Void> {
    public static void dispatch(
            @Nonnull Ref<EntityStore> playerRef,
            long amount
    ) {
        IEventDispatcher<GiveXPEvent, GiveXPEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(GiveXPEvent.class);

        if (!dispatcher.hasListener()) return;
        dispatcher.dispatch(new GiveXPEvent(playerRef, amount));
    }
}
