package fr.welltale.hud;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.level.hud.LevelProgressHud;
import fr.welltale.player.hud.RegionInfoHud;
import fr.welltale.player.hud.StatsHud;
import org.jspecify.annotations.NonNull;

public class PlayerHudBuilder extends CustomUIHud {
    public PlayerHudBuilder(@NonNull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@NonNull UICommandBuilder uiCommandBuilder) {
        Ref<EntityStore> ref = this.getPlayerRef().getReference();
        if (ref == null) return;

        Store<EntityStore> store = ref.getStore();

        RegionInfoHud.buildRegionInfoHud(uiCommandBuilder);
        StatsHud.buildStatsHud(uiCommandBuilder, store, ref);
        LevelProgressHud.buildLevelProgressHud(uiCommandBuilder, store, ref);
    }
}
