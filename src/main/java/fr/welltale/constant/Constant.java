package fr.welltale.constant;

import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;

import java.awt.*;

public class Constant {
    public static class World {
        public static class CelesteIslandWorld {
            public static String WORLD_NAME = "default";
        }
    }

    public static class Permission {
        public static String OPERATOR = "*";
        public static String STAFF = "welltall.staff";
        public static String BREAK_BLOCK = "break_block.permission";
        public static String PLACE_BLOCK = "place_block.permission";
        public static String ITEM_DROP = "item_drop.permission";
    }

    public static class Prefix {
        // Level
        public static String LEVEL_PREFIX = "Lv.";
        public static Color LEVEL_PREFIX_COLOR = fr.welltale.util.Color.WELLTALE;
        public static String XP_PREFIX = "XP: ";

        // Server
        public static String SERVER_PREFIX = "Welltale";
        public static Color SERVER_PREFIX_COLOR = fr.welltale.util.Color.WELLTALE;
    }

    public static class Particle {
        public static String PLAYER_SPAWN_SPAWN = "PlayerSpawn_Spawn";
        public static String CINEMATIC_FIREWORKS_RED_XL = "Cinematic_Fireworks_Red_XL";
    }

    public static class SoundIndex {
        public static int LEVEL_UP = SoundEvent.getAssetMap().getIndex("LevelUp");
        public static int XP_GAINED = SoundEvent.getAssetMap().getIndex("XPGained");
    }
}