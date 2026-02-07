package fr.welltale.level.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.level.hud.level.LevelProgress;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@AllArgsConstructor
public class PlayerJoinSystem extends RefSystem<EntityStore> {
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;
    private final Universe universe;

    @Override
    public void onEntityAdded(
            @NonNull Ref<EntityStore> ref,
            @NonNull AddReason addReason,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (!ref.isValid()) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerJoinSystem OnEntityAdded Failed: Ref is invalid");
            return;
        }

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerJoinSystem OnEntityAdded Failed: PlayerRef is null");
            return;
        }

        Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            universe.removePlayer(playerRef);
            this.logger.atSevere()
                    .log("[PLAYER] PlayerJoinSystem OnEntityAdded Failed: PlayerData is null");
            return;
        }

        PlayerLevelComponent playerLevelComponent = store.getComponent(ref, PlayerLevelComponent.getComponentType());
        if (playerLevelComponent != null) {
            if (playerData.getExperience() != playerLevelComponent.getTotalExperience()) {
                playerData.setExperience(playerLevelComponent.getTotalExperience());
            }
        } else {
            playerLevelComponent = new PlayerLevelComponent();
            commandBuffer.addComponent(ref, PlayerLevelComponent.getComponentType(), playerLevelComponent);
        }

        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player != null) {
            player.getHudManager().setCustomHud(playerRef, new LevelProgress(playerRef, playerLevelComponent));
        }
    }

    @Override
    public void onEntityRemove(
            @NonNull Ref<EntityStore> ref,
            @NonNull RemoveReason removeReason,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (!ref.isValid()) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerJoinSystem OnEntityRemove Failed: Ref is invalid");
            return;
        }

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        PlayerLevelComponent playerLevelComponent = store.getComponent(ref, PlayerLevelComponent.getComponentType());
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
