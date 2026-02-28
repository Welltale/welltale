package fr.welltale.player.page;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;
import fr.welltale.clazz.ClassRepository;
import fr.welltale.clazz.page.ClassSelectPage;
import fr.welltale.constant.Constant;
import fr.welltale.hud.PlayerHudBuilder;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.level.XPTable;
import fr.welltale.player.PlayerRepository;
import fr.welltale.player.charactercache.CachedCharacter;
import fr.welltale.player.charactercache.CharacterCacheRepository;
import fr.welltale.rank.Rank;
import fr.welltale.rank.RankRepository;
import fr.welltale.util.Nameplate;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CharacterSelectPage extends InteractiveCustomUIPage<CharacterSelectPage.CharacterSelectPageEventData> {
    private static final int SLOT_COUNT = 6;
    private static final String ACTION_DISCONNECT = "DISCONNECT";
    private static final String ACTION_SLOT = "SLOT";

    private final PlayerRef playerRef;
    private final PlayerRepository playerRepository;
    private final RankRepository rankRepository;
    private final ClassRepository classRepository;
    private final CharacterCacheRepository characterCacheRepository;
    private final Universe universe;
    private final HytaleLogger logger;

    public static class CharacterSelectPageEventData {
        public String action;
        public String slotIndex;

        public static final BuilderCodec<CharacterSelectPageEventData> CODEC =
                BuilderCodec.builder(CharacterSelectPageEventData.class, CharacterSelectPageEventData::new)
                        .append(
                                new KeyedCodec<>("Action", Codec.STRING),
                                (CharacterSelectPageEventData obj, String val) -> obj.action = val,
                                (CharacterSelectPageEventData obj) -> obj.action
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("SlotIndex", Codec.STRING),
                                (CharacterSelectPageEventData obj, String val) -> obj.slotIndex = val,
                                (CharacterSelectPageEventData obj) -> obj.slotIndex
                        )
                        .add()
                        .build();
    }

    public CharacterSelectPage(
            @NonNull PlayerRef playerRef,
            @NonNull PlayerRepository playerRepository,
            @NonNull CharacterCacheRepository characterCacheRepository,
            @NonNull RankRepository rankRepository,
            @NonNull ClassRepository classRepository, Universe universe,
            @NonNull HytaleLogger logger
    ) {
        super(playerRef, CustomPageLifetime.CantClose, CharacterSelectPageEventData.CODEC);
        this.playerRef = playerRef;
        this.playerRepository = playerRepository;
        this.rankRepository = rankRepository;
        this.classRepository = classRepository;
        this.characterCacheRepository = characterCacheRepository;
        this.universe = universe;
        this.logger = logger;
    }

    @Override
    public void build(@NonNull Ref<EntityStore> ref, @NonNull UICommandBuilder cmd, @NonNull UIEventBuilder event, @NonNull Store<EntityStore> store) {
        if (!ref.isValid()) {
            this.logger.atSevere().log("[PLAYER] CharacterSelectPage Build Failed: Ref is invalid");
            return;
        }

        cmd.append("Pages/Player/CharacterSelectPage.ui");
        event.addEventBinding(CustomUIEventBindingType.Activating, "#DisconnectButton", EventData.of("Action", ACTION_DISCONNECT));
        for (int i = 0; i < SLOT_COUNT; i++) {
            String selector = "#CharacterSlot" + (i + 1) + "Button";
            EventData data = new EventData().append("Action", ACTION_SLOT).append("SlotIndex", String.valueOf(i));
            event.addEventBinding(CustomUIEventBindingType.Activating, selector, data, false);
        }

        applyState(cmd, ref, store);
    }

    @Override
    public void handleDataEvent(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @NonNull CharacterSelectPageEventData data
    ) {
        if (!ref.isValid()) {
            this.logger.atSevere().log("[PLAYER] CharacterSelectPage HandleDataEvent Failed: Ref is invalid");
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            this.logger.atSevere().log("[PLAYER] CharacterSelectPage HandleDataEvent Failed: Player is null");
            return;
        }

        if (Objects.equals(data.action, ACTION_DISCONNECT)) {
            player.remove();
            return;
        }

        if (!Objects.equals(data.action, ACTION_SLOT)) {
            this.sendUpdate();
            return;
        }

        int slotIndex = parseSlotIndex(data.slotIndex);
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) {
            this.sendUpdate();
            return;
        }

        handleSlotSelection(ref, store, player, slotIndex);
    }

    private void handleSlotSelection(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @NonNull Player player,
            int slotIndex
    ) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            this.logger.atSevere().log("[PLAYER] CharacterSelectPage HandleSlotSelection Failed: UUIDComponent is null");
            player.remove();
            return;
        }

        UUID playerUuid = uuidComponent.getUuid();
        fr.welltale.player.Player playerData = this.playerRepository.getPlayer(playerUuid);
        if (playerData == null) {
            this.logger.atSevere().log("[PLAYER] CharacterSelectPage HandleSlotSelection Failed: PlayerData is null");
            player.remove();
            return;
        }

        List<fr.welltale.player.Player.Character> characters = playerData.getCharacters();
        if (characters == null || slotIndex >= characters.size() || characters.get(slotIndex) == null) {
            openClassSelectPage(ref, store, player);
            return;
        }

        fr.welltale.player.Player.Character selectedCharacter = characters.get(slotIndex);
        if (selectedCharacter.getClassUuid() == null) {
            openClassSelectPage(ref, store, player);
            return;
        }

        Characteristics.setCharacteristicsToPlayer(ref, store, selectedCharacter.getEditableCharacteristics());

        PlayerLevelComponent playerLevelComponent = store.getComponent(ref, PlayerLevelComponent.getComponentType());
        if (playerLevelComponent != null) {
            playerLevelComponent.setTotalExperience(selectedCharacter.getExperience());
        } else {
            store.addComponent(ref, PlayerLevelComponent.getComponentType(), new PlayerLevelComponent(selectedCharacter.getExperience()));
        }

        Rank playerRank = null;
        if (playerData.getRankUuid() != null) {
            Rank rank = this.rankRepository.getRankConfig(playerData.getRankUuid());
            if (rank != null) {
                playerRank = rank;
            }
        }

        Nameplate.setPlayerNameplate(
                ref,
                store,
                playerData,
                playerRef,
                playerRank,
                logger
        );

        player.getHudManager().setCustomHud(playerRef, new PlayerHudBuilder(playerRef));

        try {
            this.characterCacheRepository.addCharacterCache(new CachedCharacter(
                    playerUuid,
                    playerData.getGems(),
                    selectedCharacter.getCharacterUuid(),
                    selectedCharacter.getClassUuid(),
                    selectedCharacter.getEditableCharacteristics(),
                    selectedCharacter.getCharacteristicPoints(),
                    selectedCharacter.getGuildUuid()
            ));
        } catch (Exception e) {
            this.logger.atSevere()
                    .log("[PLAYER] PlayerReadyEvent OnPlayerReady Failed: " + e.getMessage());
            return;
        }

        player.getPageManager().setPage(ref, store, Page.None);
    }

    private void openClassSelectPage(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, @NonNull Player player) {
        player.getPageManager().openCustomPage(
                ref,
                store,
                new ClassSelectPage(this.playerRef, this.classRepository, this.playerRepository, this.characterCacheRepository, this.rankRepository, this.universe, this.logger)
        );
    }

    private int parseSlotIndex(String raw) {
        if (raw == null || raw.isBlank()) return -1;

        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ignored) {
            return -1;
        }
    }

    private void applyState(@NonNull UICommandBuilder cmd, @NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            this.logger.atSevere().log("[PLAYER] CharacterSelectPage ApplyState Failed: UUIDComponent is null");
            return;
        }

        fr.welltale.player.Player playerData = this.playerRepository.getPlayer(uuidComponent.getUuid());
        if (playerData == null) {
            this.logger.atSevere().log("[PLAYER] CharacterSelectPage ApplyState Failed: PlayerData is null");
            return;
        }

        List<fr.welltale.player.Player.Character> characters = playerData.getCharacters();
        if (characters == null) {
            characters = new ArrayList<>();
        }

        for (int i = 0; i < SLOT_COUNT; i++) {
            int slotNumber = i + 1;
            String prefix = "#CharacterSlot" + slotNumber;

            fr.welltale.player.Player.Character character = i < characters.size() ? characters.get(i) : null;
            if (character == null) {
                continue;
            }

            String className = resolveClassName(character.getClassUuid());
            int level = XPTable.getLevelForXP(Math.max(0L, character.getExperience()));

            cmd.set(prefix + "Name.Text", "PERSONNAGE " + slotNumber);
            cmd.set(prefix + "Class.Text", className);
            cmd.set(prefix + "Level.Text", Constant.Prefix.LEVEL_PREFIX.toUpperCase() + level);
            cmd.set(prefix + "Action.Text", "Cliquer pour jouer");
        }
    }

    private String resolveClassName(UUID classUuid) {
        if (classUuid == null) {
            return "UNKNOWN";
        }

        fr.welltale.clazz.Class classConfig = this.classRepository.getClassConfig(classUuid);
        if (classConfig == null || classConfig.getName() == null || classConfig.getName().isBlank()) {
            return "UNKNOWN";
        }

        return classConfig.getName();
    }
}
