package fr.welltale.chat.event;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import fr.welltale.constant.Constant;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import fr.welltale.rank.Rank;
import fr.welltale.rank.RankRepository;
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

        Player playerData = this.playerRepository.getPlayerByUuid(sender.getUuid());
        if (playerData == null) {
            this.logger.atSevere()
                    .log("[CHAT] PlayerChatEvent ReformatMessage Failed: PlayerData is null");
            return;
        }

        List<PlayerRef> targets = event.getTargets();
        String content = event.getContent();

        event.setCancelled(true);

        Message level = Message.raw("[" + Constant.Prefix.LEVEL_PREFIX + playerData.getLevel() + "] ")
                .color(Constant.Prefix.LEVEL_PREFIX_COLOR)
                .bold(true);

        Message author = Message.raw(sender.getUsername() + ": ")
                .bold(false);

        Message message = Message.raw(content)
                .bold(false);

        Message formattedMessage = Message.join(level, author, message);


        if (playerData.getRankUuid() != null) {
            Rank playerRank = this.rankRepository.getRank(playerData.getRankUuid());
            if (playerRank == null) {
                this.logger.atSevere()
                        .log("[CHAT] PlayerChatEvent ReformatMessage Failed: PlayerRank is null");
                return;
            }

            author = Message.raw(sender.getUsername() + ": ")
                    .color(fr.welltale.util.Color.getColor(playerRank.getColor()))
                    .bold(false);

            message = Message.raw(content)
                    .color(fr.welltale.util.Color.getColor(playerRank.getColor()))
                    .bold(false);

            Message prefix = Message.raw("[" + playerRank.getPrefix() + "] ")
                    .color(fr.welltale.util.Color.getColor(playerRank.getColor()))
                    .bold(true);

            formattedMessage = Message.join(level, prefix, author, message);
        }

        for (PlayerRef target : targets) {
            target.sendMessage(formattedMessage);
        }
    }
}
