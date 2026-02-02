package fr.welltale.player.event;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import fr.welltale.player.Characteristics;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;

@AllArgsConstructor
public class PlayerConnectEvent {
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;

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
            this.logger.atSevere()
                    .log("[PLAYER] PlayerConnectEvent OnPlayerConnectEvent Failed (PlayerID: " + playerRef.getUuid() + "): " + e.getMessage());
        }
    }

    private void addNewPlayerToDatabase(@Nonnull PlayerRef playerRef) throws Exception {
        Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData != null) {
            return;
        }

        Player newPlayer = new Player(
                playerRef.getUuid(),
                playerRef.getUsername(),
                null,
                0,
                null,
                null,
                List.of(),
                null,
                new Characteristics()
        );

        this.playerRepository.addPlayer(newPlayer);
    }
}