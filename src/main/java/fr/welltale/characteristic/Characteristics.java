package fr.welltale.characteristic;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.annotation.Nonnull;

public class Characteristics {
    public static final int LEVEL_UP_CHARACTERISTICS_POINTS = 3;
    public static final float MAX_ELEMENTAL_RESISTANCE = 50f;

    public static final int DEFAULT_HEALTH = 20;
    public static final int DEFAULT_WISDOM = 0;
    public static final int DEFAULT_STRENGTH = 100;
    public static final int DEFAULT_INTELLIGENCE = 0;
    public static final int DEFAULT_CHANCE = 0;
    public static final int DEFAULT_AGILITY = 0;
    public static final float DEFAULT_LIFE_REGEN_PCT = 0;
    public static final float DEFAULT_DROP_CHANCE = 0;
    public static final float DEFAULT_MOVE_SPEED = 0;
    public static final int DEFAULT_PODS = 100;
    public static final int DEFAULT_STAMINA = 20;
    public static final int DEFAULT_CRITICAL_DAMAGE = 0;
    public static final float DEFAULT_CRITICAL_PCT = 0;
    public static final int DEFAULT_CRITICAL_RESISTANCE = 0;
    public static final float DEFAULT_EARTH_RESISTANCE_PCT = 0;
    public static final float DEFAULT_FIRE_RESISTANCE_PCT = 0;
    public static final float DEFAULT_WATER_RESISTANCE_PCT = 0;
    public static final float DEFAULT_AIR_RESISTANCE_PCT = 0;

    public static final float DEFAULT_HEALTH_AMOUNT = 100;
    public static final float DEFAULT_STAMINA_AMOUNT = 10;

    public static final String STATIC_MODIFIER_HEALTH_KEY = "Health";
    public static final String STATIC_MODIFIER_WISDOM_KEY = "Wisdom";
    public static final String STATIC_MODIFIER_STRENGTH_KEY = "Strength";
    public static final String STATIC_MODIFIER_INTELLIGENCE_KEY = "Intelligence";
    public static final String STATIC_MODIFIER_CHANCE_KEY = "Chance";
    public static final String STATIC_MODIFIER_AGILITY_KEY = "Agility";
    public static final String STATIC_MODIFIER_LIFE_REGEN_PCT_KEY = "LifeRegen";
    public static final String STATIC_MODIFIER_DROP_CHANCE_KEY = "DropChance";
    public static final String STATIC_MODIFIER_MOVE_SPEED_KEY = "MoveSpeed";
    public static final String STATIC_MODIFIER_PODS_KEY = "Pods";
    public static final String STATIC_MODIFIER_STAMINA_KEY = "Stamina";
    public static final String STATIC_MODIFIER_CRITICAL_DAMAGE_KEY = "CriticalDamage";
    public static final String STATIC_MODIFIER_CRITICAL_PCT_KEY = "Critical";
    public static final String STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY = "CriticalResistance";
    public static final String STATIC_MODIFIER_EARTH_RESISTANCE_PCT_KEY = "EarthResistance";
    public static final String STATIC_MODIFIER_FIRE_RESISTANCE_PCT_KEY = "FireResistance";
    public static final String STATIC_MODIFIER_WATER_RESISTANCE_PCT_KEY = "WaterResistance";
    public static final String STATIC_MODIFIER_AIR_RESISTANCE_PCT_KEY = "AirResistance";

    public enum DamageElement {
        EARTH,
        FIRE,
        WATER,
        AIR
    }

    @Getter
    @Setter
    public static class EditableCharacteristics {
        private int health;
        private int wisdom;
        private int strength;
        private int intelligence;
        private int chance;
        private int agility;
    }

    @Getter
    @Setter
    public static class AdditionalCharacteristics {
        private int health;
        private int wisdom;
        private int strength;
        private int intelligence;
        private int chance;
        private int agility;
        private float lifeRegenPct;
        private float dropChance;
        private float moveSpeed;
        private int pods;
        private int stamina;
        private int criticalDamage;
        private float criticalPct;
        private int criticalResistance;
        private float earthResistance;
        private float fireResistance;
        private float waterResistance;
        private float airResistance;
    }

