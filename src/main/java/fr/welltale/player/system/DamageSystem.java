package fr.welltale.player.system;

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
import fr.welltale.player.Characteristics;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import org.jspecify.annotations.NonNull;

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
        com.hypixel.hytale.server.core.entity.entities.Player damager = store.getComponent(damageSourceRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (damager == null) return;

        UUIDComponent damagerUuidComponent = store.getComponent(damageSourceRef, UUIDComponent.getComponentType());
        if (damagerUuidComponent == null) return;

        Player damagerData = this.playerRepository.getPlayerByUuid(damagerUuidComponent.getUuid());
        if (damagerData == null) return;

        Ref<EntityStore> damagedRef = archetypeChunk.getReferenceTo(i);
        PlayerRef damagedPlayerRef = store.getComponent(damagedRef, PlayerRef.getComponentType());

        float damageAmount = damage.getAmount();
        if (damagedPlayerRef == null) {
            float newDamageAmount = this.applyDamageWithoutDamagedPlayer(damagerData.getCharacteristics(), damageAmount);
            damage.setAmount(newDamageAmount);

            damager.sendMessage(Message.raw("DAMAGE AMOUNT: " + damageAmount));
            damager.sendMessage(Message.raw("NEW DAMAGE AMOUNT: " + newDamageAmount));
            return;
        }

        Player damagedPlayerData = this.playerRepository.getPlayerByUuid(damagedPlayerRef.getUuid());
        if (damagedPlayerData == null) {
            float newDamageAmount = this.applyDamageWithoutDamagedPlayer(damagerData.getCharacteristics(), damageAmount);
            damage.setAmount(newDamageAmount);

            damager.sendMessage(Message.raw("DAMAGE AMOUNT: " + damageAmount));
            damager.sendMessage(Message.raw("NEW DAMAGE AMOUNT: " + newDamageAmount));
            return;
        }

        float newDamageAmount = this.applyDamageWithDamagedPlayer(damagerData.getCharacteristics(), damagedPlayerData.getCharacteristics(), damageAmount);

        damager.sendMessage(Message.raw("DAMAGE AMOUNT: " + damageAmount));
        damager.sendMessage(Message.raw("NEW DAMAGE AMOUNT: " + newDamageAmount));
        damage.setAmount(newDamageAmount);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    private float applyDamageWithDamagedPlayer(
            @NonNull Characteristics damagerCharacteristics,
            @NonNull Characteristics damagedPlayerCharacteristics,
            float initialDamage
    ) {
        float newDamageAmount = initialDamage + damagerCharacteristics.getDamage();

        boolean isCrit = ThreadLocalRandom.current().nextFloat() < damagerCharacteristics.getCriticalPct();

        if (isCrit) {
            newDamageAmount += damagerCharacteristics.getCriticalDamage();
            newDamageAmount -= damagedPlayerCharacteristics.getCriticalResistance();
        }

        newDamageAmount = newDamageAmount * (100f - damagedPlayerCharacteristics.getResistancePct()) / 100f;
        return newDamageAmount;
    }

    private float applyDamageWithoutDamagedPlayer(
            @NonNull Characteristics damagerCharacteristics,
            float initialDamage
    ) {
        float newDamageAmount = initialDamage + damagerCharacteristics.getDamage();

        boolean isCrit = ThreadLocalRandom.current().nextFloat() < damagerCharacteristics.getCriticalPct();

        if (isCrit) {
            newDamageAmount += damagerCharacteristics.getCriticalDamage();
        }

        return newDamageAmount;
    }
}
