package fr.welltale.clazz.spell.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.player.Characteristic;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import org.jspecify.annotations.NonNull;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class DamageSystem extends EntityEventSystem<EntityStore, Damage> {
    private final PlayerRepository playerRepository;

    public DamageSystem(@NonNull PlayerRepository playerRepository) {
        super(Damage.class);
        this.playerRepository = playerRepository;
    }

    @Override
    public void handle(
            int i,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer,
            @NonNull Damage damage
    ) {
        if (!(damage.getSource() instanceof Damage.EntitySource casterEntity)) return;
        Ref<EntityStore> damageSourceRef = casterEntity.getRef();
        com.hypixel.hytale.server.core.entity.entities.Player caster = store.getComponent(damageSourceRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (caster == null) return;

        UUIDComponent damageSourceUuidComponent = store.getComponent(damageSourceRef, UUIDComponent.getComponentType());
        if (damageSourceUuidComponent == null) return;

        Player casterData = this.playerRepository.getPlayerByUuid(damageSourceUuidComponent.getUuid());
        if (casterData == null) return;

        Ref<EntityStore> damagedRef = archetypeChunk.getReferenceTo(i);
        PlayerRef damagedPlayerRef = store.getComponent(damagedRef, PlayerRef.getComponentType());

        float damageAmount = damage.getAmount();
        if (damagedPlayerRef == null) {
            float newDamageAmount = this.applyDamageWithoutDamagedPlayer(casterData.getCharacteristic(), damageAmount);
            damage.setAmount(newDamageAmount);

            caster.sendMessage(Message.raw("DAMAGE AMOUNT: " + damageAmount));
            caster.sendMessage(Message.raw("NEW DAMAGE AMOUNT: " + newDamageAmount));
            return;
        }

        Player damagedPlayerData = this.playerRepository.getPlayerByUuid(damagedPlayerRef.getUuid());
        if (damagedPlayerData == null) {
            float newDamageAmount = this.applyDamageWithoutDamagedPlayer(casterData.getCharacteristic(), damageAmount);
            damage.setAmount(newDamageAmount);

            caster.sendMessage(Message.raw("DAMAGE AMOUNT: " + damageAmount));
            caster.sendMessage(Message.raw("NEW DAMAGE AMOUNT: " + newDamageAmount));
            return;
        }

        float newDamageAmount = this.applyDamageWithDamagedPlayer(casterData.getCharacteristic(), damagedPlayerData.getCharacteristic(), damageAmount);

        caster.sendMessage(Message.raw("DAMAGE AMOUNT: " + damageAmount));
        caster.sendMessage(Message.raw("NEW DAMAGE AMOUNT: " + newDamageAmount));
        damage.setAmount(newDamageAmount);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    private float applyDamageWithDamagedPlayer(
            @NonNull Characteristic casterCharacteristic,
            @NonNull Characteristic damagedPlayerCharacteristic,
            float damageAmount
    ) {
        float newDamageAmount = damageAmount + casterCharacteristic.getDamage();

        boolean isCrit = ThreadLocalRandom.current().nextFloat() < casterCharacteristic.getCriticalPct();

        if (isCrit) {
            newDamageAmount += casterCharacteristic.getCriticalDamage();
            newDamageAmount -= damagedPlayerCharacteristic.getCriticalResistance();
        }

        newDamageAmount = newDamageAmount * (100f - damagedPlayerCharacteristic.getResistancePct()) / 100f;
        return newDamageAmount;
    }

    private float applyDamageWithoutDamagedPlayer(
            @NonNull Characteristic casterCharacteristic,
            float damageAmount
    ) {
        float newDamageAmount = damageAmount + casterCharacteristic.getDamage();

        Random random = new Random();
        boolean isCrit = random.nextFloat() < casterCharacteristic.getCriticalPct();

        if (isCrit) {
            newDamageAmount += casterCharacteristic.getCriticalDamage();
        }

        return newDamageAmount;
    }
}
