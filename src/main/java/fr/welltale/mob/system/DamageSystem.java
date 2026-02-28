package fr.welltale.mob.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.mob.Mob;
import fr.welltale.mob.MobRepository;
import fr.welltale.mob.MobStatsComponent;
import fr.welltale.util.Nameplate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DamageSystem extends EntityEventSystem<EntityStore, Damage> {
    private final MobRepository mobRepository;

    public DamageSystem(MobRepository mobRepository) {super(Damage.class);
        this.mobRepository = mobRepository;
    }

    @Override
    public void handle(
            int i,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer,
            @NonNull Damage damage
    ) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        if (!ref.isValid()) return;

        MobStatsComponent mobStatsComponent = store.getComponent(ref, MobStatsComponent.getComponentType());
        if (mobStatsComponent == null) return;

        EntityStatMap entityStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) return;

        PersistentModel entityPersistentModel = store.getComponent(ref, PersistentModel.getComponentType());
        if (entityPersistentModel == null) return;

        String entityModelAssetId = entityPersistentModel.getModelReference().getModelAssetId();
        Mob mob = this.mobRepository.getMobConfig(entityModelAssetId);
        if (mob == null) return;

        Nameplate.setMobNameplate(
                ref,
                store,
                mob,
                commandBuffer,
                damage
        );
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        var mobStatsType = MobStatsComponent.getComponentType();
        if (mobStatsType == null) return Query.and(Query.not(PlayerRef.getComponentType()));

        return Query.and(Query.not(PlayerRef.getComponentType()), mobStatsType);
    }
}
