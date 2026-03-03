package fr.welltale.hud.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.hud.HudBuilder;
import org.jspecify.annotations.NonNull;

public class UpdateStatsHudSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(
            float v,
            int i,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        if (!ref.isValid()) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        EntityStatMap playerStatsMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (playerStatsMap == null) return;

        EntityStatValue healthStatValue = playerStatsMap.get(DefaultEntityStatTypes.getHealth());
        if (healthStatValue == null) return;

        EntityStatValue staminaStatValue = playerStatsMap.get(DefaultEntityStatTypes.getStamina());
        if (staminaStatValue == null) return;

//        if (healthStatValue.get() == healthStatValue.getMax() && staminaStatValue.get() == staminaStatValue.getMax()) {
//            return;
//        }

        HudBuilder.update(HudBuilder.UPDATE_TYPE.STATS, player, store, ref);
    }

    @Override
    public @NonNull Query<EntityStore> getQuery() {
        return Query.and(PlayerRef.getComponentType());
    }
}
