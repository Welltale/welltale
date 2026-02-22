package fr.welltale.level.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.level.event.GiveXPEvent;
import fr.welltale.mob.MobStatsComponent;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@AllArgsConstructor
public class OnDeathSystem extends DeathSystems.OnDeathSystem {
    private final HytaleLogger logger;

    @Override
    public void onComponentAdded(
            @NonNull Ref<EntityStore> ref,
            @NonNull DeathComponent deathComponent,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (!ref.isValid()) {
            this.logger.atSevere()
                    .log("[LEVEL] OnDeathSystem OnComponentAdded Failed: Ref is invalid");
            return;
        }

        Damage deathInfo = deathComponent.getDeathInfo();
        if (deathInfo == null) return;

        if (!(deathInfo.getSource() instanceof Damage.EntitySource source)) return;
        Ref<EntityStore> killerRef = source.getRef();
        if (!killerRef.isValid()) {
            this.logger.atSevere()
                    .log("[LEVEL] OnDeathSystem OnComponentAdded Failed: KillerRef is invalid");
            return;
        }

        PlayerRef killerPlayerRef = store.getComponent(killerRef, PlayerRef.getComponentType());
        if (killerPlayerRef == null) return;

        PlayerLevelComponent killerLevelComponent = store.getComponent(killerRef, PlayerLevelComponent.getComponentType());
        if (killerLevelComponent == null) {
            this.logger.atSevere()
                    .log("[LEVEL] OnDeathSystem OnComponentAdded Failed: KillerLevelComponent is null");
            return;
        }

        MobStatsComponent mobStatsComponent = store.getComponent(ref, MobStatsComponent.getComponentType());
        if (mobStatsComponent == null) return;

        long rewardedXP = applyLevelDifferenceXPModifier(
                mobStatsComponent.getBaseXP(),
                killerLevelComponent.getLevel(),
                mobStatsComponent.getLevel()
        );

        if (rewardedXP <= 0) return;

        // TODO: When group features are implemented, dispatch XP to eligible nearby group members.
        GiveXPEvent.dispatch(killerRef, rewardedXP);
    }

    private long applyLevelDifferenceXPModifier(
            long baseXP,
            int playerLevel,
            int mobLevel
    ) {
        if (baseXP <= 0 || playerLevel <= 0 || mobLevel <= 0) return 0;

        float multiplier = getXPLevelBalanceMultiplier(playerLevel, mobLevel);
        if (multiplier <= 0f) return 0;
        return Math.round(baseXP * multiplier);
    }

    private float getXPLevelBalanceMultiplier(int playerLevel, int mobLevel) {
        float multiplier = 1.0f;

        if (mobLevel > playerLevel + 10) {
            multiplier = (playerLevel + 10f) / mobLevel;
        } else if (playerLevel > mobLevel + 5) {
            multiplier = (float) mobLevel / playerLevel;
        }

        if (playerLevel > mobLevel * 2.5f) {
            multiplier = (float) Math.floor(mobLevel * 2.5f) / playerLevel;
        }

        return multiplier;
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.any();
    }
}
