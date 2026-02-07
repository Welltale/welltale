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
import fr.welltale.level.hud.level.LevelProgress;
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

        //TODO ADD THE XP DISPATCH LOGIC IN A RADIUS AROUND THE ENTITY KILL FOR KILLER PLAYERS GROUP
        //TODO ADD XP BONUS LOGIC BASED ON THE LEVEL DIFFERENCE BETWEEN PLAYERS AND THE ENTITY
        GiveXPEvent.dispatch(killerRef, mobStatsComponent.getBaseXP());

        com.hypixel.hytale.server.core.entity.entities.Player killerPlayer = store.getComponent(killerRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (killerPlayer != null) {
            killerPlayer.getHudManager().setCustomHud(killerPlayerRef, new LevelProgress(killerPlayerRef, killerLevelComponent));
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.any();
    }
}
