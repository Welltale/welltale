package fr.chosmoz.clazz.spell.spells;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.chosmoz.clazz.spell.Spell;
import fr.chosmoz.constant.Constant;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

@AllArgsConstructor
public class Jump implements Spell {
    public static String SLUG = "jump";

    private HytaleLogger logger;

    @Override
    public InteractionType getInteractionType() {
        return InteractionType.Ability1;
    }

    @Override
    public String getName() {
        return "Bond";
    }

    @Override
    public String getSlug() {
        return SLUG;
    }

    @Override
    public int getBaseDamage() {
        return 5;
    }

    @Override
    public int getManaCost() {
        return 5;
    }

    @Override
    public float getCooldown() {
        return 5;
    }

    @Override
    public float getRange() {
        return 10;
    }

    @Override
    public void run(@NonNull Player caster) {
        Ref<EntityStore> casterRef = caster.getReference();
        if (casterRef == null) {
            this.logger.atSevere()
                    .log("[CLASS - SPELL] Run Failed (PlayerName: " + caster.getDisplayName() + " - Spell: " + this.getName() + "): CasterRef is null");
            return;
        }

        Store<EntityStore> casterStore = casterRef.getStore();
        TransformComponent casterTransform = casterStore.getComponent(casterRef, TransformComponent.getComponentType());
        if (casterTransform == null) {
            this.logger.atSevere()
                    .log("[CLASS - SPELL] Run Failed (PlayerName: " + caster.getDisplayName() + " - Spell: " + this.getName() + "): CasterTransform is null");
            return;
        }

        Vector3d casterPosition = casterTransform.getPosition();
        Vector3f casterRotation = casterTransform.getRotation();

        double yaw = Math.toRadians(casterRotation.getY());
        double pitch = Math.toRadians(casterRotation.getX());

        double x = -Math.cos(pitch) * Math.sin(yaw);
        double y = -Math.sin(pitch);
        double z =  Math.cos(pitch) * Math.cos(yaw);

        Vector3d forward = new Vector3d(x, y, z).normalize();
        Vector3d impulse = new Vector3d(
                forward.getX() * 60.0,
                30.0,
                forward.getZ() * 60.0
        );

        Velocity casterVelocity = casterStore.getComponent(casterRef, Velocity.getComponentType());
        if (casterVelocity == null) {
            this.logger.atSevere()
                    .log("[CLASS - SPELL] Run Failed (PlayerName: " + caster.getDisplayName() + " - Spell: " + this.getName() + "): CasterVelocity is null");
            return;
        }

        casterVelocity.addInstruction(impulse, new VelocityConfig(), ChangeVelocityType.Add);
        ParticleUtil.spawnParticleEffect(Constant.Particle.PLAYER_SPAWN_SPAWN, casterPosition, casterStore);
    }
}
