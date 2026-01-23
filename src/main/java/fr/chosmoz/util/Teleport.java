package fr.chosmoz.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.NonNull;

import javax.annotation.Nonnull;

public class Teleport {
    public static void teleportPlayerToSpawn(
            @Nonnull HytaleLogger logger,
            @Nonnull Universe universe,
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store) {
        World defaultWorld = universe.getDefaultWorld();
        if (defaultWorld == null) {
            logger.atSevere()
                    .log("[PLAYER] onPlayerConnectEvent TeleportPlayerToSpawn Failed (PlayerID: " + playerRef.getUuid() + "): DefaultWorld is null");
            return;
        }

        WorldConfig worldConfig = defaultWorld.getWorldConfig();
        ISpawnProvider spawnProvider = worldConfig.getSpawnProvider();
        if (spawnProvider == null) {
            logger.atSevere()
                    .log("[PLAYER] onPlayerConnectEvent TeleportPlayerToSpawn Failed (PlayerID: " + playerRef.getUuid() + "): Spawn Provider is null");
            return;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            logger.atSevere()
                    .log("[PLAYER] onPlayerConnectEvent TeleportPlayerToSpawn Failed (PlayerID: " + playerRef.getUuid() + "): Transform is null");
            return;
        }

        Transform spawnPoint = spawnProvider.getSpawnPoint(defaultWorld, worldConfig.getUuid());
        transform.getTransform().assign(spawnPoint);
        com.hypixel.hytale.server.core.modules.entity.teleport.Teleport teleport = new com.hypixel.hytale.server.core.modules.entity.teleport.Teleport(spawnPoint.getPosition(), spawnPoint.getRotation());
        store.addComponent(ref, com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType(), teleport);
    }
}
