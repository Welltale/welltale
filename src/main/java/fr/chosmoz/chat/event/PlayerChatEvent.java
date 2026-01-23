package fr.chosmoz.chat.event;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import fr.chosmoz.player.Player;
import fr.chosmoz.player.PlayerRepository;
import fr.chosmoz.rank.Rank;
import fr.chosmoz.rank.RankRepository;
import fr.chosmoz.util.Prefix;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PlayerChatEvent {
    private final PlayerRepository playerRepository;
    private final RankRepository rankRepository;
    private final HytaleLogger logger;

    public void onPlayerChatEvent(com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent event) {
        this.reformatMessage(event);
    }

    private void reformatMessage(com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        PlayerRef sender = event.getSender();

        try {
            Player player = this.playerRepository.getPlayerByUuid(sender.getUuid());

            List<PlayerRef> targets = event.getTargets();
            String content = event.getContent();

            event.setCancelled(true);

            Message level = Message.raw("[" + Prefix.LevelPrefix + player.getLevel() + "] ")
                    .color(Prefix.LevelPrefixColor)
                    .bold(true);

            Message author = Message.raw(sender.getUsername() + ": ")
                    .bold(false);

            Message message = Message.raw(content)
                    .bold(false);

            Message formattedMessage = Message.join(level, author, message);


            if (player.getRankUuid() != null) {
                Rank playerRank = this.rankRepository.getRank(player.getRankUuid());

                author = Message.raw(sender.getUsername() + ": ")
                        .color(fr.chosmoz.util.Color.getColor(playerRank.getColor()))
                        .bold(false);

                message = Message.raw(content)
                        .color(fr.chosmoz.util.Color.getColor(playerRank.getColor()))
                        .bold(false);

                Message prefix = Message.raw("[" + playerRank.getPrefix() + "] ")
                        .color(fr.chosmoz.util.Color.getColor(playerRank.getColor()))
                        .bold(true);

                formattedMessage = Message.join(level, prefix, author, message);
            }

            for (PlayerRef target : targets) {
                target.sendMessage(formattedMessage);
            }
        } catch (Exception e) {
            this.logger.atSevere()
                    .log("[CHAT] onPlayerChatEvent reformatMessage Failed (PlayerID: " + sender.getUuid() + "): " + e.getMessage());
        }
    }
}
