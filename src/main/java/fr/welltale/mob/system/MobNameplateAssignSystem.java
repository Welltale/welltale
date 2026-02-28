package fr.welltale.mob.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.mob.Mob;
import fr.welltale.mob.MobRepository;
import fr.welltale.util.Nameplate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MobNameplateAssignSystem extends RefSystem<EntityStore> {
    private final MobRepository mobRepository;

    public MobNameplateAssignSystem(MobRepository mobRepository) {
        this.mobRepository = mobRepository;
    }

    @Override
    public void onEntityAdded(
            @NonNull Ref<EntityStore> ref,
            @NonNull AddReason addReason,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (!ref.isValid()) return;

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
                null
        );
    }

    @Override
    public void onEntityRemove(
            @NonNull Ref<EntityStore> ref,
            @NonNull RemoveReason removeReason,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {}

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.not(PlayerRef.getComponentType());
    }
}
