package fr.chosmoz.clazz.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

public class ClassSelectUI extends InteractiveCustomUIPage<ClassSelectUI.Data> {
    public ClassSelectUI(@NotNull PlayerRef playerRef, @NotNull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, Data.CODEC);
    }

    @Override
    public void build(@NotNull Ref<EntityStore> ref, @NotNull UICommandBuilder uiCommandBuilder, @NotNull UIEventBuilder uiEventBuilder, @NotNull Store<EntityStore> store) {
        uiCommandBuilder.append("ClassSelectMenu.ui");
        uiCommandBuilder.set("#MyLabel.TextSpans", Message.raw("Choix de classe"));
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .build();
    }
}