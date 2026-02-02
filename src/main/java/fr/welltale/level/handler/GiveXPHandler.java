package fr.welltale.level.handler;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.level.LevelComponent;
import fr.welltale.level.event.GiveXPEvent;
import fr.welltale.level.event.LevelUpEvent;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class GiveXPHandler implements Consumer<GiveXPEvent> {
    private final HytaleLogger logger;

    @Override
    public void accept(GiveXPEvent event) {
        if (!event.playerRef().isValid()) return;

        Store<EntityStore> store = event.playerRef().getStore();
        LevelComponent playerLevelComponent = store.getComponent(event.playerRef(), LevelComponent.getComponentType());
        if (playerLevelComponent == null) {
            this.logger.atSevere()
                    .log("[LEVEL] GiveXPHandler Accept Failed: PlayerLevelComponent is null");
            return;
        }

        int oldLevel = playerLevelComponent.getLevel();
        boolean leveledUp = playerLevelComponent.addExperience(event.amount());
        if (!leveledUp) return;

        LevelUpEvent.dispatch(event.playerRef(), oldLevel, playerLevelComponent.getLevel());
    }
}
