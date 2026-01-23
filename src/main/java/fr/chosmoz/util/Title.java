package fr.chosmoz.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

public class Title {
    public static void sendJoinTitle(PlayerRef playerRef, String title, String subTitle, boolean isMajor) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw(title),
                Message.raw(subTitle),
                isMajor
        );
    }

    public static void sendWelcomeTitle(PlayerRef playerRef) {
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw("Bienvenue sur Chosmoz"),
                Message.raw("Saison: L’Éveil du Chosmoz"),
                true
        );
    }
}
