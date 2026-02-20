package fr.welltale.level.handler;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import fr.welltale.characteristic.Characteristics;
import fr.welltale.constant.Constant;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.level.event.GiveXPEvent;
import fr.welltale.level.event.LevelUpEvent;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class GiveXPHandler implements Consumer<GiveXPEvent> {
    private final HytaleLogger logger;

    // Wisdom bonus to XP: 0.1% (0.001) per point of Wisdom
    private static final float WISDOM_BONUS_PER_POINT = 0.001f;

    // Cached stat index for Wisdom
    private Integer wisdomStatIndex = null;

    @Override
    public void accept(GiveXPEvent event) {
        if (!event.playerRef().isValid()) return;

        Store<EntityStore> store = event.playerRef().getStore();
        PlayerRef playerRef = store.getComponent(event.playerRef(), PlayerRef.getComponentType());
        if (playerRef == null) {
            this.logger.atSevere()
                    .log("[LEVEL] GiveXPHandler Accept Failed: PlayerUUIDComponent is null");
            return;
        }

        // Calculate XP bonus based on Wisdom
        float bonusXP = getWisdomXPBonus(event.playerRef(), store);
        long xpAmount = Math.round(event.amount() * (1.0 + bonusXP));

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

    /**
     * Gets the Wisdom XP bonus for a player.
     * Formula: wisdom * WISDOM_BONUS_PER_POINT
     *
     * @param ref The entity reference
     * @param store The entity store
     * @return Wisdom XP bonus as a multiplier (e.g., 0.1 = 10% bonus)
     */
    private float getWisdomXPBonus(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) {
            return 0.0f;
        }

        // Lazy initialization of stat index
        if (wisdomStatIndex == null) {
            wisdomStatIndex = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_WISDOM_KEY);
        }

        EntityStatValue wisdomStatValue = entityStatMap.get(wisdomStatIndex);
        if (wisdomStatValue != null) {
            return wisdomStatValue.getMax() * WISDOM_BONUS_PER_POINT;
        }
        return 0.0f;
    }
}
