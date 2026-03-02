package fr.welltale.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import lombok.NonNull;

public class Title {
    public static void sendTitle(
            @NonNull PlayerRef playerRef,
            @NonNull String title,
            @NonNull String subTitle,
            boolean isMajor
    ) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw(title),
                Message.raw(subTitle),
                isMajor
        );
    }

    public static void sendWelcomeTitle(@NonNull PlayerRef playerRef) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw("Bienvenue sur Welltale"),
                Message.raw("Saison: ?"),
                true
        );
    }
}
