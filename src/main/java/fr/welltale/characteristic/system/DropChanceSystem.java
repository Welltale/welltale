package fr.welltale.characteristic.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;

import javax.annotation.Nonnull;

/**
 * System that applies the Chance characteristic to player drop chance.
 * This runs every tick to ensure the DropChance stat is updated based on current Chance stat.
 */
public class DropChanceSystem extends EntityTickingSystem<EntityStore> {
    @Nonnull
    private static final Query<EntityStore> QUERY = Query.and(
            PlayerRef.getComponentType()
    );

    // Cached stat indices
    private int chanceStatIndex = -1;
    private int dropChanceStatIndex = -1;

    // Drop chance bonus per point of Chance: 0.1% (0.001)
    private static final float DROP_CHANCE_PER_CHANCE = 0.001f;
    private static final float UPDATE_EPSILON = 0.0001f;
    private static final String DROP_CHANCE_MODIFIER_KEY =
            StaticModifier.CalculationType.ADDITIVE.createKey("ChanceDropBonus");

    public DropChanceSystem() {
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

        // Lazy initialization of stat indices
        if (chanceStatIndex < 0) {
            chanceStatIndex = resolveStatIndex(Characteristics.STATIC_MODIFIER_CHANCE_KEY);
            dropChanceStatIndex = resolveStatIndex(Characteristics.STATIC_MODIFIER_DROP_CHANCE_KEY);
        }

        if (chanceStatIndex < 0 || dropChanceStatIndex < 0) {
            return;
        }

        // Get Chance and DropChance values from EntityStatMap
        EntityStatValue chanceStatValue = entityStatMap.get(chanceStatIndex);
        if (chanceStatValue != null) {
            float chanceValue = chanceStatValue.getMax();
            float calculatedDropChance = chanceValue * DROP_CHANCE_PER_CHANCE;

            EntityStatValue dropChanceStatValue = entityStatMap.get(dropChanceStatIndex);
            if (dropChanceStatValue != null && Math.abs(dropChanceStatValue.getMax() - calculatedDropChance) <= UPDATE_EPSILON) {
                return;
            }

            // Update the DropChance stat modifier based on Chance
            // This updates the DropChance stat value which can be used when processing drops
            StaticModifier dropChanceModifier = new StaticModifier(
                    Modifier.ModifierTarget.MAX,
                    StaticModifier.CalculationType.ADDITIVE,
                    calculatedDropChance
            );
            entityStatMap.putModifier(
                    dropChanceStatIndex,
                    DROP_CHANCE_MODIFIER_KEY,
                    dropChanceModifier
            );
        }
    }

    /**
     * Gets the drop chance multiplier for a player from their EntityStatMap.
     * This should be called when processing mob drops.
     * @param ref The entity reference of the player
     * @param store The entity store
     * @return The drop chance multiplier (e.g., 1.5 = 50% increased drop rate)
     */
    public static float getDropChanceMultiplier(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) {
            return 1.0f;
        }

        int chanceStatIndex = resolveStatIndex(Characteristics.STATIC_MODIFIER_CHANCE_KEY);
        if (chanceStatIndex < 0) {
            return 1.0f;
        }

        EntityStatValue chanceStatValue = entityStatMap.get(chanceStatIndex);
        if (chanceStatValue != null) {
            float chanceValue = Math.max(0f, chanceStatValue.getMax());
            float bonus = chanceValue * DROP_CHANCE_PER_CHANCE;
            return 1.0f + bonus;
        }

        return 1.0f;
    }

    private static int resolveStatIndex(String statKey) {
        int index = EntityStatType.getAssetMap().getIndex(statKey);
        if (index < 0) {
            return -1;
        }

        EntityStatType statType = EntityStatType.getAssetMap().getAsset(index);
        if (statType == null || !statKey.equals(statType.getId())) {
            return -1;
        }

        return index;
    }
}
