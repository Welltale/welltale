package fr.welltale.player.hud;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;
import org.jspecify.annotations.NonNull;

public class StatsHud {
    public static void buildStatsHud(
            @NonNull UICommandBuilder uiCommandBuilder,
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> ref
    ) {
        EntityStatMap playerStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (playerStatMap == null) return;

        EntityStatValue playerHealthStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_HEALTH_KEY));
        EntityStatValue playerStaminaStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_STAMINA_KEY));
        if (playerHealthStatValue == null && playerStaminaStatValue == null) return;

        uiCommandBuilder.append("Hud/Player/Stats.ui");

        if (playerHealthStatValue != null) {
            uiCommandBuilder.set("#HealthLabel.Text", (long) playerHealthStatValue.get() + " / " + (long) playerHealthStatValue.getMax());
        }

        if (playerStaminaStatValue != null) {
            uiCommandBuilder.set("#StaminaLabel.Text", (long) playerStaminaStatValue.get() + " / " + (long) playerStaminaStatValue.getMax());
        }
    }
}
