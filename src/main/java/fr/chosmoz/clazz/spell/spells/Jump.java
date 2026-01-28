package fr.chosmoz.clazz.spell.spells;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import fr.chosmoz.clazz.spell.Spell;
import fr.chosmoz.clazz.spell.SpellComponent;
import fr.chosmoz.clazz.spell.SpellScheduler;
import fr.chosmoz.constant.Constant;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class Jump implements Spell {
    public static String SLUG = "jump";

    private final SpellScheduler spellScheduler;
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
        return 10;
    }

    @Override
    public float getCooldown() {
        return 5;
    }

    @Override
    public void run(
            @NonNull Player caster,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull Universe universe,
            @Nonnull CommandBuffer<EntityStore> cmdBuffer
    ) {
        PlayerRef casterRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (casterRef == null) return;

        TransformComponent casterTransform = store.getComponent(ref, TransformComponent.getComponentType());
        if (casterTransform == null) return;

        Transform casterLook = TargetUtil.getLook(ref, store);
        Vector3d forward = casterLook.getDirection().normalize();

        Vector3d impulse = new Vector3d(
                forward.getX() * 10,
                20,
                forward.getZ() * 10
        );

        Velocity casterVelocity = store.getComponent(ref, Velocity.getComponentType());
        if (casterVelocity == null) {
            this.logger.atSevere()
                    .log("[CLASS - SPELL] Run Failed (PlayerName: " + caster.getDisplayName() + " - Spell: " + this.getName() + "): CasterVelocity is null");
            return;
        }

        casterVelocity.addInstruction(impulse, new VelocityConfig(), ChangeVelocityType.Add);
        ParticleUtil.spawnParticleEffect(Constant.Particle.BLOCK_BREAK_MUD, casterLook.getPosition(), store);
        SoundUtil.playSoundEvent2d(
                Constant.SoundIndex.SFX_BATTLEAXE_T1_SWING_CHARGED,
                SoundCategory.SFX,
                store
        );

        MovementStatesComponent movementStates = store.getComponent(ref, MovementStatesComponent.getComponentType());
        if (movementStates == null) return;

        final boolean[] spellCasted = {false};
        spellScheduler.schedule((s, b) -> {
            MovementStatesComponent cMovement = s.getComponent(ref, MovementStatesComponent.getComponentType());
            if (cMovement == null) return false;

            if (!spellCasted[0]) {
                if (!cMovement.getMovementStates().onGround) {
                    spellCasted[0] = true;
                }
            }

            caster.sendMessage(Message.raw("SPELL CASTED? : " + spellCasted[0]));

            if (!spellCasted[0]) {
                return false;
            }

            return cMovement.getMovementStates().onGround;
        }, (s, b) -> {
            TransformComponent cTransform = s.getComponent(ref, TransformComponent.getComponentType());
            if (cTransform == null) return;

            caster.sendMessage(Message.raw("ON FLOOR, CREATING ARROW..."));

            ProjectileConfig arrowFireConfig = ProjectileConfig.getAssetMap().getAsset("Projectile_Config_Arrow_Shortbow");
            if (arrowFireConfig == null) {
                caster.sendMessage(Message.raw("ARROW FIRE CONFIG IS NULL"));
                return;
            }

            Vector3d position = cTransform.getPosition().clone();
            position.y += 1.6;

            Vector3d direction = TargetUtil.getLook(ref, s).getDirection().normalize();

            ProjectileModule.get().spawnProjectile(
                    ref,
                    b,
                    arrowFireConfig,
                    position,
                    direction
            );

            caster.sendMessage(Message.raw("ARROW CREATED!"));
            cmdBuffer.removeComponent(ref, SpellComponent.getComponentType());
        });
    }
}
