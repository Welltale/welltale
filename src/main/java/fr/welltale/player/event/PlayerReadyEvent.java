package fr.welltale.player.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;
import fr.welltale.constant.Constant;
import fr.welltale.player.PlayerRepository;
import fr.welltale.rank.Rank;
import fr.welltale.rank.RankRepository;
import fr.welltale.util.Nameplate;
import fr.welltale.util.Teleport;
import lombok.AllArgsConstructor;

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
            player.remove();
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent OnPlayerReady Failed: Ref is null");
            return;
        }

        Store<EntityStore> store = ref.getStore();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            player.remove();
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent OnPlayerReady Failed: PlayerRef is null");
            return;
        }

        fr.welltale.player.Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            player.remove();
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent OnPlayerReady Failed: PlayerData is null");
            return;
        }

        Characteristics.setCharacteristicsToPlayer(ref, store, playerData.getEditableCharacteristics());

        Rank playerRank = null;
        if (playerData.getRankUuid() != null) {
            Rank rank = this.rankRepository.getRank(playerData.getRankUuid());
            if (rank != null) {
                playerRank = rank;
            }
        }

        Nameplate.setPlayerNameplate(
                ref,
                store,
                playerData,
                playerRef,
                playerRank,
                logger
        );

        if (playerData.getClassUuid() != null) {
            return;
        }

        Teleport.teleportPlayerToSpawn(this.logger, this.universe, playerRef.getUuid(), ref, store, Constant.World.CelesteIslandWorld.WORLD_NAME, Constant.Particle.PLAYER_SPAWN_SPAWN);
    }
}
