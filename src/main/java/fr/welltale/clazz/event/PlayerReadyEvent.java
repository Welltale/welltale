package fr.welltale.clazz.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.clazz.ClassRepository;
import fr.welltale.clazz.page.ClassSelectPage;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class PlayerReadyEvent {
    private final PlayerRepository playerRepository;
    private final ClassRepository classRepository;
    private final HytaleLogger logger;

    public void onPlayerReadyEvent(@Nonnull com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent event) {
        Ref<EntityStore> ref = event.getPlayerRef();
        if (!ref.isValid()) {
            this.logger.atSevere()
                    .log("[CLASS] PlayerReadyEvent OnPlayerReadyEvent Failed: Ref is not valid");
            return;
        }

        Store<EntityStore> store = ref.getStore();
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player == null) {
            this.logger.atSevere()
                    .log("[CLASS] PlayerReadyEvent OnPlayerReadyEvent Failed: Player is null");
            return;
        }

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            this.logger.atSevere()
                    .log("[CLASS] PlayerReadyEvent OnPlayerReadyEvent Failed: PlayerRef is null");
            player.remove();
            return;
        }

        Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            this.logger.atSevere()
                    .log("[CLASS] PlayerReadyEvent OnPlayerReadyEvent Failed: PlayerData is null");
            player.remove();
            return;
        }

        if (playerData.getClassUuid() != null) {
            this.classRepository.getClass(playerData.getClassUuid());
            return;
        }

        ClassSelectPage page = new ClassSelectPage(playerRef, this.classRepository, this.playerRepository, this.logger);
        player.getPageManager().openCustomPage(ref, store, page);
    }
}