package fr.welltale.item.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.ChangeGameModeEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.item.virtual.RolledItemPacketAdapter;
import org.jspecify.annotations.NonNull;

public class RolledItemGameModeSystem extends EntityEventSystem<EntityStore, ChangeGameModeEvent> {
    private final RolledItemPacketAdapter rolledItemPacketAdapter;

    public RolledItemGameModeSystem(@NonNull RolledItemPacketAdapter rolledItemPacketAdapter) {
        super(ChangeGameModeEvent.class);
        this.rolledItemPacketAdapter = rolledItemPacketAdapter;
    }

    @Override
    public void handle(
            int index,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer,
            @NonNull ChangeGameModeEvent event
    ) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        rolledItemPacketAdapter.onGameModeChanged(playerRef, event.getGameMode());
    }

    @Override
    public @NonNull Query<EntityStore> getQuery() {
        return Query.and(PlayerRef.getComponentType());
    }
}
