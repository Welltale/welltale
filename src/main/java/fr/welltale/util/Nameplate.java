package fr.welltale.util;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.constant.Constant;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.mob.Mob;
import fr.welltale.rank.Rank;
import lombok.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Nameplate {
    public static void setPlayerNameplate(
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull fr.welltale.player.Player playerData,
            @Nonnull PlayerRef playerRef,
            @Nullable Rank playerRank,
            @Nonnull HytaleLogger logger
    ) {
        if (!ref.isValid()) {
            logger.atSevere()
                    .log("[UTIL] Nameplate SetNameplate Failed (PlayerID: " + playerData.getUuid() + "): Ref is null");
            return;
        }

        com.hypixel.hytale.server.core.entity.nameplate.Nameplate nameplate = store.getComponent(ref, com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType());
        if (nameplate == null) {
            logger.atSevere()
                    .log("[UTIL] Nameplate SetNameplate Failed (PlayerID: " + playerData.getUuid() + "): Nameplate is null");
            return;
        }

        PlayerLevelComponent playerLevelComponent = store.getComponent(ref, PlayerLevelComponent.getComponentType());
        if (playerLevelComponent == null) {
            logger.atSevere()
                    .log("[UTIL] Nameplate SetNameplate Failed (PlayerID: " + playerData.getUuid() + "): PlayerLevelComponent is null");
            return;
        }

        if (playerData.getRankUuid() == null || playerRank == null || !playerRank.getPermissions().contains(Constant.Permission.STAFF)) {
            nameplate.setText("[" + Constant.Prefix.LEVEL_PREFIX + playerLevelComponent.getLevel() + "] " + playerRef.getUsername());
            return;
        }

        nameplate.setText("[" + playerRef.getUsername() + "] [" + Constant.Prefix.LEVEL_PREFIX + playerLevelComponent.getLevel() + "]");
    }

    public static void setMobNameplate(
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull Mob mob,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nullable Damage damage
    ) {
        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) return;

        EntityStatValue mobHealthStatValue = entityStatMap.get(DefaultEntityStatTypes.getHealth());
        if (mobHealthStatValue == null) return;

        float mobHealth = mobHealthStatValue.get();
        if (damage != null) {
            mobHealth -= damage.getAmount();
        }

        String nameplateText = "[" + Constant.Prefix.LEVEL_PREFIX + mob.getLevel() + "] " + mob.getModelAsset();

        long currentHealthValue = (long) Math.max(0, mobHealth);
        long maxHealthValue = (long) mobHealthStatValue.getMax();
        nameplateText += " [" + currentHealthValue + " / " + maxHealthValue + " HP]";

        com.hypixel.hytale.server.core.entity.nameplate.Nameplate nameplate = new com.hypixel.hytale.server.core.entity.nameplate.Nameplate(nameplateText);
        com.hypixel.hytale.server.core.entity.nameplate.Nameplate mobNameplate = store.getComponent(ref, com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType());
        if (mobNameplate != null) {
            if (mobNameplate.getText().equals(nameplate.getText())) return;
            commandBuffer.replaceComponent(ref, com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType(), nameplate);
            return;
        }

        commandBuffer.addComponent(ref, com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType(), nameplate);
    }
}
