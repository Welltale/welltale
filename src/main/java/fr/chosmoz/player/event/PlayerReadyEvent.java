package fr.chosmoz.player.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.chosmoz.player.PlayerRepository;
import fr.chosmoz.rank.Rank;
import fr.chosmoz.rank.RankRepository;
import fr.chosmoz.util.Prefix;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class PlayerReadyEvent {
    private final PlayerRepository playerRepository;
    private final RankRepository rankRepository;
    private final HytaleLogger logger;

    public void onPlayerReady(@Nonnull com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent event) {
        Player player = event.getPlayer();
        this.setNameplate(player);
    }

    private void setNameplate(Player p) {
        try {
            fr.chosmoz.player.Player player = this.playerRepository.getPlayerByUsername(p.getDisplayName());

            if (player.getRankUuid() == null) {
                this.logger.atSevere().log("[PLAYER] onPlayerReadyEvent Failed (PlayerID: " + player.getPlayerUuid() + "): RankUUID is null");
                return;
            }

            Rank playerRank = this.rankRepository.getRank(player.getRankUuid());

            Ref<EntityStore> ref = p.getReference();
            if (ref == null) {
                this.logger.atSevere().log("[PLAYER] onPlayerReadyEvent Failed (PlayerID: " + player.getPlayerUuid() + "): Ref is null");
                return;
            }

            Store<EntityStore> store = ref.getStore();
            Nameplate nameplate = store.getComponent(ref, Nameplate.getComponentType());
            if (nameplate == null) {
                this.logger.atSevere().log("[PLAYER] onPlayerReadyEvent Failed (PlayerID: " + player.getPlayerUuid() + "): Nameplate is null");
                return;
            }

            nameplate.setText("[" + Prefix.LevelPrefix + player.getLevel() + "] " + "[" + playerRank.getPrefix() + "] " + p.getDisplayName());
        } catch (Exception e) {
            HytaleLogger.getLogger().atSevere()
                    .log("[PLAYER] Failed to set custom nameplate to player " + p.getDisplayName() + ": " + e.getMessage());
        }
    }
}
