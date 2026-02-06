package fr.welltale.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.constant.Constant;
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
    public static final int DEFAULT_BONUS_XP = 0;

    @Getter
    @Setter
    public static class EditableCharacteristics {
        private int health;
        private int mana;
        private int stamina;
        private int bonusXP;
    }

    @Getter
    @Setter
    public static class AdditionalCharacteristics {
        private int damage;
        private int criticalDamage;
        private float criticalPct;
        private int criticalResistance;
        private int bonusXP;
    }

    @Nonnull
    public static AdditionalCharacteristics getAdditionalCharacteristicsFromPlayer(
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store
    ) {
        EntityStatMap playerStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (playerStatMap == null) {
            return new AdditionalCharacteristics();
        }

        AdditionalCharacteristics additionalCharacteristics = new AdditionalCharacteristics();

        EntityStatValue damageStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_DAMAGE_KEY));
        if (damageStatValue != null) {
            additionalCharacteristics.damage = (int) damageStatValue.getMax();
        }

        EntityStatValue criticalDamageStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_CRITICAL_DAMAGE_KEY));
        if (criticalDamageStatValue != null) {
            additionalCharacteristics.criticalDamage = (int) criticalDamageStatValue.getMax();
        }

        EntityStatValue criticalPctStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_CRITICAL_PCT_KEY));
        if (criticalPctStatValue != null) {
            additionalCharacteristics.criticalPct = criticalPctStatValue.getMax();
        }

        EntityStatValue criticalResistanceStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY));
        if (criticalResistanceStatValue != null) {
            additionalCharacteristics.criticalResistance = (int) criticalResistanceStatValue.getMax();
        }

        EntityStatValue bonusXPStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_BONUS_XP_KEY));
        if (bonusXPStatValue != null) {
            additionalCharacteristics.bonusXP = (int) bonusXPStatValue.getMax();
        }

        return additionalCharacteristics;
    }

    public static void setCharacteristicsToPlayer(
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull EditableCharacteristics playerEditableCharacteristics,
            @Nonnull Universe universe
    ) {
        EntityStatMap playerStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (playerStatMap == null) {
            universe.removePlayer(playerRef);
            return;
        }

        StaticModifier staticModifierHealth = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                (DEFAULT_HEALTH + playerEditableCharacteristics.health) - Constant.Player.Stat.DEFAULT_HEALTH_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getHealth(),
                staticModifierHealth.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_HEALTH_KEY),
                staticModifierHealth
        );

        StaticModifier staticModifierMana = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                (DEFAULT_MANA + playerEditableCharacteristics.mana) - Constant.Player.Stat.DEFAULT_MANA_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getMana(),
                staticModifierMana.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_MANA_KEY),
                staticModifierMana
        );

        StaticModifier staticModifierStamina = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                (DEFAULT_STAMINA + playerEditableCharacteristics.stamina) - Constant.Player.Stat.DEFAULT_STAMINA_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getStamina(),
                staticModifierStamina.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_STAMINA_KEY),
                staticModifierStamina
        );

        StaticModifier staticModifierDamage = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_DAMAGE
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_DAMAGE_KEY),
                staticModifierDamage.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_DAMAGE_KEY),
                staticModifierDamage
        );

        StaticModifier staticModifierCriticalDamage = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_CRITICAL_DAMAGE
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_CRITICAL_DAMAGE_KEY),
                staticModifierCriticalDamage.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_CRITICAL_DAMAGE_KEY),
                staticModifierCriticalDamage
        );

        StaticModifier staticModifierCriticalPct = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                DEFAULT_CRITICAL_PCT
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_CRITICAL_PCT_KEY),
                staticModifierCriticalPct.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_CRITICAL_PCT_KEY),
                staticModifierCriticalPct
        );

        StaticModifier staticModifierCriticalResistance = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_CRITICAL_RESISTANCE
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY),
                staticModifierCriticalResistance.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY),
                staticModifierCriticalResistance
        );

        StaticModifier staticModifierBonusXP = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                DEFAULT_BONUS_XP
        );
        playerStatMap.putModifier(
                EntityStatType.getAssetMap().getIndex(Constant.Player.Stat.STATIC_MODIFIER_BONUS_XP_KEY),
                staticModifierBonusXP.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_BONUS_XP_KEY),
                staticModifierBonusXP
        );
    }
}
