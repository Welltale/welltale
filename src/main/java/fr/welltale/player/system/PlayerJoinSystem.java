package fr.welltale.player.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import fr.welltale.player.charactercache.CachedCharacter;
import fr.welltale.player.charactercache.CharacterCacheRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@AllArgsConstructor
public class PlayerJoinSystem extends RefSystem<EntityStore> {
    private final PlayerRepository playerRepository;
    private final CharacterCacheRepository characterCacheRepository;
    private final HytaleLogger logger;

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

        Player playerData = this.playerRepository.getPlayer(playerRef.getUuid());
        if (playerData == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerJoinSystem OnEntityRemove Failed: PlayerData is null");
            return;
        }

        CachedCharacter cachedCharacter = this.characterCacheRepository.getCharacterCache(playerRef.getUuid());
        if (cachedCharacter == null) return;

        Player.Character currentCharacter = playerData.getCharacters().stream()
                .filter(c -> c.getCharacterUuid().equals(cachedCharacter.getCharacterUuid()))
                .findFirst()
                .orElse(null);

        if (currentCharacter == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerJoinSystem OnEntityRemove Failed: Character is null");
            return;
        }

        currentCharacter.setExperience(playerLevelComponent.getTotalExperience());
        try {
            this.playerRepository.updatePlayer(playerData);
        } catch (Exception e) {
            this.logger.atInfo()
                    .log("[PLAYER] PlayerJoinSystem OnEntityRemove Failed: " + e.getMessage());
        }

        try {
            this.characterCacheRepository.removeCharacter(playerRef.getUuid());
        } catch (Exception e) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerJoinSystem OnEntityRemove Failed: " + e.getMessage());
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Archetype.of(PlayerRef.getComponentType());
    }
}
