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

    public static final int DEFAULT_HEALTH = 10;
    public static final int DEFAULT_MANA = 10;
    public static final int DEFAULT_STAMINA = 10;
    public static final int DEFAULT_DAMAGE = 0;
    public static final int DEFAULT_CRITICAL_DAMAGE = 0;
    public static final float DEFAULT_CRITICAL_PCT = 0;
    public static final int DEFAULT_CRITICAL_RESISTANCE = 0;
    public static final float DEFAULT_BONUS_XP_PCT = 0;

    public static final float DEFAULT_STAMINA_AMOUNT = 10;
    public static final float DEFAULT_HEALTH_AMOUNT = 100;
    public static final float DEFAULT_MANA_AMOUNT = 0;

    public static final String STATIC_MODIFIER_STAMINA_KEY = "Stamina";
    public static final String STATIC_MODIFIER_HEALTH_KEY = "Health";
    public static final String STATIC_MODIFIER_MANA_KEY = "Mana";
    public static final String STATIC_MODIFIER_DAMAGE_KEY = "Damage";
    public static final String STATIC_MODIFIER_CRITICAL_DAMAGE_KEY = "CriticalDamage";
    public static final String STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY = "CriticalResistance";
    public static final String STATIC_MODIFIER_CRITICAL_PCT_KEY = "Critical";
    public static final String STATIC_MODIFIER_BONUS_XP_KEY = "BonusXP";

    @Getter
    @Setter
    public static class EditableCharacteristics {
        private int health;
        private int mana;
        private int stamina;
        private float bonusXPPct;
    }

    @Getter
    @Setter
    public static class AdditionalCharacteristics {
        private int damage;
        private int criticalDamage;
        private float criticalPct;
        private int criticalResistance;
        private float bonusXPPct;
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

        EntityStatValue damageStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_DAMAGE_KEY));
        if (damageStatValue != null) {
            additionalCharacteristics.damage = (int) damageStatValue.getMax();
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

        EntityStatValue bonusXPStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_BONUS_XP_KEY));
        if (bonusXPStatValue != null) {
            additionalCharacteristics.bonusXPPct = bonusXPStatValue.getMax();
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

        StaticModifier staticModifierMana = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                (DEFAULT_MANA + playerEditableCharacteristics.mana) - DEFAULT_MANA_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getMana(),
                staticModifierMana.getCalculationType().createKey(STATIC_MODIFIER_MANA_KEY),
                staticModifierMana
        );

        StaticModifier staticModifierStamina = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                (DEFAULT_STAMINA + playerEditableCharacteristics.stamina) - DEFAULT_STAMINA_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getStamina(),
                staticModifierStamina.getCalculationType().createKey(STATIC_MODIFIER_STAMINA_KEY),
                staticModifierStamina
        );

        StaticModifier staticModifierDamage = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_DAMAGE
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_DAMAGE_KEY),
                staticModifierDamage.getCalculationType().createKey(STATIC_MODIFIER_DAMAGE_KEY),
                staticModifierDamage
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

        StaticModifier staticModifierBonusXPPct = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_BONUS_XP_PCT
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(STATIC_MODIFIER_BONUS_XP_KEY),
                staticModifierBonusXPPct.getCalculationType().createKey(STATIC_MODIFIER_BONUS_XP_KEY),
                staticModifierBonusXPPct
        );
    }
}
