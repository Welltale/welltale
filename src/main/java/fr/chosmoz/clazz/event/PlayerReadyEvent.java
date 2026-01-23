package fr.chosmoz.clazz.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.chosmoz.clazz.ClassRepository;
import fr.chosmoz.clazz.page.ClassSelect.ClassSelectPage;
import fr.chosmoz.player.Player;
import fr.chosmoz.player.PlayerRepository;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class PlayerReadyEvent {
    private final PlayerRepository playerRepository;
    private final ClassRepository classRepository;
    private final HytaleLogger logger;

    public void onPlayerReadyEvent(@Nonnull com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent event) {
        Ref<EntityStore> ref = event.getPlayerRef();
        Store<EntityStore> store = ref.getStore();
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player == null) {
            this.logger.atSevere()
                    .log("[CLASS] onPlayerReadyEvent Failed: Player is null");
            return;
        }

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            this.logger.atSevere()
                    .log("[CLASS] onPlayerReadyEvent Failed: PlayerRef is null");
            player.remove();
            return;
        }

        try {
            Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
            if (playerData.getClazzUuid() != null) {
                return;
            }

            ClassSelectPage page = new ClassSelectPage(playerRef, this.classRepository, this.playerRepository, this.logger);
            player.getPageManager().openCustomPage(ref, store, page);
        } catch (Exception e) {
            player.remove();
            this.logger.atSevere()
                    .log("[CLASS] onPlayerReadyEvent Failed (PlayerID: " + playerRef.getUuid() + "): " + e.getMessage());
        }
    }
}