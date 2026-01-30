package fr.welltale.clazz.spell.spells;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.ExplosionConfig;
import com.hypixel.hytale.server.core.entity.ExplosionUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import fr.welltale.clazz.spell.Spell;
import fr.welltale.clazz.spell.SpellScheduler;
import fr.welltale.constant.Constant;
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
        Vector3d forward = casterLook.getDirection();

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

        final boolean[] spellCasted = {false};
        spellScheduler.schedule(() -> {
            MovementStatesComponent cMovement = store.getComponent(ref, MovementStatesComponent.getComponentType());
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
        }, () -> {
            World cWorld = caster.getWorld();
            if (cWorld == null) return;

            TransformComponent cTransform = store.getComponent(ref, TransformComponent.getComponentType());
            if (cTransform == null) return;

            caster.sendMessage(Message.raw("ON FLOOR!, EXPLODE"));
            if (casterTransform.getChunkRef() == null) return;

            ExplosionConfig explosionConfig = new ExplosionConfig();
            ExplosionUtils.performExplosion(new Damage.EntitySource(ref), casterTransform.getPosition(), explosionConfig, ref, cmdBuffer, casterTransform.getChunkRef().getStore());
        });
    }
}
