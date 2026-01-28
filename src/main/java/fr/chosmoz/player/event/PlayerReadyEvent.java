package fr.chosmoz.player.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.chosmoz.constant.Constant;
import fr.chosmoz.player.Characteristic;
import fr.chosmoz.player.PlayerRepository;
import fr.chosmoz.rank.Rank;
import fr.chosmoz.rank.RankRepository;
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

        fr.chosmoz.player.Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent OnPlayerReady Failed: PlayerData is null");
            return;
        }

        Characteristic.setCharacteristicToPlayer(playerRef, ref, store, playerData, this.universe);
        this.setNameplate(ref, store, playerData);

        if (playerData.getClassUuid() != null) {
            return;
        }

        Teleport.teleportPlayerToSpawn(this.logger, this.universe, playerRef.getUuid(), ref, store, Constant.World.CelesteIslandWorld.WORLD_NAME, Constant.Particle.PLAYER_SPAWN_SPAWN);
    }

    private void setNameplate(
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull fr.chosmoz.player.Player playerData
    ) {
        Nameplate nameplate = store.getComponent(ref, Nameplate.getComponentType());
        if (nameplate == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent SetNameplate Failed (PlayerID: " + playerData.getUuid() + "): Nameplate is null");
            return;
        }

        if (playerData.getRankUuid() == null) {
            nameplate.setText("[" + Constant.Prefix.LEVEL_PREFIX + playerData.getLevel() + "] " + playerData.getPlayerName());
            return;
        }

        Rank playerRank = this.rankRepository.getRank(playerData.getRankUuid());
        if (playerRank == null) return;

        nameplate.setText("[" + Constant.Prefix.LEVEL_PREFIX + playerData.getLevel() + "] " + "[" + playerRank.getPrefix() + "] " + playerData.getPlayerName());
    }
}
