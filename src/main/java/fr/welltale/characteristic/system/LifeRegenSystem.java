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
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;

import javax.annotation.Nonnull;

/**
 * System that implements natural health regeneration inspired by Dofus.
 *
 * Mechanism:
 * - Base natural regeneration: 1 HP per second
 * - Intelligence bonus: adds % regeneration based on Intelligence stat
 *   - Formula: regen = base + base * (intelligence * INTELLIGENCE_BONUS_PER_POINT)
 *
 * The system directly modifies the Health stat's current value each tick
 * to simulate natural regeneration, without increasing any permanent stats.
 */
public class LifeRegenSystem extends EntityTickingSystem<EntityStore> {
    @Nonnull
    private static final Query<EntityStore> QUERY = Query.and(
            PlayerRef.getComponentType()
    );

    // Cached stat indices
    private Integer intelligenceStatIndex = null;
    private Integer healthStatIndex = null;

    // Base natural regeneration in HP per second
    // This is the default regeneration a player gets without any Intelligence
    private static final float BASE_REGEN_PER_SECOND = 1.0f;

    // Intelligence bonus to regeneration: 0.1% (0.001) per point of Intelligence
    private static final float INTELLIGENCE_BONUS_PER_POINT = 0.001f;

    public LifeRegenSystem() {
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
        if (intelligenceStatIndex == null) {
            intelligenceStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_INTELLIGENCE_KEY);
            healthStatIndex = DefaultEntityStatTypes.getHealth();
        }

        // Get Health and Intelligence values from EntityStatMap
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
     * Formula: (base * (1 + intelligence * bonus)) * delta_time
     *
     * @param entityStatMap The entity's stat map
     * @param dt Delta time in seconds
     * @return Regeneration amount for this tick
     */
    private float calculateRegenPerTick(@Nonnull EntityStatMap entityStatMap, float dt) {
        EntityStatValue intelligenceStatValue = entityStatMap.get(intelligenceStatIndex);

        // Base regeneration
        float totalRegen = BASE_REGEN_PER_SECOND;

        // Add Intelligence bonus if Intelligence stat exists
        // Intelligence provides a % bonus to regeneration
        if (intelligenceStatValue != null) {
            float intelligenceValue = intelligenceStatValue.getMax();
            float intelligenceBonus = intelligenceValue * INTELLIGENCE_BONUS_PER_POINT;
            totalRegen *= (1.0f + intelligenceBonus);
        }

        // Apply delta time
        return totalRegen * dt;
    }

    /**
     * Gets the total regeneration rate per second for a player.
     * This includes base regeneration + Intelligence % bonus.
     *
     * @param ref The entity reference
     * @param store The entity store
     * @return Regeneration rate in HP per second
     */
    public static float getRegenRate(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) {
            return BASE_REGEN_PER_SECOND;
        }

        int intelligenceStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_INTELLIGENCE_KEY);
        EntityStatValue intelligenceStatValue = entityStatMap.get(intelligenceStatIndex);

        float totalRegen = BASE_REGEN_PER_SECOND;
        if (intelligenceStatValue != null) {
            float intelligenceBonus = intelligenceStatValue.getMax() * INTELLIGENCE_BONUS_PER_POINT;
            totalRegen *= (1.0f + intelligenceBonus);
        }

        return totalRegen;
    }
}
