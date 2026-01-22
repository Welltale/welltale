package fr.chosmoz.clazz.page.ClassSelect;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

public class ClassSelectPage extends InteractiveCustomUIPage<ClassSelectPage.ClassSelectEventData> {
    public static class ClassSelectEventData {
        public static final BuilderCodec<ClassSelectEventData> CODEC =
                BuilderCodec.builder(ClassSelectEventData.class, ClassSelectEventData::new)
                        .build();
    }

    public ClassSelectPage(@NonNull PlayerRef playerRef, HytaleLogger logger) {
        super(
                playerRef,
                //TODO EDIT TO CANTCLOSE
                CustomPageLifetime.CanDismiss,
                ClassSelectEventData.CODEC
        );
    }

    @Override
    public void build(@NonNull Ref<EntityStore> ref, @NonNull UICommandBuilder command, @NonNull UIEventBuilder event, @NonNull Store<EntityStore> store) {
        command.append("Pages/Class/ClassSelectPage.ui");
    }

    //public void handleDataEvent(
    //        @NonNull Ref<EntityStore> ref,
    //        @NonNull Store<EntityStore> store,
    //        @NonNull ClassSelectEventData data
    //) {
    //    Player player = store.getComponent(ref, Player.getComponentType());
    //   if (player == null) {
    //        this.logger.atSevere()
    //                .log("[CLASS] ClassSelectPage HandleDataEvent Failed: Player is null");
    //        return;
    //    }
    //
    //    player.getPageManager().setPage(ref, store, Page.None);
    //    player.sendMessage(Message.raw("handleDataEvent received"));
    //}
}