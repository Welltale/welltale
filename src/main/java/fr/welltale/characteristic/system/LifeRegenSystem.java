package fr.welltale.characteristic.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;
import lombok.NonNull;

/**
 * System that implements natural health regeneration inspired by Dofus.
 *
 * Mechanism:
 * - Base natural regeneration: 1 HP per second
 * - LifeRegen bonus: adds % regeneration based on LifeRegen stat
 *   - Formula: regen = base * (1 + lifeRegenPct / 100)
 *
 * The system directly modifies the Health stat's current value each tick
 * to simulate natural regeneration, without increasing any permanent stats.
 */
public class LifeRegenSystem extends EntityTickingSystem<EntityStore> {
    // Cached stat indices
    private int lifeRegenStatIndex = -1;
    private int healthStatIndex = -1;

    // Base natural regeneration in HP per second
    private static final float BASE_REGEN_PER_SECOND = 1.0f;

    public LifeRegenSystem() {
    }

    @Override
    public @NonNull Query<EntityStore> getQuery() {
        return Query.and(PlayerRef.getComponentType());
    }

    @Override
    public void tick(
            float dt,
            int index,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) return;

        // Lazy initialization of stat indices
        if (lifeRegenStatIndex < 0) {
            lifeRegenStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_LIFE_REGEN_PCT_KEY);
            healthStatIndex = DefaultEntityStatTypes.getHealth();
        }

        // Get Health values from EntityStatMap
        EntityStatValue healthStatValue = entityStatMap.get(healthStatIndex);
        if (healthStatValue == null) return;

        // Only regenerate if health is not at max
        float currentHealth = healthStatValue.get();
        float maxHealth = healthStatValue.getMax();

        if (currentHealth >= maxHealth) {
            return; // Already at full health
        }

        // Calculate regeneration amount for this tick
        float regenPerTick = calculateRegenPerTick(entityStatMap, dt);

        // Apply regeneration
        entityStatMap.addStatValue(healthStatIndex, regenPerTick);
    }

    /**
     * Calculates the regeneration amount for a single tick.
     * Formula: (base * (1 + lifeRegenPct / 100)) * delta_time
     *
     * @param entityStatMap The entity's stat map
     * @param dt Delta time in seconds
     * @return Regeneration amount for this tick
     */
    private float calculateRegenPerTick(@NonNull EntityStatMap entityStatMap, float dt) {
        EntityStatValue lifeRegenStatValue = entityStatMap.get(lifeRegenStatIndex);

        // Base regeneration
        float totalRegen = BASE_REGEN_PER_SECOND;

        // Additional LifeRegen % bonus (10 => +10% regen speed)
        if (lifeRegenStatValue != null) {
            float lifeRegenBonusPct = lifeRegenStatValue.getMax();
            totalRegen *= (1.0f + (lifeRegenBonusPct / 100.0f));
        }

        // Apply delta time
        return totalRegen * dt;
    }
}
