package fr.welltale.level.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import fr.welltale.level.LevelComponent;
import fr.welltale.level.event.GiveXPEvent;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@AllArgsConstructor
public class OnDeathSystem extends DeathSystems.OnDeathSystem {
    private final HytaleLogger logger;

    private static final long XP_PER_KILL = 50L; //TODO DO THIS FLEXIBLE

    @Override
    public void onComponentAdded(
            @NonNull Ref<EntityStore> ref,
            @NonNull DeathComponent deathComponent,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
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

        LevelComponent killerLevelComponent = store.getComponent(killerRef, LevelComponent.getComponentType());
        if (killerLevelComponent == null) {
            this.logger.atSevere()
                    .log("[LEVEL] OnDeathSystem OnComponentAdded Failed: KillerLevelComponent is null");
            return;
        }


        GiveXPEvent.dispatch(killerRef, XP_PER_KILL);
        NotificationUtil.sendNotification(
                killerPlayerRef.getPacketHandler(),
                Message.raw("Vous avez gagné " + XP_PER_KILL + "XP !"),
                "Icons/Notifications/XP.png",
                NotificationStyle.Success
        );
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.any();
    }
}
