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
    public static final String STATIC_MODIFIER_STAMINA_KEY = "Stamina";
    public static final String STATIC_MODIFIER_CRITICAL_DAMAGE_KEY = "CriticalDamage";
    public static final String STATIC_MODIFIER_CRITICAL_PCT_KEY = "Critical";
    public static final String STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY = "CriticalResistance";
    public static final String STATIC_MODIFIER_EARTH_RESISTANCE_PCT_KEY = "EarthResistance";
    public static final String STATIC_MODIFIER_FIRE_RESISTANCE_PCT_KEY = "FireResistance";
    public static final String STATIC_MODIFIER_WATER_RESISTANCE_PCT_KEY = "WaterResistance";
    public static final String STATIC_MODIFIER_AIR_RESISTANCE_PCT_KEY = "AirResistance";

    private static final String MODIFIER_KEY_HEALTH = StaticModifier.CalculationType.ADDITIVE.createKey(STATIC_MODIFIER_HEALTH_KEY);
    private static final String MODIFIER_KEY_WISDOM = StaticModifier.CalculationType.ADDITIVE.createKey(STATIC_MODIFIER_WISDOM_KEY);
    private static final String MODIFIER_KEY_STRENGTH = StaticModifier.CalculationType.ADDITIVE.createKey(STATIC_MODIFIER_STRENGTH_KEY);
    private static final String MODIFIER_KEY_INTELLIGENCE = StaticModifier.CalculationType.ADDITIVE.createKey(STATIC_MODIFIER_INTELLIGENCE_KEY);
    private static final String MODIFIER_KEY_CHANCE = StaticModifier.CalculationType.ADDITIVE.createKey(STATIC_MODIFIER_CHANCE_KEY);
    private static final String MODIFIER_KEY_AGILITY = StaticModifier.CalculationType.ADDITIVE.createKey(STATIC_MODIFIER_AGILITY_KEY);
    private static final String MODIFIER_KEY_LIFE_REGEN = StaticModifier.CalculationType.MULTIPLICATIVE.createKey(STATIC_MODIFIER_LIFE_REGEN_PCT_KEY);
    private static final String MODIFIER_KEY_DROP_CHANCE = StaticModifier.CalculationType.MULTIPLICATIVE.createKey(STATIC_MODIFIER_DROP_CHANCE_KEY);
    private static final String MODIFIER_KEY_MOVE_SPEED = StaticModifier.CalculationType.MULTIPLICATIVE.createKey(STATIC_MODIFIER_MOVE_SPEED_KEY);
    private static final String MODIFIER_KEY_STAMINA = StaticModifier.CalculationType.ADDITIVE.createKey(STATIC_MODIFIER_STAMINA_KEY);
    private static final String MODIFIER_KEY_CRITICAL_DAMAGE = StaticModifier.CalculationType.ADDITIVE.createKey(STATIC_MODIFIER_CRITICAL_DAMAGE_KEY);
    private static final String MODIFIER_KEY_CRITICAL_PCT = StaticModifier.CalculationType.MULTIPLICATIVE.createKey(STATIC_MODIFIER_CRITICAL_PCT_KEY);
    private static final String MODIFIER_KEY_CRITICAL_RESISTANCE = StaticModifier.CalculationType.ADDITIVE.createKey(STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY);
    private static final String MODIFIER_KEY_EARTH_RESISTANCE = StaticModifier.CalculationType.MULTIPLICATIVE.createKey(STATIC_MODIFIER_EARTH_RESISTANCE_PCT_KEY);
    private static final String MODIFIER_KEY_FIRE_RESISTANCE = StaticModifier.CalculationType.MULTIPLICATIVE.createKey(STATIC_MODIFIER_FIRE_RESISTANCE_PCT_KEY);
    private static final String MODIFIER_KEY_WATER_RESISTANCE = StaticModifier.CalculationType.MULTIPLICATIVE.createKey(STATIC_MODIFIER_WATER_RESISTANCE_PCT_KEY);
    private static final String MODIFIER_KEY_AIR_RESISTANCE = StaticModifier.CalculationType.MULTIPLICATIVE.createKey(STATIC_MODIFIER_AIR_RESISTANCE_PCT_KEY);

    private static final class StatIndices {
        private static final int WISDOM = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_WISDOM_KEY);
        private static final int STRENGTH = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_STRENGTH_KEY);
        private static final int INTELLIGENCE = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_INTELLIGENCE_KEY);
        private static final int CHANCE = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CHANCE_KEY);
        private static final int AGILITY = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_AGILITY_KEY);
        private static final int LIFE_REGEN = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_LIFE_REGEN_PCT_KEY);
        private static final int DROP_CHANCE = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_DROP_CHANCE_KEY);
        private static final int MOVE_SPEED = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_MOVE_SPEED_KEY);
        private static final int CRITICAL_DAMAGE = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CRITICAL_DAMAGE_KEY);
        private static final int CRITICAL_PCT = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CRITICAL_PCT_KEY);
        private static final int CRITICAL_RESISTANCE = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY);
        private static final int EARTH_RESISTANCE = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_EARTH_RESISTANCE_PCT_KEY);
        private static final int FIRE_RESISTANCE = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_FIRE_RESISTANCE_PCT_KEY);
        private static final int WATER_RESISTANCE = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_WATER_RESISTANCE_PCT_KEY);
        private static final int AIR_RESISTANCE = EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_AIR_RESISTANCE_PCT_KEY);
    }

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

        EntityStatValue healthStatValue = playerStatMap.get(DefaultEntityStatTypes.getHealth());
        if (healthStatValue != null) {
            additionalCharacteristics.health = (int) healthStatValue.getMax();
        }

        EntityStatValue wisdomStatValue = playerStatMap.get(StatIndices.WISDOM);
        if (wisdomStatValue != null) {
            additionalCharacteristics.wisdom = (int) wisdomStatValue.getMax();
        }

        EntityStatValue strengthStatValue = playerStatMap.get(StatIndices.STRENGTH);
        if (strengthStatValue != null) {
            additionalCharacteristics.strength = (int) strengthStatValue.getMax();
        }

        EntityStatValue intelligenceStatValue = playerStatMap.get(StatIndices.INTELLIGENCE);
        if (intelligenceStatValue != null) {
            additionalCharacteristics.intelligence = (int) intelligenceStatValue.getMax();
        }

        EntityStatValue chanceStatValue = playerStatMap.get(StatIndices.CHANCE);
        if (chanceStatValue != null) {
            additionalCharacteristics.chance = (int) chanceStatValue.getMax();
        }

        EntityStatValue agilityStatValue = playerStatMap.get(StatIndices.AGILITY);
        if (agilityStatValue != null) {
            additionalCharacteristics.agility = (int) agilityStatValue.getMax();
        }

        EntityStatValue lifeRegenPctStatValue = playerStatMap.get(StatIndices.LIFE_REGEN);
        if (lifeRegenPctStatValue != null) {
            additionalCharacteristics.lifeRegenPct = lifeRegenPctStatValue.getMax();
        }

        EntityStatValue dropChanceStatValue = playerStatMap.get(StatIndices.DROP_CHANCE);
        if (dropChanceStatValue != null) {
            additionalCharacteristics.dropChance = (int) dropChanceStatValue.getMax();
        }

        EntityStatValue moveSpeedStatValue = playerStatMap.get(StatIndices.MOVE_SPEED);
        if (moveSpeedStatValue != null) {
            additionalCharacteristics.moveSpeed = (int) moveSpeedStatValue.getMax();
        }

        EntityStatValue staminaStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_STAMINA_KEY));
        if (staminaStatValue != null) {
            additionalCharacteristics.stamina = (int) staminaStatValue.getMax();
        }

        EntityStatValue criticalDamageStatValue = playerStatMap.get(StatIndices.CRITICAL_DAMAGE);
        if (criticalDamageStatValue != null) {
            additionalCharacteristics.criticalDamage = (int) criticalDamageStatValue.getMax();
        }

        EntityStatValue criticalPctStatValue = playerStatMap.get(StatIndices.CRITICAL_PCT);
        if (criticalPctStatValue != null) {
            additionalCharacteristics.criticalPct = criticalPctStatValue.getMax();
        }

        EntityStatValue criticalResistanceStatValue = playerStatMap.get(StatIndices.CRITICAL_RESISTANCE);
        if (criticalResistanceStatValue != null) {
            additionalCharacteristics.criticalResistance = (int) criticalResistanceStatValue.getMax();
        }

        EntityStatValue earthResistanceStatValue = playerStatMap.get(StatIndices.EARTH_RESISTANCE);
        if (earthResistanceStatValue != null) {
            additionalCharacteristics.earthResistance = (int) earthResistanceStatValue.getMax();
        }

        EntityStatValue fireResistanceStatValue = playerStatMap.get(StatIndices.FIRE_RESISTANCE);
        if (fireResistanceStatValue != null) {
            additionalCharacteristics.fireResistance = (int) fireResistanceStatValue.getMax();
        }

        EntityStatValue waterResistanceStatValue = playerStatMap.get(StatIndices.WATER_RESISTANCE);
        if (waterResistanceStatValue != null) {
            additionalCharacteristics.waterResistance = (int) waterResistanceStatValue.getMax();
        }

        EntityStatValue airResistanceStatValue = playerStatMap.get(StatIndices.AIR_RESISTANCE);
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
                MODIFIER_KEY_HEALTH,
                staticModifierHealth
        );

        StaticModifier staticModifierWisdom = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_WISDOM + playerEditableCharacteristics.wisdom
        );
        playerStatMap.putModifier(
                StatIndices.WISDOM,
                MODIFIER_KEY_WISDOM,
                staticModifierWisdom
        );

        StaticModifier staticModifierStrength = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_STRENGTH + playerEditableCharacteristics.strength
        );
        playerStatMap.putModifier(
                StatIndices.STRENGTH,
                MODIFIER_KEY_STRENGTH,
                staticModifierStrength
        );

        StaticModifier staticModifierIntelligence = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_INTELLIGENCE + playerEditableCharacteristics.intelligence
        );
        playerStatMap.putModifier(
                StatIndices.INTELLIGENCE,
                MODIFIER_KEY_INTELLIGENCE,
                staticModifierIntelligence
        );

        StaticModifier staticModifierChance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_CHANCE + playerEditableCharacteristics.chance
        );
        playerStatMap.putModifier(
                StatIndices.CHANCE,
                MODIFIER_KEY_CHANCE,
                staticModifierChance
        );

        StaticModifier staticModifierAgility = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_AGILITY + playerEditableCharacteristics.agility
        );
        playerStatMap.putModifier(
                StatIndices.AGILITY,
                MODIFIER_KEY_AGILITY,
                staticModifierAgility
        );

        StaticModifier staticModifierLifeRegenPct = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_LIFE_REGEN_PCT
        );
        playerStatMap.putModifier(
                StatIndices.LIFE_REGEN,
                MODIFIER_KEY_LIFE_REGEN,
                staticModifierLifeRegenPct
        );

        StaticModifier staticModifierDropChance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_DROP_CHANCE
        );
        playerStatMap.putModifier(
                StatIndices.DROP_CHANCE,
                MODIFIER_KEY_DROP_CHANCE,
                staticModifierDropChance
        );

        StaticModifier staticModifierMoveSpeed = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_MOVE_SPEED
        );
        playerStatMap.putModifier(
                StatIndices.MOVE_SPEED,
                MODIFIER_KEY_MOVE_SPEED,
                staticModifierMoveSpeed
        );

        StaticModifier staticModifierStamina = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_STAMINA - DEFAULT_STAMINA_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getStamina(),
                MODIFIER_KEY_STAMINA,
                staticModifierStamina
        );

        StaticModifier staticModifierCriticalDamage = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_CRITICAL_DAMAGE
        );
        playerStatMap.putModifier(
                StatIndices.CRITICAL_DAMAGE,
                MODIFIER_KEY_CRITICAL_DAMAGE,
                staticModifierCriticalDamage
        );

        StaticModifier staticModifierCriticalPct = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_CRITICAL_PCT
        );
        playerStatMap.putModifier(
                StatIndices.CRITICAL_PCT,
                MODIFIER_KEY_CRITICAL_PCT,
                staticModifierCriticalPct
        );

        StaticModifier staticModifierCriticalResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_CRITICAL_RESISTANCE
        );
        playerStatMap.putModifier(
                StatIndices.CRITICAL_RESISTANCE,
                MODIFIER_KEY_CRITICAL_RESISTANCE,
                staticModifierCriticalResistance
        );

        StaticModifier staticModifierEarthResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_EARTH_RESISTANCE_PCT
        );
        playerStatMap.putModifier(
                StatIndices.EARTH_RESISTANCE,
                MODIFIER_KEY_EARTH_RESISTANCE,
                staticModifierEarthResistance
        );

        StaticModifier staticModifierFireResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_FIRE_RESISTANCE_PCT
        );
        playerStatMap.putModifier(
                StatIndices.FIRE_RESISTANCE,
                MODIFIER_KEY_FIRE_RESISTANCE,
                staticModifierFireResistance
        );

        StaticModifier staticModifierWaterResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_WATER_RESISTANCE_PCT
        );
        playerStatMap.putModifier(
                StatIndices.WATER_RESISTANCE,
                MODIFIER_KEY_WATER_RESISTANCE,
                staticModifierWaterResistance
        );

        StaticModifier staticModifierAirResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_AIR_RESISTANCE_PCT
        );
        playerStatMap.putModifier(
                StatIndices.AIR_RESISTANCE,
                MODIFIER_KEY_AIR_RESISTANCE,
                staticModifierAirResistance
        );
    }
}
