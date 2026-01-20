package fr.chosmoz.player.event;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import fr.chosmoz.player.Player;
import fr.chosmoz.player.PlayerRepository;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class PlayerConnectEvent {
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;

    public void onPlayerConnect(@Nonnull com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent event) {
        if (!event.getPlayerRef().isValid()) {
            return;
        }

        PlayerRef playerRef = event.getPlayerRef();

        try {
            this.sendJoinTitle(playerRef);
            this.addNewPlayerInDatabase(playerRef);
        } catch (Exception e) {
            this.logger.atSevere().log("onPlayerConnectEvent Failed (PlayerID: " + event.getPlayerRef().getUuid() + "): " + e.getMessage());
        }
    }

    private void sendJoinTitle(PlayerRef player) {
        EventTitleUtil.showEventTitleToPlayer(
                player,
                Message.raw("Bienvenue sur Chosmoz"),
                Message.raw("Saison: L’Éveil du Chosmoz"),
                true
        );
    }

    private void addNewPlayerInDatabase(PlayerRef playerRef) throws Exception {
       try {
           this.playerRepository.getPlayerByUuid(playerRef.getUuid());
       } catch (Exception e) {
           if (e.getMessage().equals(PlayerRepository.ERR_PLAYER_NOT_FOUND.getMessage())) {
               Player newPlayer = new Player(
                       playerRef.getUuid(),
                       playerRef.getUsername(),
                       null,
                       1,
                       0,
                       null
               );

               this.playerRepository.addPlayer(newPlayer);
               return;
           }

           this.logger.atSevere()
                   .log("Failed to add player " + playerRef.getUuid() + " in database: " + e.getMessage());
       }
    }
}