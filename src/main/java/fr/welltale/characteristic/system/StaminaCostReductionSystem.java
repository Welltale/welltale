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

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StaminaCostReductionSystem extends EntityTickingSystem<EntityStore> {
    private static final float EPSILON = 0.0001f;
    private static final float REDUCTION_PER_STRENGTH_POINT = 0.001f;
    private static final float MAX_REDUCTION = 0.7f;

    @Nonnull
    private static final Query<EntityStore> QUERY = Query.and(PlayerRef.getComponentType());

    private final Map<UUID, Float> lastStaminaByPlayer = new ConcurrentHashMap<>();

    private int strengthStatIndex = -1;
    private int staminaStatIndex = -1;

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

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) return;

        if (strengthStatIndex < 0) {
            strengthStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_STRENGTH_KEY);
            staminaStatIndex = DefaultEntityStatTypes.getStamina();
        }

        EntityStatValue staminaValue = statMap.get(staminaStatIndex);
        if (staminaValue == null) return;

        UUID playerUuid = playerRef.getUuid();
        float currentStamina = staminaValue.get();
        Float previousStamina = this.lastStaminaByPlayer.putIfAbsent(playerUuid, currentStamina);
        if (previousStamina == null) return;

        float delta = previousStamina - currentStamina;
        if (delta <= EPSILON) {
            this.lastStaminaByPlayer.put(playerUuid, currentStamina);
            return;
        }

        float reduction = getReductionPct(statMap);
        if (reduction <= EPSILON) {
            this.lastStaminaByPlayer.put(playerUuid, currentStamina);
            return;
        }

        float refund = delta * reduction;
        if (refund <= EPSILON) {
            this.lastStaminaByPlayer.put(playerUuid, currentStamina);
            return;
        }

        statMap.addStatValue(staminaStatIndex, refund);

        EntityStatValue updatedStamina = statMap.get(staminaStatIndex);
        this.lastStaminaByPlayer.put(playerUuid, updatedStamina == null ? currentStamina : updatedStamina.get());
    }

    private float getReductionPct(EntityStatMap statMap) {
        EntityStatValue strengthValue = statMap.get(strengthStatIndex);
        if (strengthValue == null) return 0f;

        return getReductionPctFromStrength(strengthValue.getMax());
    }

    public static float getReductionPctFromStrength(float strength) {
        float reduction = strength * REDUCTION_PER_STRENGTH_POINT;
        return Math.max(0f, Math.min(MAX_REDUCTION, reduction));
    }

    public void clearPlayer(UUID playerUuid) {
        if (playerUuid == null) return;

        this.lastStaminaByPlayer.remove(playerUuid);
    }
}
