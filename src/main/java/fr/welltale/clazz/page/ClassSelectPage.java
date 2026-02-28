package fr.welltale.clazz.page;

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
import fr.welltale.clazz.Class;
import fr.welltale.clazz.ClassRepository;
import fr.welltale.constant.Constant;
import fr.welltale.hud.PlayerHudBuilder;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.player.PlayerRepository;
import fr.welltale.player.charactercache.CachedCharacter;
import fr.welltale.player.charactercache.CharacterCacheRepository;
import fr.welltale.player.page.CharacterSelectPage;
import fr.welltale.rank.RankRepository;
import fr.welltale.util.Teleport;
import fr.welltale.util.Title;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClassSelectPage extends InteractiveCustomUIPage<ClassSelectPage.ClassSelectPageEventData> {
    public enum ClassSelectPageButtonPressedId {
        RETURN, PLAY
    }

    private UUID classSelectedUuid;

    private final ClassRepository classRepository;
    private final PlayerRepository playerRepository;
    private final CharacterCacheRepository characterCacheRepository;
    private final RankRepository rankRepository;
    private final Universe universe;
    private final HytaleLogger logger;

    public static class ClassSelectPageEventData {
        public String buttonPressedId;
        public UUID classSelectedUuid;

        public static final BuilderCodec<ClassSelectPageEventData> CODEC =
                BuilderCodec.builder(ClassSelectPageEventData.class, ClassSelectPageEventData::new)
                        .append(new KeyedCodec<>("ButtonPressed", Codec.STRING),
                                (ClassSelectPageEventData obj, String val) -> obj.buttonPressedId = val,
                                (ClassSelectPageEventData obj) -> obj.buttonPressedId
                        )
                        .add()
                        .append(new KeyedCodec<>("ClassSelected", Codec.UUID_STRING),
                                (ClassSelectPageEventData obj, UUID val) -> {
                                    if (val != null) {
                                        obj.classSelectedUuid = val;
                                    }
                                },
                                (ClassSelectPageEventData obj) -> obj.classSelectedUuid
                        )
                        .add()
                        .build();
    }

    public ClassSelectPage(
            @NonNull PlayerRef playerRef,
            @NonNull ClassRepository classRepository,
            @NonNull PlayerRepository playerRepository,
            @NonNull CharacterCacheRepository characterCacheRepository,
            @NonNull RankRepository rankRepository,
            @NonNull Universe universe,
            @NonNull HytaleLogger logger
    ) {
        super(
                playerRef,
                CustomPageLifetime.CantClose,
                ClassSelectPageEventData.CODEC
        );
        this.classRepository = classRepository;
        this.playerRepository = playerRepository;
        this.characterCacheRepository = characterCacheRepository;
        this.rankRepository = rankRepository;
        this.universe = universe;
        this.logger = logger;
    }

    @Override
    public void build(@NonNull Ref<EntityStore> ref, @NonNull UICommandBuilder cmd, @NonNull UIEventBuilder event, @NonNull Store<EntityStore> store) {
        if (!ref.isValid()) {
            this.logger.atSevere()
                    .log("[CLASS] ClassSelectPage Build Failed: Ref is invalid");
            return;
        }

        cmd.append("Pages/Class/ClassSelectPage.ui");

        List<Class> classes = this.classRepository.getClassesConfigs();
        if (classes.isEmpty()) {
            Player player = ref.getStore().getComponent(ref, Player.getComponentType());
            if (player == null) {
                this.logger.atSevere()
                        .log("[CLASS] ClassSelectPage Build Failed: Player is null");
                return;
            }

            player.remove();
            return;
        }

        this.classSelectedUuid = classes.getFirst().getUuid();
        cmd.set("#ClassSelectedName.Text", classes.getFirst().getName());
        //TODO ADD CLASS ICON & PREVIEW

        for (int i = 0; i < classes.size(); i++) {
            int y = i + 1;
            cmd.set("#Class" + y + "Name.Text", classes.get(i).getName());
            event.addEventBinding(CustomUIEventBindingType.Activating, "#Class" + y + "Button", EventData.of("ClassSelected", classes.get(i).getUuid().toString()));
        }
        event.addEventBinding(CustomUIEventBindingType.Activating, "#PlayButton", EventData.of("ButtonPressed", ClassSelectPageButtonPressedId.PLAY.name()));
        event.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnButton", EventData.of("ButtonPressed", ClassSelectPageButtonPressedId.RETURN.name()));
    }

    @Override
    public void handleDataEvent(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @NonNull ClassSelectPageEventData data
    ) {
        if (!ref.isValid()) {
            this.logger.atSevere()
                    .log("[CLASS] ClassSelectPage Build Failed: Ref is invalid");
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            this.logger.atSevere()
                    .log("[CLASS] ClassSelectPage HandleDataEvent Failed: Player is null");
            this.sendUpdate();
            return;
        }

        if (Objects.equals(data.buttonPressedId, ClassSelectPageButtonPressedId.RETURN.name())) {
            handleReturn(ref, store, player);
            return;
        }

        if (Objects.equals(data.buttonPressedId, ClassSelectPageButtonPressedId.PLAY.name())) {
            handlePlay(ref, store, player);
        }

        if (data.classSelectedUuid != null) {
            Class newClass = this.classRepository.getClassConfig(data.classSelectedUuid);
            if (newClass == null) {
                this.sendUpdate();
                return;
            }

            this.classSelectedUuid = newClass.getUuid();
            this.sendUpdate(new UICommandBuilder().set("#ClassSelectedName.Text", newClass.getName()));
            //TODO ADD CLASS ICON & PREVIEW
            return;
        }

        this.sendUpdate();
    }

    private void handleReturn(
            @NonNull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull Player player
    ) {
        player.getPageManager().openCustomPage(ref, store, new CharacterSelectPage(
                playerRef,
                this.playerRepository,
                this.characterCacheRepository,
                this.rankRepository,
                this.classRepository,
                this.universe,
                this.logger
                ));
    }

    private void handlePlay(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @NonNull Player player
    ) {
        if (!ref.isValid()) {
            this.logger.atSevere()
                    .log("[CLASS] ClassSelectPage HandlePlay Failed: Ref is invalid");
            return;
        }

        if (this.classSelectedUuid == null) {
            this.sendUpdate();
            return;
        }

        UUIDComponent playerUuid = store.getComponent(ref, UUIDComponent.getComponentType());
        if (playerUuid == null) {
            player.remove();
            return;
        }

        fr.welltale.player.Player playerData = this.playerRepository.getPlayer(playerUuid.getUuid());
        if (playerData == null) {
            player.remove();
            return;
        }

        Class selectedClass = this.classRepository.getClassConfig(this.classSelectedUuid);
        if (selectedClass == null) {
            this.sendUpdate();
            return;
        }

        Characteristics.setCharacteristicsToPlayer(ref, store, new Characteristics.EditableCharacteristics());

        PlayerLevelComponent playerLevelComponent = store.getComponent(ref, PlayerLevelComponent.getComponentType());
        if (playerLevelComponent != null) {
            store.removeComponent(ref, PlayerLevelComponent.getComponentType());
        }

        store.addComponent(ref, PlayerLevelComponent.getComponentType(), new PlayerLevelComponent());

        fr.welltale.player.Player.Character newCharacter = new fr.welltale.player.Player.Character(
                UUID.randomUUID(),
                selectedClass.getUuid(),
                0,
                new Characteristics.EditableCharacteristics(),
                0,
                null
        );
        playerData.getCharacters().add(newCharacter);

        try {
            playerRepository.updatePlayer(playerData);
        } catch (Exception e) {
            this.logger.atSevere().log("[CLASS] ClassSelectPage HandlePlayer Failed: " + e.getMessage());
            player.remove();
            return;
        }

        try {
            this.characterCacheRepository.addCharacterCache(new CachedCharacter(
                    playerUuid.getUuid(),
                    newCharacter.getCharacterUuid(),
                    newCharacter.getClassUuid(),
                    newCharacter.getExperience(),
                    playerData.getGems(),
                    newCharacter.getEditableCharacteristics(),
                    newCharacter.getCharacteristicPoints(),
                    newCharacter.getGuildUuid()
            ));
        } catch (Exception e) {
            this.logger.atSevere().log("[CLASS] ClassSelectPage HandlePlayer Failed: " + e.getMessage());
            player.remove();
            return;
        }

        player.getHudManager().setCustomHud(playerRef, new PlayerHudBuilder(playerRef));

        Teleport.teleportPlayerToSpawn(this.logger, this.universe, playerData.getUuid(), ref, store, Constant.World.CelesteIslandWorld.WORLD_NAME, Constant.Particle.PLAYER_SPAWN_SPAWN);
        player.getPageManager().setPage(ref, store, Page.None);
        Title.sendWelcomeTitle(playerRef);
    }
}
