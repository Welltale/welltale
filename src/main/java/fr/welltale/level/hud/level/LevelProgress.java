package fr.welltale.level.hud.level;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import fr.welltale.level.PlayerLevelComponent;
import org.jspecify.annotations.NonNull;

public class LevelProgress extends CustomUIHud {
    private final PlayerLevelComponent playerLevelComponent;

    public LevelProgress(
            @NonNull PlayerRef playerRef,
            @NonNull PlayerLevelComponent playerLevelComponent
    ) {
        super(playerRef);
        this.playerLevelComponent = playerLevelComponent;
    }

    @Override
    protected void build(@NonNull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Hud/Level/LevelProgress.ui");
        uiCommandBuilder.set("#LevelLabel.Text", "LV." + this.playerLevelComponent.getLevel());
        uiCommandBuilder.set("#XPLabel.Text", "XP: " + this.playerLevelComponent.getCurrentLevelExp() + " / " + this.playerLevelComponent.getXPToNextLevel());
        uiCommandBuilder.set("#ProgressBar.Value", this.playerLevelComponent.getProgress());
    }
}
