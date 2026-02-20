package fr.welltale.util;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
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

    //TODO ADD MOB HP IN NAMEPLATE
    public static void setMobNameplate(
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull Mob mob,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        com.hypixel.hytale.server.core.entity.nameplate.Nameplate nameplate = new com.hypixel.hytale.server.core.entity.nameplate.Nameplate(mob.getModelAsset() + " [" + Constant.Prefix.LEVEL_PREFIX + mob.getLevel() + "]");
        com.hypixel.hytale.server.core.entity.nameplate.Nameplate mobNameplate = store.getComponent(ref, com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType());
        if (mobNameplate != null) {
            if (mobNameplate.getText().equals(nameplate.getText())) return;
            commandBuffer.removeComponent(ref, com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType());
        }

        commandBuffer.addComponent(ref, com.hypixel.hytale.server.core.entity.nameplate.Nameplate.getComponentType(), nameplate);
    }
}
