package fr.welltale.characteristic.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
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
        if (!damagerSourceRef.isValid()) return;

        Ref<EntityStore> damagedRef = archetypeChunk.getReferenceTo(i);

        PlayerRef damagerPlayerRef = store.getComponent(damagerSourceRef, PlayerRef.getComponentType());
        if (damagerPlayerRef == null) {
            // Mob -> Player
            PlayerRef damagedPlayerRef = store.getComponent(damagedRef, PlayerRef.getComponentType());
            if (damagedPlayerRef == null) return;

            damage.setAmount(applyEntityDamageToPlayer(store, damagerSourceRef, damagedRef, damage, damageAmount));
            return;
        }

        PlayerRef damagedPlayerRef = store.getComponent(damagedRef, PlayerRef.getComponentType());
        if (damagedPlayerRef == null) {
            // Player -> Mob
            damage.setAmount(applyPlayerDamageToEntity(store, damagerSourceRef, damagedRef, damage, damageAmount));
            return;
        }

        // Player -> Player
        damage.setAmount(applyPlayerDamageToPlayer(store, damagerSourceRef, damagedRef, damage, damageAmount));
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    // -------------------------------------------------------------------------
    // MOB -> PLAYER
    // -------------------------------------------------------------------------
    private float applyEntityDamageToPlayer(
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> entityDamagerRef,
            @NonNull Ref<EntityStore> damagedRef,
            @NonNull Damage damage,
            float initialDamage
    ) {
        MobStatsComponent mob = store.getComponent(entityDamagerRef, MobStatsComponent.getComponentType());
        if (mob == null) return initialDamage;

        Characteristics.DamageElement element = getElementFromDamage(damage);
        Characteristics.AdditionalCharacteristics damagedStats = Characteristics.getAdditionalCharacteristicsFromPlayer(damagedRef, store);

        // Les mobs n'ont pas de stats élémentaires pour l'instant,
        // leur initialDamage est déjà fixé dans MobStatsComponent.

        // Critique
        boolean isCrit = ThreadLocalRandom.current().nextFloat() < mob.getCriticalPct() / 100f;
        if (isCrit) {
            initialDamage += mob.getCriticalDamage();
            initialDamage -= Math.max(0, damagedStats.getCriticalResistance() + Characteristics.DEFAULT_CRITICAL_RESISTANCE);
        }

        // Résistance élémentaire du joueur ciblé
        initialDamage = applyElementalResistanceFromPlayer(initialDamage, element, damagedStats);

        return Math.max(0, initialDamage);
    }

    // -------------------------------------------------------------------------
    // PLAYER -> PLAYER
    // -------------------------------------------------------------------------
    private float applyPlayerDamageToPlayer(
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> damagerRef,
            @NonNull Ref<EntityStore> damagedRef,
            @NonNull Damage damage,
            float initialDamage
    ) {
        Characteristics.DamageElement element = getElementFromDamage(damage);
        Characteristics.AdditionalCharacteristics damagerStats = Characteristics.getAdditionalCharacteristicsFromPlayer(damagerRef, store);
        Characteristics.AdditionalCharacteristics damagedStats  = Characteristics.getAdditionalCharacteristicsFromPlayer(damagedRef, store);

        // Boost élémentaire selon les stats du damager (Intelligence, Strength, etc.)
        float newDamage = applyElementalBoostFromPlayer(initialDamage, element, damagerStats);

        // Critique
        boolean isCrit = ThreadLocalRandom.current().nextFloat()
                < (Characteristics.DEFAULT_CRITICAL_PCT + damagerStats.getCriticalPct()) / 100f;
        if (isCrit) {
            newDamage += Characteristics.DEFAULT_CRITICAL_DAMAGE + damagerStats.getCriticalDamage();
            newDamage -= Math.max(0, Characteristics.DEFAULT_CRITICAL_RESISTANCE + damagedStats.getCriticalResistance());
        }

        // Résistance élémentaire du joueur ciblé
        newDamage = applyElementalResistanceFromPlayer(newDamage, element, damagedStats);

        return Math.max(0, newDamage);
    }

    // -------------------------------------------------------------------------
    // PLAYER -> MOB
    // -------------------------------------------------------------------------
    private float applyPlayerDamageToEntity(
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> damagerRef,
            @NonNull Ref<EntityStore> entityDamagedRef,
            @NonNull Damage damage,
            float initialDamage
    ) {
        Characteristics.DamageElement element = getElementFromDamage(damage);
        Characteristics.AdditionalCharacteristics damagerStats = Characteristics.getAdditionalCharacteristicsFromPlayer(damagerRef, store);
        MobStatsComponent mob = store.getComponent(entityDamagedRef, MobStatsComponent.getComponentType());

        // Boost élémentaire selon les stats du damager
        float newDamage = applyElementalBoostFromPlayer(initialDamage, element, damagerStats);

        // Critique
        boolean isCrit = ThreadLocalRandom.current().nextFloat()
                < (Characteristics.DEFAULT_CRITICAL_PCT + damagerStats.getCriticalPct()) / 100f;
        if (isCrit) {
            newDamage += Characteristics.DEFAULT_CRITICAL_DAMAGE + damagerStats.getCriticalDamage();
            if (mob != null) {
                newDamage -= Math.max(0, mob.getCriticalResistance());
            }
        }

        // Résistance élémentaire du mob ciblé
        if (mob != null) {
            newDamage = applyElementalResistanceFromMob(newDamage, element, mob);
        }

        return Math.max(0, newDamage);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    /**
     * Applique le boost élémentaire des stats du DAMAGER sur les dommages.
     *
     * Correspondances élément -> stat (comme Dofus) :
     *   FIRE  -> Intelligence
     *   EARTH -> Strength
     *   WATER -> Chance
     *   AIR   -> Agility
     *
     * Formule : damage * (1 + stat / 100)
     * Exemple : 50 Intelligence sur un spell feu à 100 de base → 100 * 1.5 = 150
     *
     * Si element est null (damage physique/neutre), aucun boost appliqué.
     */
    private float applyElementalBoostFromPlayer(
            float damage,
            Characteristics.DamageElement element,
            Characteristics.AdditionalCharacteristics stats
    ) {
        if (element == null) return damage;

        float stat = switch (element) {
            case FIRE  -> stats.getIntelligence();
            case EARTH -> stats.getStrength();
            case WATER -> stats.getChance();
            case AIR   -> stats.getAgility();
        };

        return damage * (1f + stat / 100f);
    }

    /**
     * Applique la résistance élémentaire d'un JOUEUR ciblé.
     * Formule Dofus : damage * (100 - résistance%) / 100
     * Cap à MAX_ELEMENTAL_RESISTANCE pour éviter l'immunité totale.
     *
     * Si element est null, aucune résistance appliquée.
     */
    private float applyElementalResistanceFromPlayer(
            float damage,
            Characteristics.DamageElement element,
            Characteristics.AdditionalCharacteristics stats
    ) {
        if (element == null) return damage;

        float resistancePct = switch (element) {
            case EARTH -> Characteristics.DEFAULT_EARTH_RESISTANCE_PCT + stats.getEarthResistance();
            case FIRE  -> Characteristics.DEFAULT_FIRE_RESISTANCE_PCT  + stats.getFireResistance();
            case WATER -> Characteristics.DEFAULT_WATER_RESISTANCE_PCT + stats.getWaterResistance();
            case AIR   -> Characteristics.DEFAULT_AIR_RESISTANCE_PCT   + stats.getAirResistance();
        };

        resistancePct = Math.min(resistancePct, Characteristics.MAX_ELEMENTAL_RESISTANCE);
        return damage * (100f - resistancePct) / 100f;
    }

    /**
     * Applique la résistance globale d'un MOB.
     */
    private float applyElementalResistanceFromMob(
            float damage,
            Characteristics.DamageElement element,
            MobStatsComponent mob
    ) {
        float resistancePct;

        if (element == null) {
            // Damage physique/neutre → résistance globale
            resistancePct = mob.getResistancePct();
        } else {
            resistancePct = switch (element) {
                case EARTH -> mob.getEarthResistance();
                case FIRE  -> mob.getFireResistance();
                case WATER -> mob.getWaterResistance();
                case AIR   -> mob.getAirResistance();
            };
        }

        resistancePct = Math.min(resistancePct, Characteristics.MAX_ELEMENTAL_RESISTANCE);
        return damage * (100f - resistancePct) / 100f;
    }

    private Characteristics.DamageElement getElementFromDamage(Damage damage) {
        DamageCause cause = DamageCause.getAssetMap().getAsset(damage.getDamageCauseIndex());
        if (cause == null) return null;

        return switch (cause.getId()) {
            case "Earth" -> Characteristics.DamageElement.EARTH;
            case "Fire"  -> Characteristics.DamageElement.FIRE;
            case "Water" -> Characteristics.DamageElement.WATER;
            case "Air"   -> Characteristics.DamageElement.AIR;
            default            -> null;
        };
    }
}