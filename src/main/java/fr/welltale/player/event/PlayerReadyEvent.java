package fr.welltale.player.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.constant.Constant;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.player.Characteristics;
import fr.welltale.player.PlayerRepository;
import fr.welltale.rank.Rank;
import fr.welltale.rank.RankRepository;
import fr.welltale.util.Teleport;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class PlayerReadyEvent {
    private final PlayerRepository playerRepository;
    private final RankRepository rankRepository;
    private final HytaleLogger logger;
    private final Universe universe;

    public void onPlayerReady(@Nonnull com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent event) {
        Player player = event.getPlayer();
        Ref<EntityStore> ref = player.getReference();
        if (ref == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent OnPlayerReady Failed: Ref is null");
            return;
        }

        Store<EntityStore> store = ref.getStore();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent OnPlayerReady Failed: PlayerRef is null");
            return;
        }

        fr.welltale.player.Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent OnPlayerReady Failed: PlayerData is null");
            return;
        }

        Characteristics.setCharacteristicsToPlayer(playerRef, ref, store, playerData.getEditableCharacteristics(), this.universe);
        this.setNameplate(ref, store, playerData, playerRef);

        if (playerData.getClassUuid() != null) {
            return;
        }

        Teleport.teleportPlayerToSpawn(this.logger, this.universe, playerRef.getUuid(), ref, store, Constant.World.CelesteIslandWorld.WORLD_NAME, Constant.Particle.PLAYER_SPAWN_SPAWN);
    }

    private void setNameplate(
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull fr.welltale.player.Player playerData,
            @Nonnull PlayerRef playerRef
    ) {
        PlayerLevelComponent playerLevelComponent = store.getComponent(ref, PlayerLevelComponent.getComponentType());
        if (playerLevelComponent == null) return;

        Nameplate nameplate = store.getComponent(ref, Nameplate.getComponentType());
        if (nameplate == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent SetNameplate Failed (PlayerID: " + playerData.getUuid() + "): Nameplate is null");
            return;
        }

        if (playerData.getRankUuid() == null) {
            nameplate.setText("[" + Constant.Prefix.LEVEL_PREFIX + playerLevelComponent.getLevel() + "] " + playerRef.getUsername());
            return;
        }

        Rank playerRank = this.rankRepository.getRank(playerData.getRankUuid());
        if (playerRank == null) return;

        if (playerRank.getPermissions().contains(Constant.Permission.STAFF)) {
            nameplate.setText("[" + playerRef.getUsername() + "] [" + Constant.Prefix.LEVEL_PREFIX + playerLevelComponent.getLevel() + "]");
            return;
        }

        nameplate.setText(playerRef.getUsername() + " [" + Constant.Prefix.LEVEL_PREFIX + playerLevelComponent.getLevel() + "]");
    }
}
