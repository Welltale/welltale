package fr.welltale.player.hud;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import org.jspecify.annotations.NonNull;

public class RegionInfoHud {
    public static void buildRegionInfoHud(@NonNull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Hud/Player/RegionInfo.ui");
    }
}
