package fr.welltale.clazz.spell.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import fr.welltale.clazz.spell.Spell;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RainOfArrows implements Spell {
    @Override
    public InteractionType getInteractionType() {
        return InteractionType.Ability2;
    }

    @Override
    public String getName() {
        return "Rain Of Arrows";
    }

    @Override
    public String getSlug() {
        return "rain_of_arrows";
    }

    @Override
    public int getBaseDamage() {
        return 1;
    }

    @Override
    public int getManaCost() {
        return 15;
    }

    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public void run(
            @NonNull Player caster,
            @NonNull Ref<EntityStore> casterRef,
            @NonNull Store<EntityStore> casterStore,
            @NonNull Universe universe,
            @NonNull CommandBuffer<EntityStore> cmdBuffer
    ) {
        ProjectileConfig projectileConfig = ProjectileConfig.getAssetMap().getAsset("Projectile_Config_Fireball");
        if (projectileConfig == null) return;

        World world = caster.getWorld();
        if (world == null) return;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final int[] i = {0};

        Vector3d direction = TargetUtil.getLook(casterRef, casterStore).getDirection();
        direction.y += 1.6;

        TransformComponent casterTransform = casterStore.getComponent(casterRef, TransformComponent.getComponentType());
        if (casterTransform == null) return;

        world.execute(() -> {
            scheduler.scheduleWithFixedDelay(() -> {
                if (i[0] >= 10) {
                    scheduler.close();
                    return;
                }


                ProjectileModule.get().spawnProjectile(
                        casterRef,
                        cmdBuffer,
                        projectileConfig,
                        casterTransform.getPosition(),
                        direction
                );

                i[0]++;
            }, 0, 300, TimeUnit.MILLISECONDS);
        });
    }
}
