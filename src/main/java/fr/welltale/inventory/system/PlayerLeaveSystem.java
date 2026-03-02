package fr.welltale.inventory.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.system.StaminaCostReductionSystem;
import fr.welltale.inventory.event.OpenInventoryPacketInterceptor;
import fr.welltale.item.system.RolledItemStatSystem;
import fr.welltale.item.virtual.RolledItemPacketAdapter;
import org.jspecify.annotations.NonNull;

public class PlayerLeaveSystem extends RefSystem<EntityStore> {
    private final OpenInventoryPacketInterceptor openInventoryPacketInterceptor;
    private final StaminaCostReductionSystem staminaCostReductionSystem;
    private final RolledItemPacketAdapter rolledItemPacketAdapter;
    private final RolledItemStatSystem rolledItemStatSystem;

    public PlayerLeaveSystem(
            @NonNull OpenInventoryPacketInterceptor openInventoryPacketInterceptor,
            @NonNull StaminaCostReductionSystem staminaCostReductionSystem,
            @NonNull RolledItemPacketAdapter rolledItemPacketAdapter,
            @NonNull RolledItemStatSystem rolledItemStatSystem
    ) {
        this.openInventoryPacketInterceptor = openInventoryPacketInterceptor;
        this.staminaCostReductionSystem = staminaCostReductionSystem;
        this.rolledItemPacketAdapter = rolledItemPacketAdapter;
        this.rolledItemStatSystem = rolledItemStatSystem;
    }

    @Override
    public void onEntityAdded(
            @NonNull Ref<EntityStore> ref,
            @NonNull AddReason addReason,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {}

    @Override
    public void onEntityRemove(
            @NonNull Ref<EntityStore> ref,
            @NonNull RemoveReason removeReason,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (!ref.isValid()) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        this.openInventoryPacketInterceptor.clearInstalledPlayer(playerRef.getUuid());
        this.staminaCostReductionSystem.clearPlayer(playerRef.getUuid());
        this.rolledItemPacketAdapter.onPlayerLeave(playerRef.getUuid());
        this.rolledItemStatSystem.clearPlayer(playerRef.getUuid());
    }

    @Override
    public @NonNull Query<EntityStore> getQuery() {
        return Query.and(PlayerRef.getComponentType());
    }
}