    @Nonnull
    public static AdditionalCharacteristics getAdditionalCharacteristicsFromPlayer(
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store
    ) {
        if (!ref.isValid()) {
            return new AdditionalCharacteristics();
        }

        EntityStatMap playerStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (playerStatMap == null) {
            return new AdditionalCharacteristics();
        }

        AdditionalCharacteristics additionalCharacteristics = new AdditionalCharacteristics();

        EntityStatValue healthStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_HEALTH_KEY));
        if (healthStatValue != null) {
            additionalCharacteristics.health = (int) healthStatValue.getMax();
        }

        EntityStatValue wisdomStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_WISDOM_KEY));
        if (wisdomStatValue != null) {
            additionalCharacteristics.wisdom = (int) wisdomStatValue.getMax();
        }

        EntityStatValue strengthStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_STRENGTH_KEY));
        if (strengthStatValue != null) {
            additionalCharacteristics.strength = (int) strengthStatValue.getMax();
        }

        EntityStatValue intelligenceStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_INTELLIGENCE_KEY));
        if (intelligenceStatValue != null) {
            additionalCharacteristics.intelligence = (int) intelligenceStatValue.getMax();
        }

        EntityStatValue chanceStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CHANCE_KEY));
        if (chanceStatValue != null) {
            additionalCharacteristics.chance = (int) chanceStatValue.getMax();
        }

        EntityStatValue agilityStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_AGILITY_KEY));
        if (agilityStatValue != null) {
            additionalCharacteristics.agility = (int) agilityStatValue.getMax();
        }

        EntityStatValue lifeRegenPctStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_LIFE_REGEN_PCT_KEY));
        if (lifeRegenPctStatValue != null) {
            additionalCharacteristics.lifeRegenPct = (int) lifeRegenPctStatValue.getMax();
        }

        EntityStatValue dropChanceStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_DROP_CHANCE_KEY));
        if (dropChanceStatValue != null) {
            additionalCharacteristics.dropChance = (int) dropChanceStatValue.getMax();
        }

        EntityStatValue moveSpeedStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_MOVE_SPEED_KEY));
        if (moveSpeedStatValue != null) {
            additionalCharacteristics.moveSpeed = (int) moveSpeedStatValue.getMax();
        }

        EntityStatValue podsStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_PODS_KEY));
        if (podsStatValue != null) {
            additionalCharacteristics.pods = (int) podsStatValue.getMax();
        }

        EntityStatValue staminaStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_STAMINA_KEY));
        if (staminaStatValue != null) {
            additionalCharacteristics.stamina = (int) staminaStatValue.getMax();
        }

        EntityStatValue criticalDamageStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CRITICAL_DAMAGE_KEY));
        if (criticalDamageStatValue != null) {
            additionalCharacteristics.criticalDamage = (int) criticalDamageStatValue.getMax();
        }

        EntityStatValue criticalPctStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CRITICAL_PCT_KEY));
        if (criticalPctStatValue != null) {
            additionalCharacteristics.criticalPct = criticalPctStatValue.getMax();
        }

        EntityStatValue criticalResistanceStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY));
        if (criticalResistanceStatValue != null) {
            additionalCharacteristics.criticalResistance = (int) criticalResistanceStatValue.getMax();
        }

        EntityStatValue earthResistanceStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_EARTH_RESISTANCE_PCT_KEY));
        if (earthResistanceStatValue != null) {
            additionalCharacteristics.earthResistance = (int) earthResistanceStatValue.getMax();
        }

        EntityStatValue fireResistanceStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_FIRE_RESISTANCE_PCT_KEY));
        if (fireResistanceStatValue != null) {
            additionalCharacteristics.fireResistance = (int) fireResistanceStatValue.getMax();
        }

        EntityStatValue waterResistanceStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_WATER_RESISTANCE_PCT_KEY));
        if (waterResistanceStatValue != null) {
            additionalCharacteristics.waterResistance = (int) waterResistanceStatValue.getMax();
        }

        EntityStatValue airResistanceStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_AIR_RESISTANCE_PCT_KEY));
        if (airResistanceStatValue != null) {
            additionalCharacteristics.airResistance = (int) airResistanceStatValue.getMax();
        }

        return additionalCharacteristics;
    }

    public static void setCharacteristicsToPlayer(
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull EditableCharacteristics playerEditableCharacteristics
    ) {
        if (!ref.isValid()) {
            return;
        }

        EntityStatMap playerStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (playerStatMap == null) {
            return;
        }

        StaticModifier staticModifierHealth = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                (DEFAULT_HEALTH + playerEditableCharacteristics.health) - DEFAULT_HEALTH_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getHealth(),
                staticModifierHealth.getCalculationType().createKey(STATIC_MODIFIER_HEALTH_KEY),
                staticModifierHealth
        );

        StaticModifier staticModifierWisdom = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_WISDOM
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_WISDOM_KEY),
                staticModifierWisdom.getCalculationType().createKey(STATIC_MODIFIER_WISDOM_KEY),
                staticModifierWisdom
        );

        StaticModifier staticModifierStrength = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_STRENGTH
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_STRENGTH_KEY),
                staticModifierStrength.getCalculationType().createKey(STATIC_MODIFIER_STRENGTH_KEY),
                staticModifierStrength
        );

        StaticModifier staticModifierIntelligence = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_INTELLIGENCE
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_INTELLIGENCE_KEY),
                staticModifierIntelligence.getCalculationType().createKey(STATIC_MODIFIER_INTELLIGENCE_KEY),
                staticModifierIntelligence
        );

        StaticModifier staticModifierChance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_CHANCE
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CHANCE_KEY),
                staticModifierChance.getCalculationType().createKey(STATIC_MODIFIER_CHANCE_KEY),
                staticModifierChance
        );

        StaticModifier staticModifierAgility = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_AGILITY
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_AGILITY_KEY),
                staticModifierAgility.getCalculationType().createKey(STATIC_MODIFIER_AGILITY_KEY),
                staticModifierAgility
        );

        StaticModifier staticModifierLifeRegenPct = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_LIFE_REGEN_PCT
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_LIFE_REGEN_PCT_KEY),
                staticModifierLifeRegenPct.getCalculationType().createKey(STATIC_MODIFIER_LIFE_REGEN_PCT_KEY),
                staticModifierLifeRegenPct
        );

        StaticModifier staticModifierDropChance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_DROP_CHANCE
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_DROP_CHANCE_KEY),
                staticModifierDropChance.getCalculationType().createKey(STATIC_MODIFIER_DROP_CHANCE_KEY),
                staticModifierDropChance
        );

        StaticModifier staticModifierMoveSpeed = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_MOVE_SPEED
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_MOVE_SPEED_KEY),
                staticModifierMoveSpeed.getCalculationType().createKey(STATIC_MODIFIER_MOVE_SPEED_KEY),
                staticModifierMoveSpeed
        );

        StaticModifier staticModifierPods = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_PODS
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_PODS_KEY),
                staticModifierPods.getCalculationType().createKey(STATIC_MODIFIER_PODS_KEY),
                staticModifierPods
        );

        StaticModifier staticModifierStamina = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_STAMINA - DEFAULT_STAMINA_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getStamina(),
                staticModifierStamina.getCalculationType().createKey(STATIC_MODIFIER_STAMINA_KEY),
                staticModifierStamina
        );

        StaticModifier staticModifierCriticalDamage = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_CRITICAL_DAMAGE
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CRITICAL_DAMAGE_KEY),
                staticModifierCriticalDamage.getCalculationType().createKey(STATIC_MODIFIER_CRITICAL_DAMAGE_KEY),
                staticModifierCriticalDamage
        );

        StaticModifier staticModifierCriticalPct = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_CRITICAL_PCT
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CRITICAL_PCT_KEY),
                staticModifierCriticalPct.getCalculationType().createKey(STATIC_MODIFIER_CRITICAL_PCT_KEY),
                staticModifierCriticalPct
        );

        StaticModifier staticModifierCriticalResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_CRITICAL_RESISTANCE
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY),
                staticModifierCriticalResistance.getCalculationType().createKey(STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY),
                staticModifierCriticalResistance
        );

        StaticModifier staticModifierEarthResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_EARTH_RESISTANCE_PCT
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_EARTH_RESISTANCE_PCT_KEY),
                staticModifierEarthResistance.getCalculationType().createKey(STATIC_MODIFIER_EARTH_RESISTANCE_PCT_KEY),
                staticModifierEarthResistance
        );

        StaticModifier staticModifierFireResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_FIRE_RESISTANCE_PCT
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_FIRE_RESISTANCE_PCT_KEY),
                staticModifierFireResistance.getCalculationType().createKey(STATIC_MODIFIER_FIRE_RESISTANCE_PCT_KEY),
                staticModifierFireResistance
        );

        StaticModifier staticModifierWaterResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_WATER_RESISTANCE_PCT
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_WATER_RESISTANCE_PCT_KEY),
                staticModifierWaterResistance.getCalculationType().createKey(STATIC_MODIFIER_WATER_RESISTANCE_PCT_KEY),
                staticModifierWaterResistance
        );

        StaticModifier staticModifierAirResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_AIR_RESISTANCE_PCT
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_AIR_RESISTANCE_PCT_KEY),
                staticModifierAirResistance.getCalculationType().createKey(STATIC_MODIFIER_AIR_RESISTANCE_PCT_KEY),
                staticModifierAirResistance
        );
    }
}
