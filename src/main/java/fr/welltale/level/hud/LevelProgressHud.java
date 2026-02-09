package fr.welltale.level.hud;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.constant.Constant;
import fr.welltale.level.PlayerLevelComponent;
import org.jspecify.annotations.NonNull;


public class LevelProgressHud {
    public static void buildLevelProgressHud(
            @NonNull UICommandBuilder uiCommandBuilder,
            @NonNull Store<EntityStore> store,
            @NonNull Ref<EntityStore> ref
            ) {
        PlayerLevelComponent playerLevelComponent = store.getComponent(ref, PlayerLevelComponent.getComponentType());
        if (playerLevelComponent == null) return;

        uiCommandBuilder.append("Hud/Player/Level/LevelProgress.ui");
        uiCommandBuilder.set("#LevelLabel.Text", Constant.Prefix.LEVEL_PREFIX.toUpperCase() + playerLevelComponent.getLevel());
        uiCommandBuilder.set("#XPLabel.Text", Constant.Prefix.XP_PREFIX.toUpperCase() + playerLevelComponent.getCurrentLevelExp() + " / " + playerLevelComponent.getXPToNextLevel());
        uiCommandBuilder.set("#LevelProgressBar.Value", playerLevelComponent.getProgress());
    }
}
