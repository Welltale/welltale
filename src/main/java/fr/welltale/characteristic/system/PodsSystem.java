package fr.welltale.characteristic.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;

import javax.annotation.Nonnull;

/**
 * System that applies the Strength characteristic to player inventory capacity (Pods).
 * This runs every tick to ensure inventory capacity is updated based on current Strength stat.
 */
public class PodsSystem extends EntityTickingSystem<EntityStore> {
    @Nonnull
    private static final Query<EntityStore> QUERY = Query.and(
            PlayerRef.getComponentType()
    );

    // Cached stat indices
    private Integer strengthStatIndex = null;
    private Integer podsStatIndex = null;

    // Base backpack capacity (from Inventory.DEFAULT_STORAGE_CAPACITY)
    private static final short BASE_BACKPACK_CAPACITY = 36;

    // Pods bonus per point of Strength: 0.5 pods per point
    private static final float PODS_PER_STRENGTH = 0.5f;

    public PodsSystem() {
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        // Lazy initialization of stat indices
        if (strengthStatIndex == null) {
            strengthStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_STRENGTH_KEY);
            podsStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_PODS_KEY);
        }

        // Get Strength and Pods values from EntityStatMap
        EntityStatValue strengthStatValue = entityStatMap.get(strengthStatIndex);
        EntityStatValue podsStatValue = entityStatMap.get(podsStatIndex);
        if (strengthStatValue != null && podsStatValue != null) {
            short targetCapacity = getTargetCapacity(strengthStatValue, podsStatValue);
            short currentCapacity = player.getInventory().getBackpack().getCapacity();

            // Update capacity if it has changed
            if (currentCapacity != targetCapacity) {
                HytaleLogger.getLogger().atInfo().log(
                        "[PODS] Updating backpack capacity for player " +
                                store.getComponent(ref, PlayerRef.getComponentType()).getUuid() +
                                ": " + currentCapacity + " -> " + targetCapacity
                );
                player.getInventory().resizeBackpack(targetCapacity, null);
            }
        }
    }

    private static short getTargetCapacity(@Nonnull EntityStatValue strengthStatValue, @Nonnull EntityStatValue podsStatValue) {
        float strengthValue = strengthStatValue.getMax();
        float podsValue = podsStatValue.getMax();

        // Apply Strength bonus to pods (inventory capacity)
        // Each point of Strength adds PODS_PER_STRENGTH pods
        float strengthBonus = strengthValue * PODS_PER_STRENGTH;

        // Calculate target capacity: base + strength bonus + pods stat
        float targetCapacity = BASE_BACKPACK_CAPACITY + strengthBonus + podsValue;

        // Ensure capacity is at least 1
        return (short) Math.max(1, (int) targetCapacity);
    }
}
