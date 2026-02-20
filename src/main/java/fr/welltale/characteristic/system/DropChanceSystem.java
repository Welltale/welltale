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
    private Integer chanceStatIndex = null;
    private Integer dropChanceStatIndex = null;

    // Drop chance bonus per point of Chance: 0.1% (0.001)
    private static final float DROP_CHANCE_PER_CHANCE = 0.001f;

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
        if (chanceStatIndex == null) {
            chanceStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_CHANCE_KEY);
            dropChanceStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_DROP_CHANCE_KEY);
        }

        // Get Chance and DropChance values from EntityStatMap
        EntityStatValue chanceStatValue = entityStatMap.get(chanceStatIndex);
        if (chanceStatValue != null) {
            float chanceValue = chanceStatValue.getMax();
            float calculatedDropChance = chanceValue * DROP_CHANCE_PER_CHANCE;

            // Update the DropChance stat modifier based on Chance
            // This updates the DropChance stat value which can be used when processing drops
            String modifierKey = "ChanceDropBonus";
            StaticModifier dropChanceModifier = new StaticModifier(
                    Modifier.ModifierTarget.MAX,
                    StaticModifier.CalculationType.ADDITIVE,
                    calculatedDropChance
            );
            entityStatMap.putModifier(
                    dropChanceStatIndex,
                    dropChanceModifier.getCalculationType().createKey(modifierKey),
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
    //TODO ADD MOB DROP SYSTEM
    public static float getDropChanceMultiplier(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) {
            return 1.0f;
        }

        int dropChanceStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_DROP_CHANCE_KEY);
        EntityStatValue dropChanceStatValue = entityStatMap.get(dropChanceStatIndex);
        if (dropChanceStatValue != null) {
            // The DropChance stat is stored as a percentage (e.g., 0.1 = 10%)
            // Convert to multiplier: 1.0 + percentage
            return 1.0f + dropChanceStatValue.getMax();
        }
        return 1.0f;
    }
}
