package fr.welltale.player.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.constant.Constant;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import fr.welltale.rank.Rank;
import fr.welltale.rank.RankRepository;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class PlayerChatEvent {
    private final PlayerRepository playerRepository;
    private final RankRepository rankRepository;
    private final HytaleLogger logger;
    private final Universe universe;

    public void onPlayerChatEvent(com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        PlayerRef senderRef = event.getSender();
        if (senderRef.getWorldUuid() == null) {
            this.logger.atSevere()
                    .log("[CHAT] PlayerChatEvent ReformatMessage Failed: WorldUUID is null");
            return;
        }

        World senderWorld = universe.getWorld(senderRef.getWorldUuid());
        if (senderWorld == null) {
            this.logger.atSevere()
                    .log("[CHAT] PlayerChatEvent ReformatMessage Failed: SenderWorld is null");
            return;
        }

        Ref<EntityStore> ref = senderRef.getReference();
        if (ref == null) {
            this.logger.atSevere()
                    .log("[CHAT] PlayerChatEvent ReformatMessage Failed: SenderRef is null");
            return;
        }

        Store<EntityStore> senderStore = ref.getStore();

        this.reformatMessage(senderWorld, senderStore, senderRef, ref, event.getContent());
        event.setCancelled(true);
    }

    private void reformatMessage(
            @Nonnull World senderWorld,
            @Nonnull Store<EntityStore> store,
            @Nonnull PlayerRef senderRef,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull String content
    ) {
        if (!ref.isValid()) {
            this.logger.atSevere()
                    .log("[CHAT] PlayerChatEvent ReformatMessage Failed: Ref is invalid");
            return;
        }

        senderWorld.execute(() -> {
            PlayerLevelComponent senderLevelComponent = store.getComponent(ref, PlayerLevelComponent.getComponentType());
            if (senderLevelComponent == null) {
                this.logger.atSevere()
                        .log("[CHAT] PlayerChatEvent ReformatMessage Failed: LevelComponent is null");
                return;
            }

            Player playerData = this.playerRepository.getPlayerByUuid(senderRef.getUuid());
            if (playerData == null) {
                this.logger.atSevere()
                        .log("[CHAT] PlayerChatEvent ReformatMessage Failed: PlayerData is null");
                return;
            }

            Message level = Message.raw("[" + Constant.Prefix.LEVEL_PREFIX + senderLevelComponent.getLevel() + "] ")
                    .color(Constant.Prefix.LEVEL_PREFIX_COLOR)
                    .bold(true);

            Message author = Message.raw(senderRef.getUsername() + ": ")
                    .bold(false);

            Message message = Message.raw(content)
                    .bold(false);

            Message formattedMessage = Message.join(level, author, message);


            if (playerData.getRankUuid() != null) {
                Rank playerRank = this.rankRepository.getRankConfig(playerData.getRankUuid());
                if (playerRank == null) {
                    this.logger.atSevere()
                            .log("[CHAT] PlayerChatEvent ReformatMessage Failed: PlayerRank is null");
                    return;
                }

                author = Message.raw(senderRef.getUsername() + ": ")
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

            universe.sendMessage(formattedMessage);
        });
    }
}
