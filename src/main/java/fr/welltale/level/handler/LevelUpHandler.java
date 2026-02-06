package fr.welltale.level.handler;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import fr.welltale.constant.Constant;
import fr.welltale.level.event.LevelUpEvent;
import fr.welltale.player.Characteristics;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class LevelUpHandler implements Consumer<LevelUpEvent> {
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;

    @Override
    public void accept(LevelUpEvent event) {
        if (!event.playerRef().isValid()) return;

        Store<EntityStore> store = event.playerRef().getStore();
        PlayerRef playerRef = store.getComponent(event.playerRef(), PlayerRef.getComponentType());
        if (playerRef == null) return;

        Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            this.logger.atSevere()
                    .log("[LEVEL] LevelUpHandler Accept Error: PlayerData is null");
            return;
        } else {
            playerData.setCharacteristicPoints(playerData.getCharacteristicPoints() + Characteristics.LEVEL_UP_CHARACTERISTICS_POINTS);
        }

        Message title = Message.raw("Vous êtes passé niveau " + event.newLevel() + " !");
        Message subtitle = Message.raw("Vous avez gagné " + Characteristics.LEVEL_UP_CHARACTERISTICS_POINTS * event.levelsGained() + " points de caractéristiques");

        NotificationUtil.sendNotification(
                playerRef.getPacketHandler(),
                title,
                subtitle,
                "Icons/Notifications/LevelUp.png",
                NotificationStyle.Success
        );

        SoundUtil.playSoundEvent2d(
                event.playerRef(),
                Constant.SoundIndex.LEVEL_UP,
                SoundCategory.SFX,
                store
        );
    }
}
