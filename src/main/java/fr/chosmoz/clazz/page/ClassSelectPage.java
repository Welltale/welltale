package fr.chosmoz.clazz.page;

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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.chosmoz.clazz.Class;
import fr.chosmoz.clazz.ClassRepository;
import fr.chosmoz.player.PlayerRepository;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClassSelectPage extends InteractiveCustomUIPage<ClassSelectPage.ClassSelectPageEventData> {
    public enum ClassSelectPageButtonPressedId {
        DISCONNECT, PLAY
    }

    private UUID classSelectedUuid;

    private final ClassRepository classRepository;
    private final PlayerRepository playerRepository;
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

    public ClassSelectPage(@NonNull PlayerRef playerRef, ClassRepository classRepository, PlayerRepository playerRepository, HytaleLogger logger) {
        super(
                playerRef,
                CustomPageLifetime.CantClose,
                ClassSelectPageEventData.CODEC
        );
        this.classRepository = classRepository;
        this.playerRepository = playerRepository;
        this.logger = logger;
    }

    @Override
    public void build(@NonNull Ref<EntityStore> ref, @NonNull UICommandBuilder cmd, @NonNull UIEventBuilder event, @NonNull Store<EntityStore> store) {
        cmd.append("Pages/Class/ClassSelectPage.ui");

        try {
            List<Class> classes = this.classRepository.getClasses();
            if (!classes.isEmpty()) {
                this.classSelectedUuid = classes.getFirst().getUuid();
                cmd.set("#ClassSelectedName.Text", classes.getFirst().getName());
                //TODO ADD CLASS ICON
            }

            for (int i = 0; i < classes.size(); i++) {
                int y = i + 1;
                cmd.set("#Class" + y + "Name.Text", classes.get(i).getName());
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Class" + y + "Button", EventData.of("ClassSelected", classes.get(i).getUuid().toString()));
            }
        } catch (Exception e) {
            this.logger.atSevere()
                    .log("[CLASS] ClassSelectPage Build Failed: " + e.getMessage());

            Player player = ref.getStore().getComponent(ref, Player.getComponentType());
            if (player == null) {
                this.logger.atSevere()
                        .log("[CLASS] ClassSelectPage Build Failed Exception: Player is null");
                return;
            }

            player.remove();
        }

        event.addEventBinding(CustomUIEventBindingType.Activating, "#PlayButton", EventData.of("ButtonPressed", ClassSelectPageButtonPressedId.PLAY.name()));
        event.addEventBinding(CustomUIEventBindingType.Activating, "#DisconnectButton", EventData.of("ButtonPressed", ClassSelectPageButtonPressedId.DISCONNECT.name()));
    }

    @Override
    public void handleDataEvent(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @NonNull ClassSelectPageEventData data
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            this.logger.atSevere()
                    .log("[CLASS] ClassSelectPage HandleDataEvent Failed: Player is null");
            this.sendUpdate();
            return;
        }

        if (Objects.equals(data.buttonPressedId, ClassSelectPageButtonPressedId.DISCONNECT.name())) {
            handleDisconnect(player);
            return;
        }

        if (Objects.equals(data.buttonPressedId, ClassSelectPageButtonPressedId.PLAY.name())) {
            handlePlay(ref, store, player);
        }

        if (data.classSelectedUuid != null) {
            try {
                Class newClass = this.classRepository.getClass(data.classSelectedUuid);
                this.classSelectedUuid = newClass.getUuid();
                this.sendUpdate(new UICommandBuilder().set("#ClassSelectedName.Text", newClass.getName()));
                return;
            } catch (Exception e) {
                this.logger.atSevere()
                        .log("[CLASS] ClassSelectPage HandleDataEvent Failed: " + e.getMessage());

                this.sendUpdate();
                return;
            }
        }

        this.sendUpdate();
    }

    private void handleDisconnect(@NonNull Player player) {
        player.remove();
    }

    private void handlePlay(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @NonNull Player player
    ) {
        if (this.classSelectedUuid == null) {
            this.sendUpdate();
            return;
        }

        UUIDComponent playerUuid = store.getComponent(ref, UUIDComponent.getComponentType());
        if (playerUuid == null) {
            player.remove();
            return;
        }

        try {
            fr.chosmoz.player.Player playerData = this.playerRepository.getPlayerByUuid(playerUuid.getUuid());
            if (playerData.getClazzUuid() != null) {
                player.getPageManager().setPage(ref, store, Page.None);
                return;
            }

            playerData.setClazzUuid(this.classSelectedUuid);
            playerRepository.updatePlayer(playerData);
            player.getPageManager().setPage(ref, store, Page.None);
        } catch (Exception e) {
            this.logger.atSevere()
                    .log("[CLASS] ClassSelectPage HandlePlay Failed: " + e.getMessage());
        }
    }
}