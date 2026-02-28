package fr.welltale.player.event;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@AllArgsConstructor
public class PlayerConnectEvent {
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;
    private final Universe universe;

    public void onPlayerConnect(@Nonnull com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerConnectEvent OnPlayerConnectEvent Failed: PlayerRef is null");
            return;
        }

        try {
            this.addNewPlayerToDatabase(playerRef);
        } catch (Exception e) {
            universe.removePlayer(playerRef);
            this.logger.atSevere()
                    .log("[PLAYER] PlayerConnectEvent OnPlayerConnectEvent Failed (PlayerID: " + playerRef.getUuid() + "): " + e.getMessage());
        }
    }

    private void addNewPlayerToDatabase(@Nonnull PlayerRef playerRef) throws Exception {
        Player playerData = this.playerRepository.getPlayer(playerRef.getUuid());
        if (playerData != null) {
            return;
        }

        Player newPlayer = new Player(
                playerRef.getUuid(),
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                0
        );

        this.playerRepository.addPlayer(newPlayer);
    }
}