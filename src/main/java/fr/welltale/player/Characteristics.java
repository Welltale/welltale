package fr.welltale.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.constant.Constant;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;

//TODO ADD SETTER
@Getter
public class Characteristics {
    private final int health = 10;
    private final int mana = 20;
    private final int stamina = 10;
    private final int damage = 10; //TODO REPLACE TO 0
    private final int criticalDamage = 0;
    private final int criticalPct = 0;
    private final int criticalResistance = 0;
    private final int resistancePct = 0;

    public static void setCharacteristicToPlayer(
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @Nonnull fr.welltale.player.Player playerData,
            @Nonnull Universe universe
    ) {
        EntityStatMap playerStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (playerStatMap == null) {
            universe.removePlayer(playerRef);
            return;
        }

        //TODO CALCULATE STUFFS TO ADD MORE STATS
        StaticModifier staticModifierHealth = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                playerData.getCharacteristics().getHealth() - Constant.Player.Stat.DEFAULT_HEALTH_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getHealth(),
                staticModifierHealth.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_HEALTH_KEY),
                staticModifierHealth
        );

        StaticModifier staticModifierMana = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                playerData.getCharacteristics().getMana() - Constant.Player.Stat.DEFAULT_MANA_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getMana(),
                staticModifierMana.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_MANA_KEY),
                staticModifierMana
        );

        StaticModifier staticModifierStamina = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                playerData.getCharacteristics().getStamina() - Constant.Player.Stat.DEFAULT_STAMINA_AMOUNT
        );
        playerStatMap.putModifier(
                DefaultEntityStatTypes.getStamina(),
                staticModifierStamina.getCalculationType().createKey(Constant.Player.Stat.STATIC_MODIFIER_STAMINA_KEY),
                staticModifierStamina
        );
    }
}
