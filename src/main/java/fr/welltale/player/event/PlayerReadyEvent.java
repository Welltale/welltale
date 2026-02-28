package fr.welltale.player.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.clazz.ClassRepository;
import fr.welltale.constant.Constant;
import fr.welltale.player.PlayerRepository;
import fr.welltale.player.charactercache.CharacterCacheRepository;
import fr.welltale.player.page.CharacterSelectPage;
import fr.welltale.rank.RankRepository;
import fr.welltale.util.Teleport;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class PlayerReadyEvent {
    private final PlayerRepository playerRepository;
    private final RankRepository rankRepository;
    private final CharacterCacheRepository characterCacheRepository;
    private final ClassRepository classRepository;
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

        fr.welltale.player.Player playerData = this.playerRepository.getPlayer(playerRef.getUuid());
        if (playerData == null) {
            player.remove();
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent OnPlayerReady Failed: PlayerData is null");
            return;
        }

        player.getPageManager().openCustomPage(ref, store, new CharacterSelectPage(
                playerRef,
                this.playerRepository,
                this.characterCacheRepository,
                this.rankRepository,
                this.classRepository,
                this.universe,
                this.logger
        ));

        //TODO TELEPORT TO SELECT CLASS SPAWN
        Teleport.teleportPlayerToSpawn(this.logger, this.universe, playerRef.getUuid(), ref, store, Constant.World.CelesteIslandWorld.WORLD_NAME, Constant.Particle.PLAYER_SPAWN_SPAWN);
    }
}
