package fr.welltale.inventory.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.inventory.CustomInventoryService;
import fr.welltale.inventory.loot.MobLootGenerator;
import fr.welltale.mob.Mob;
import fr.welltale.mob.MobRepository;
import fr.welltale.characteristic.system.DropChanceSystem;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class MobLootOnDeathSystem extends DeathSystems.OnDeathSystem {
    private final CustomInventoryService customInventoryService;
    private final MobRepository mobRepository;
    private final HytaleLogger logger;

    public MobLootOnDeathSystem(
            CustomInventoryService customInventoryService,
            MobRepository mobRepository,
            HytaleLogger logger
    ) {
        this.customInventoryService = customInventoryService;
        this.mobRepository = mobRepository;
        this.logger = logger;
    }

    @Override
    public void onComponentAdded(
            @NonNull Ref<EntityStore> ref,
            @NonNull DeathComponent deathComponent,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        try {
            if (!ref.isValid()) return;

            Damage deathInfo = deathComponent.getDeathInfo();
            if (deathInfo == null) return;

            if (!(deathInfo.getSource() instanceof Damage.EntitySource source)) return;

            Ref<EntityStore> killerRef = source.getRef();
            if (!killerRef.isValid()) return;

            PlayerRef killerPlayerRef = store.getComponent(killerRef, PlayerRef.getComponentType());
            if (killerPlayerRef == null) return;

            PersistentModel persistentModel = store.getComponent(ref, PersistentModel.getComponentType());
            String modelAssetId = persistentModel == null ? null : persistentModel.getModelReference().getModelAssetId();
            Mob mobConfig = resolveMobConfig(modelAssetId);
            if (mobConfig == null) {
                this.logger.atInfo().log("[LOOT] No mob config found for model asset: " + modelAssetId);
                return;
            }

            float dropChanceMultiplier = DropChanceSystem.getDropChanceMultiplier(killerRef, store);
            List<com.hypixel.hytale.server.core.inventory.ItemStack> loot = MobLootGenerator.rollLoot(mobConfig, dropChanceMultiplier);
            if (loot.isEmpty()) return;

            customInventoryService.addLoot(killerPlayerRef.getUuid(), loot);
            killerPlayerRef.sendMessage(Message.raw("Butin ajoute dans le panneau loot (" + loot.size() + ")"));
        } catch (Exception e) {
            this.logger.atSevere().log("[LOOT] Mob loot roll failed: " + e.getMessage());
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.any();
    }

    private Mob resolveMobConfig(String modelAssetId) {
        if (modelAssetId == null || modelAssetId.isBlank()) return null;

        Mob exact = mobRepository.getMobConfig(modelAssetId);
        if (exact != null) return exact;

        List<Mob> mobs = mobRepository.getMobsConfig();
        if (mobs == null) return null;

        String normalizedModelAsset = normalizeModelAsset(modelAssetId);
        for (Mob mob : mobs) {
            if (mob == null || mob.getModelAsset() == null) continue;

            String normalizedConfigAsset = normalizeModelAsset(mob.getModelAsset());
            if (normalizedConfigAsset.equals(normalizedModelAsset)
                    || normalizedModelAsset.contains(normalizedConfigAsset)
                    || normalizedConfigAsset.contains(normalizedModelAsset)) {
                return mob;
            }
        }

        return null;
    }

    private String normalizeModelAsset(String modelAssetId) {
        String normalized = modelAssetId.toLowerCase();
        int slashIndex = Math.max(normalized.lastIndexOf('/'), normalized.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex < normalized.length() - 1) {
            normalized = normalized.substring(slashIndex + 1);
        }

        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex > 0) {
            normalized = normalized.substring(0, dotIndex);
        }

        int colonIndex = normalized.lastIndexOf(':');
        if (colonIndex >= 0 && colonIndex < normalized.length() - 1) {
            normalized = normalized.substring(colonIndex + 1);
        }

        return normalized;
    }
}
