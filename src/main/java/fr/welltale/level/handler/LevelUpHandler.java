package fr.welltale.level.handler;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;
import fr.welltale.constant.Constant;
import fr.welltale.level.event.LevelUpEvent;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import fr.welltale.util.Title;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class LevelUpHandler implements Consumer<LevelUpEvent> {
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;

    @Override
    public void accept(LevelUpEvent event) {
        if (!event.playerRef().isValid()) return;

        Store<EntityStore> store = event.playerRef().getStore();
        PlayerRef playerRef = store.getComponent(event.playerRef(), PlayerRef.getComponentType());
        if (playerRef == null) return;

        Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            this.logger.atSevere()
                    .log("[LEVEL] LevelUpHandler Accept Error: PlayerData is null");
            return;
        } else {
            playerData.setCharacteristicPoints(playerData.getCharacteristicPoints() + Characteristics.LEVEL_UP_CHARACTERISTICS_POINTS);
        }

        Title.sendTitle(
                playerRef,
                "Niveau " + event.newLevel() + " !",
                "Vous avez gagné " + Characteristics.LEVEL_UP_CHARACTERISTICS_POINTS * event.levelsGained() + " points de caractéristiques",
                false
        );

        SoundUtil.playSoundEvent2d(
                event.playerRef(),
                Constant.SoundIndex.LEVEL_UP,
                SoundCategory.SFX,
                store
        );

        Vector3d playerPosition = playerRef.getTransform().getPosition().clone();
        playerPosition.y += 2;

        ParticleUtil.spawnParticleEffect(
                Constant.Particle.CINEMATIC_FIREWORKS_RED_XL,
                playerPosition,
                store
        );
    }
}
