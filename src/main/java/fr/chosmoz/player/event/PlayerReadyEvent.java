package fr.chosmoz.player.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.chosmoz.player.PlayerRepository;
import fr.chosmoz.rank.Rank;
import fr.chosmoz.rank.RankRepository;
import fr.chosmoz.util.Prefix;
import fr.chosmoz.util.Teleport;
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
                    .log("[PLAYER] onPlayerReadyEvent Failed: Ref is null");
            return;
        }

        Store<EntityStore> store = ref.getStore();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            this.logger.atSevere()
                    .log("[PLAYER] onPlayerReadyEvent Failed: PlayerRef is null");
            return;
        }

        try {
            fr.chosmoz.player.Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
            this.setNameplate(playerRef,ref, store, playerData);

            if (playerData.getRankUuid() != null) {
                return;
            }

            Teleport.teleportPlayerToSpawn(this.logger, this.universe, playerRef, ref, store);
        } catch (Exception e) {
            this.logger.atSevere()
                    .log("[PLAYER] onPlayerReadyEvent Failed (PlayerID: " + playerRef.getUuid() + "): " + e.getMessage());
        }
    }

    private void setNameplate(
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull fr.chosmoz.player.Player playerData
    ) {
        Nameplate nameplate = store.getComponent(ref, Nameplate.getComponentType());
        if (nameplate == null) {
            this.logger.atSevere()
                    .log("[PLAYER] onPlayerReadyEvent SetNameplate Failed (PlayerID: " + playerData.getPlayerUuid() + "): Nameplate is null");
            return;
        }

        if (playerData.getRankUuid() == null) {
            nameplate.setText("[" + Prefix.LevelPrefix + playerData.getLevel() + "] " + playerData.getPlayerName());
            return;
        }

        try {
            Rank playerRank = this.rankRepository.getRank(playerData.getRankUuid());
            nameplate.setText("[" + Prefix.LevelPrefix + playerData.getLevel() + "] " + "[" + playerRank.getPrefix() + "] " + playerData.getPlayerName());
        } catch (Exception e) {
            this.logger.atSevere()
                    .log("[PLAYER] onPlayerReadyEvent SetNameplate Failed (PlayerID: " + playerRef.getUuid() + "): Nameplate is null");
        }
    }
}
