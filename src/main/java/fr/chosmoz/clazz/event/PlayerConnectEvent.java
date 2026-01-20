package fr.chosmoz.clazz.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.chosmoz.clazz.ui.ClassSelectUI;
import fr.chosmoz.player.Player;
import fr.chosmoz.player.PlayerRepository;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class PlayerConnectEvent {
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;

    public void onPlayerConnectEvent(@Nonnull com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null) {
            return;
        }

        Store<EntityStore> store = ref.getStore();
        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player == null) {
            return;
        }

        try {
            player.remove();

            Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
            if (playerData.getCls() != null) {
                return;
            }

            //TODO ADD CHECK IF CLS (CLASS) IS VALID

            PageManager pageManager = player.getPageManager();
            CustomUIPage page = pageManager.getCustomPage();
            if (page == null) {
                page = new ClassSelectUI(playerRef, CustomPageLifetime.CantClose);
                pageManager.openCustomPage(ref, store, page);
            }

        } catch (Exception e) {
            //TODO TRY IF PLAYER IS KICKED
            player.remove();
            this.logger.atSevere().log("onPlayerConnectEvent Failed (PlayerID: " + event.getPlayerRef().getUuid() + "): " + e.getMessage());
        }
    }
}