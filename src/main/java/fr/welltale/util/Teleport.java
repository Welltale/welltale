package fr.welltale.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.constant.Constant;
import lombok.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class Teleport {
    public static void teleportPlayerToSpawn(
            @Nonnull HytaleLogger logger,
            @Nonnull Universe universe,
            @Nonnull UUID playerUuid,
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull String worldName,
            @Nullable String particle
    ) {
        if (!ref.isValid()) {
            logger.atSevere()
                    .log("[PLAYER] onPlayerConnectEvent TeleportPlayerToSpawn Failed (PlayerID: " + playerUuid + "): Ref is invalid");
            return;
        }

        com.hypixel.hytale.server.core.universe.world.World world = universe.getWorld(worldName);
        if (world == null) {
            logger.atSevere()
                    .log("[PLAYER] onPlayerConnectEvent TeleportPlayerToSpawn Failed (PlayerID: " + playerUuid + "): World is null");
            return;
        }

        world.execute(() -> {
            WorldConfig worldConfig = world.getWorldConfig();
            ISpawnProvider spawnProvider = worldConfig.getSpawnProvider();
            if (spawnProvider == null) {
                logger.atSevere()
                        .log("[PLAYER] onPlayerConnectEvent TeleportPlayerToSpawn Failed (PlayerID: " + playerUuid + "): Spawn Provider is null");
                return;
            }

            TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform == null) {
                logger.atSevere()
                        .log("[PLAYER] onPlayerConnectEvent TeleportPlayerToSpawn Failed (PlayerID: " + playerUuid + "): Transform is null");
                return;
            }

            Transform spawnPoint = spawnProvider.getSpawnPoint(world, worldConfig.getUuid());
            com.hypixel.hytale.server.core.modules.entity.teleport.Teleport teleport = new com.hypixel.hytale.server.core.modules.entity.teleport.Teleport(spawnPoint.getPosition(), spawnPoint.getRotation());
            store.addComponent(ref, com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType(), teleport);

            if (particle != null) {
                ParticleUtil.spawnParticleEffect(Constant.Particle.PLAYER_SPAWN_SPAWN, spawnPoint.getPosition(), store);
            }
        });
    }
}
