package fr.welltale.player.hud;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
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
        if (playerHealthStatValue == null || playerStaminaStatValue == null) return;

        uiCommandBuilder.append("Hud/Player/Stats.ui");

        long currentHealthValue = (long) (playerHealthStatValue.get() > 0 ? playerHealthStatValue.get() : 0);
        uiCommandBuilder.set("#HealthLabel.Text", currentHealthValue + " / " + (long) playerHealthStatValue.getMax());

        long currentManaValue = (long) (playerStaminaStatValue.get() > 0 ? playerStaminaStatValue.get() : 0);
        uiCommandBuilder.set("#StaminaLabel.Text", currentManaValue + " / " + (long) playerStaminaStatValue.getMax());

        removeDefaultHudComponents(store, ref);
    }

    public static void updateStatsHud(
            @NonNull Player player,
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> ref
    ) {
        CustomUIHud playerHud = player.getHudManager().getCustomHud();
        if (playerHud == null) return;

        EntityStatMap playerStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (playerStatMap == null) return;

        EntityStatValue playerHealthStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_HEALTH_KEY));
        EntityStatValue playerStaminaStatValue = playerStatMap.get(EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_STAMINA_KEY));
        if (playerHealthStatValue == null || playerStaminaStatValue == null) return;

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        long currentHealthValue = (long) (playerHealthStatValue.get() > 0 ? playerHealthStatValue.get() : 0);
        uiCommandBuilder.set("#HealthLabel.Text", currentHealthValue + " / " + (long) playerHealthStatValue.getMax());

        long currentManaValue = (long) (playerStaminaStatValue.get() > 0 ? playerStaminaStatValue.get() : 0);
        uiCommandBuilder.set("#StaminaLabel.Text", currentManaValue + " / " + (long) playerStaminaStatValue.getMax());

        playerHud.update(false, uiCommandBuilder);
    }

    private static void removeDefaultHudComponents(
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> ref
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        HudManager hudManager = player.getHudManager();

        hudManager.hideHudComponents(playerRef,
                HudComponent.Mana,
//                HudComponent.Hotbar,
                HudComponent.UtilitySlotSelector,
                HudComponent.AmmoIndicator,
                HudComponent.KillFeed
        );
    }
}
