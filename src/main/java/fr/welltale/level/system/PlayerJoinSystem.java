package fr.welltale.level.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.level.LevelComponent;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@AllArgsConstructor
public class PlayerJoinSystem extends RefSystem<EntityStore> {
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;

    @Override
    public void onEntityAdded(
            @NonNull Ref<EntityStore> ref,
            @NonNull AddReason addReason,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (addReason != AddReason.LOAD) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        LevelComponent playerLevelComponent = store.getComponent(ref, LevelComponent.getComponentType());
        if (playerLevelComponent == null) {
            commandBuffer.addComponent(ref, LevelComponent.getComponentType(), new LevelComponent());
        }
    }

    //TODO CREATE TASK TO SAVE PLAYER EXPERIENCE
    @Override
    public void onEntityRemove(
            @NonNull Ref<EntityStore> ref,
            @NonNull RemoveReason removeReason,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (removeReason != RemoveReason.UNLOAD) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        LevelComponent playerLevelComponent = store.getComponent(ref, LevelComponent.getComponentType());
        if (playerLevelComponent == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerJoinSystem OnEntityRemove Failed: PlayerLevelComponent is null");
            return;
        }

        Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerJoinSystem OnEntityRemove Failed: PlayerData is null");
            return;
        }

        playerData.setExperience(playerLevelComponent.getTotalExperience());
        try {
            this.playerRepository.updatePlayer(playerData);
        } catch (Exception e) {
            this.logger.atInfo()
                    .log("[PLAYER] PlayerJoinSystem OnEntityRemove Failed: " + e.getMessage());
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Archetype.of(PlayerRef.getComponentType());
    }
}
