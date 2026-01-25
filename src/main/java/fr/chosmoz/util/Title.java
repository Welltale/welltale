package fr.chosmoz.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

import javax.annotation.Nonnull;

public class Title {
    public static void sendTitle(
            @Nonnull PlayerRef playerRef,
            @Nonnull String title,
            @Nonnull String subTitle,
            boolean isMajor
    ) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw(title),
                Message.raw(subTitle),
                isMajor
        );
    }

    public static void sendWelcomeTitle(@Nonnull PlayerRef playerRef) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw("Bienvenue sur Chosmoz"),
                Message.raw("Saison: L’Éveil du Chosmoz"),
                true
        );
    }
}
