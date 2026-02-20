package fr.welltale.characteristic.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.player.UpdateMovementSettings;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;

import javax.annotation.Nonnull;

/**
 * System that applies the MoveSpeed characteristic to player movement settings.
 * This runs every tick to ensure movement speed is updated based on current characteristics.
 */
public class MoveSpeedSystem extends EntityTickingSystem<EntityStore> {
    @Nonnull
    private static final Query<EntityStore> QUERY = Query.and(
            PlayerRef.getComponentType()
    );

    // Cached stat indices
    private Integer moveSpeedStatIndex = null;
    private Integer agilityStatIndex = null;

    // Agility bonus per point: 0.1% (0.001)
    private static final float AGILITY_BONUS_PER_POINT = 0.001f;

    public MoveSpeedSystem() {
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

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) return;

        MovementManager movementManager = store.getComponent(ref, MovementManager.getComponentType());
        if (movementManager == null) return;

        // Lazy initialization of stat indices
        if (moveSpeedStatIndex == null) {
            moveSpeedStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_MOVE_SPEED_KEY);
            agilityStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_AGILITY_KEY);
        }

        // Get MoveSpeed and Agility values from EntityStatMap
        EntityStatValue moveSpeedStatValue = entityStatMap.get(moveSpeedStatIndex);
        EntityStatValue agilityStatValue = entityStatMap.get(agilityStatIndex);
        float speedMultiplier = 0;
        if (moveSpeedStatValue != null && agilityStatValue != null) {
            speedMultiplier = getSpeedMultiplier(moveSpeedStatValue, agilityStatValue);
            HytaleLogger.getLogger().atInfo().log("SPEED MULTIPLIER: " + speedMultiplier);
        }


        // Get current settings and update them with the speed multiplier
        var settings = movementManager.getSettings();
        var defaultSettings = movementManager.getDefaultSettings();

        // Update the movement settings with the speed multiplier
        // We only update if the multiplier has changed to avoid unnecessary packet sends
        boolean needsUpdate = false;

        if (Math.abs(settings.baseSpeed - defaultSettings.baseSpeed * speedMultiplier) > 0.001f) {
            settings.baseSpeed = defaultSettings.baseSpeed * speedMultiplier;
            needsUpdate = true;
        }

        // Update climb speeds
        if (Math.abs(settings.climbSpeed - defaultSettings.climbSpeed * speedMultiplier) > 0.001f) {
            settings.climbSpeed = defaultSettings.climbSpeed * speedMultiplier;
            needsUpdate = true;
        }

        if (Math.abs(settings.climbSpeedLateral - defaultSettings.climbSpeedLateral * speedMultiplier) > 0.001f) {
            settings.climbSpeedLateral = defaultSettings.climbSpeedLateral * speedMultiplier;
            needsUpdate = true;
        }

        // Update climb sprint speeds
        if (Math.abs(settings.climbUpSprintSpeed - defaultSettings.climbUpSprintSpeed * speedMultiplier) > 0.001f) {
            settings.climbUpSprintSpeed = defaultSettings.climbUpSprintSpeed * speedMultiplier;
            needsUpdate = true;
        }

        if (Math.abs(settings.climbDownSprintSpeed - defaultSettings.climbDownSprintSpeed * speedMultiplier) > 0.001f) {
            settings.climbDownSprintSpeed = defaultSettings.climbDownSprintSpeed * speedMultiplier;
            needsUpdate = true;
        }

        // Update fly speeds
        if (Math.abs(settings.horizontalFlySpeed - defaultSettings.horizontalFlySpeed * speedMultiplier) > 0.001f) {
            settings.horizontalFlySpeed = defaultSettings.horizontalFlySpeed * speedMultiplier;
            needsUpdate = true;
        }

        if (Math.abs(settings.verticalFlySpeed - defaultSettings.verticalFlySpeed * speedMultiplier) > 0.001f) {
            settings.verticalFlySpeed = defaultSettings.verticalFlySpeed * speedMultiplier;
            needsUpdate = true;
        }

        // Send update to client if settings changed
        if (needsUpdate) {
            playerRef.getPacketHandler().writeNoCache(new UpdateMovementSettings(settings));
        }
    }

    private static float getSpeedMultiplier(@Nonnull EntityStatValue moveSpeedStatValue, @Nonnull EntityStatValue agilityStatValue) {
        float moveSpeedValue = moveSpeedStatValue.getMax();
        float agilityValue = agilityStatValue.getMax();

        // Apply MoveSpeed characteristic to movement settings
        // The MoveSpeed characteristic is a percentage-based multiplier
        // Default value of 10 means 10% speed boost, so we convert to 1.0 + 0.10 = 1.10

        // Apply Agility bonus to movement speed
        // Each point of Agility adds 0.1% (0.001) to the movespeed
        float agilityBonus = agilityValue * AGILITY_BONUS_PER_POINT;

        return 1.0f + (moveSpeedValue / 100.0f) + agilityBonus;
    }
}
