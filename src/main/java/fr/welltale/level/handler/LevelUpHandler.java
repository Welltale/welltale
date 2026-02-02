package fr.welltale.level.handler;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import fr.welltale.level.event.LevelUpEvent;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class LevelUpHandler implements Consumer<LevelUpEvent> {
    private final HytaleLogger logger;

    @Override
    public void accept(LevelUpEvent event) {
        if (!event.playerRef().isValid()) return;

        Store<EntityStore> store = event.playerRef().getStore();
        PlayerRef playerRef = store.getComponent(event.playerRef(), PlayerRef.getComponentType());
        if (playerRef == null) return;

        //TODO REPLACE THIS TO DO SOMETHING LIKE DOFUS
        NotificationUtil.sendNotification(
                playerRef.getPacketHandler(),
                Message.raw("Vous êtes passé niveau " + event.newLevel() + " !"),
                "Icons/Notifications/LevelUp.png",
                NotificationStyle.Success
        );
    }
}
