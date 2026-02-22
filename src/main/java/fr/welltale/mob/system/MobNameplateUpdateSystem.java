package fr.welltale.mob.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.mob.Mob;
import fr.welltale.mob.MobRepository;
import fr.welltale.mob.MobStatsComponent;
import fr.welltale.util.Nameplate;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@AllArgsConstructor
public class MobNameplateUpdateSystem extends EntityTickingSystem<EntityStore> {
    private final MobRepository mobRepository;

    @Override
    public void tick(
            float dt,
            int index,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        MobStatsComponent mobStatsComponent = store.getComponent(ref, MobStatsComponent.getComponentType());
        if (mobStatsComponent == null) return;

        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) return;

        EntityStatValue mobHealthStatValue = entityStatMap.get(DefaultEntityStatTypes.getHealth());
        if (mobHealthStatValue == null) return;

        PersistentModel entityPersistentModel = store.getComponent(ref, PersistentModel.getComponentType());
        if (entityPersistentModel == null) return;

        String entityModelAssetId = entityPersistentModel.getModelReference().getModelAssetId();
        Mob mob = this.mobRepository.getMobConfig(entityModelAssetId);
        if (mob == null) return;

        Nameplate.setMobNameplate(
                ref,
                store,
                mob,
                commandBuffer
        );
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(
                Query.not(PlayerRef.getComponentType())
        );
    }
}
