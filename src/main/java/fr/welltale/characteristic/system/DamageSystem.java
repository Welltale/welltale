package fr.welltale.characteristic.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;
import fr.welltale.mob.MobStatsComponent;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ThreadLocalRandom;

public class DamageSystem extends EntityEventSystem<EntityStore, Damage> {
    public DamageSystem() {
        super(Damage.class);
    }

    @Override
    public void handle(
            int i,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer,
            @NonNull Damage damage
    ) {
        float damageAmount = damage.getAmount();
        if (!(damage.getSource() instanceof Damage.EntitySource damager)) return;
        Ref<EntityStore> damagerSourceRef = damager.getRef();

        if (!damagerSourceRef.isValid()) {
            return;
        }

        PlayerRef damagerPlayerRef = store.getComponent(damagerSourceRef, PlayerRef.getComponentType());
        if (damagerPlayerRef == null) {
            Ref<EntityStore> damagedRef = archetypeChunk.getReferenceTo(i);
            PlayerRef damagedPlayerRef = store.getComponent(damagedRef, PlayerRef.getComponentType());
            if (damagedPlayerRef == null) return;

            float newDamageAmount = this.applyEntityWithRefDamageToPlayer(store, damagerSourceRef, damagedRef, damageAmount);
            damage.setAmount(newDamageAmount);
            return;
        }

        Ref<EntityStore> damagedRef = archetypeChunk.getReferenceTo(i);
        if (!damagedRef.isValid()) {
            float newDamageAmount = this.applyPlayerDamageToEntity(store, damagerSourceRef, damagedRef, damageAmount);
            damage.setAmount(newDamageAmount);
            return;
        }

        PlayerRef damagedPlayerRef = store.getComponent(damagedRef, PlayerRef.getComponentType());
        if (damagedPlayerRef == null) {
            float newDamageAmount = this.applyPlayerDamageToEntity(store, damagerSourceRef, damagedRef, damageAmount);
            damage.setAmount(newDamageAmount);
            return;
        }

        float newDamageAmount = this.applyPlayerDamageToAnotherPlayer(store, damagerSourceRef, damagedRef, damageAmount);
        damage.setAmount(newDamageAmount);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    private float applyEntityWithRefDamageToPlayer(
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> entityDamagerRef,
            @NonNull Ref<EntityStore> damagedRef,
            float initialDamage
    ) {
        MobStatsComponent mobStatsComponent = store.getComponent(entityDamagerRef, MobStatsComponent.getComponentType());
        if (mobStatsComponent == null) return initialDamage;

        Characteristics.AdditionalCharacteristics additionalCharacteristicsFromDamaged = Characteristics.getAdditionalCharacteristicsFromPlayer(damagedRef, store);

        boolean isCrit = ThreadLocalRandom.current().nextFloat() < mobStatsComponent.getCriticalPct();

        if (isCrit) {
            initialDamage += mobStatsComponent.getCriticalDamage();
            initialDamage -= (additionalCharacteristicsFromDamaged.getCriticalResistance() + Characteristics.DEFAULT_CRITICAL_RESISTANCE);
        }

        return initialDamage;
    }

    private float applyPlayerDamageToAnotherPlayer(
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> damagerRef,
            @NonNull Ref<EntityStore> damagedRef,
            float initialDamage
    ) {
        Characteristics.AdditionalCharacteristics additionalCharacteristicsFromDamager = Characteristics.getAdditionalCharacteristicsFromPlayer(damagerRef, store);
        Characteristics.AdditionalCharacteristics additionalCharacteristicsFromDamaged = Characteristics.getAdditionalCharacteristicsFromPlayer(damagedRef, store);

        float newDamageAmount = initialDamage + (Characteristics.DEFAULT_DAMAGE + additionalCharacteristicsFromDamager.getDamage());

        boolean isCrit = ThreadLocalRandom.current().nextFloat() < (Characteristics.DEFAULT_CRITICAL_PCT + additionalCharacteristicsFromDamager.getCriticalPct());

        if (isCrit) {
            newDamageAmount += (additionalCharacteristicsFromDamager.getCriticalDamage() + Characteristics.DEFAULT_CRITICAL_DAMAGE);
            newDamageAmount -= (additionalCharacteristicsFromDamaged.getCriticalResistance() + Characteristics.DEFAULT_CRITICAL_RESISTANCE);
        }

        return newDamageAmount;
    }

    private float applyPlayerDamageToEntity(
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> damagerRef,
            @NonNull Ref<EntityStore> entityDamagedRef,
            float initialDamage
    ) {
        Characteristics.AdditionalCharacteristics additionalCharacteristicsFromDamager = Characteristics.getAdditionalCharacteristicsFromPlayer(damagerRef, store);
        MobStatsComponent mobStatsComponent = store.getComponent(entityDamagedRef, MobStatsComponent.getComponentType());

        float newDamageAmount = initialDamage + (additionalCharacteristicsFromDamager.getDamage() + Characteristics.DEFAULT_DAMAGE);

        boolean isCrit = ThreadLocalRandom.current().nextFloat() < (Characteristics.DEFAULT_CRITICAL_PCT + additionalCharacteristicsFromDamager.getCriticalPct());

        if (isCrit) {
            newDamageAmount += (additionalCharacteristicsFromDamager.getCriticalDamage() + Characteristics.DEFAULT_CRITICAL_DAMAGE);
            if (mobStatsComponent != null) {
                newDamageAmount -= mobStatsComponent.getCriticalResistance();
            }
        }

        if (mobStatsComponent != null) {
            newDamageAmount = newDamageAmount * (100f - mobStatsComponent.getResistancePct()) / 100f;
        }

        return newDamageAmount;
    }
}
