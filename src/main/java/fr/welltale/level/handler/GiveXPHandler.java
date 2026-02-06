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
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.level.event.GiveXPEvent;
import fr.welltale.level.event.LevelUpEvent;
import fr.welltale.player.Characteristics;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class GiveXPHandler implements Consumer<GiveXPEvent> {
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;

    @Override
    public void accept(GiveXPEvent event) {
        if (!event.playerRef().isValid()) return;

        Store<EntityStore> store = event.playerRef().getStore();

        var bonusXP = Characteristics.DEFAULT_BONUS_XP;
        PlayerRef playerRef = store.getComponent(event.playerRef(), PlayerRef.getComponentType());
        if (playerRef == null) {
            this.logger.atSevere()
                    .log("[LEVEL] GiveXPHandler Accept Failed: PlayerUUIDComponent is null");
            return;
        }

        Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData != null) {
            bonusXP += playerData.getEditableCharacteristics().getBonusXP();
        }

        Characteristics.AdditionalCharacteristics additionalCharacteristicsFromPlayer = Characteristics.getAdditionalCharacteristicsFromPlayer(event.playerRef(), store);

        bonusXP += additionalCharacteristicsFromPlayer.getBonusXP();

        long xpAmount = event.amount();
        xpAmount = xpAmount + (xpAmount * bonusXP / 100);

        PlayerLevelComponent playerLevelComponent = store.getComponent(event.playerRef(), PlayerLevelComponent.getComponentType());
        if (playerLevelComponent == null) {
            this.logger.atSevere()
                    .log("[LEVEL] GiveXPHandler Accept Failed: PlayerLevelComponent is null");
            return;
        }

        int oldLevel = playerLevelComponent.getLevel();
        boolean leveledUp = playerLevelComponent.addExperience(xpAmount);
        NotificationUtil.sendNotification(
                playerRef.getPacketHandler(),
                Message.raw("Vous avez gagné " + xpAmount + "XP !"),
                "Icons/Notifications/XP.png",
                NotificationStyle.Success
        );

        SoundUtil.playSoundEvent2d(
                event.playerRef(),
                Constant.SoundIndex.XP_GAINED,
                SoundCategory.SFX,
                store
        );

        if (!leveledUp) return;
        LevelUpEvent.dispatch(event.playerRef(), oldLevel, playerLevelComponent.getLevel());
    }
}
