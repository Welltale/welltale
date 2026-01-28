package fr.chosmoz.constant;

import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;

import java.awt.*;

public class Constant {
    public static class Player {
        public static class Stat {
            public static final float DEFAULT_STAMINA_AMOUNT = 10;
            public static final float DEFAULT_HEALTH_AMOUNT = 100;
            public static final float DEFAULT_MANA_AMOUNT = 0;

            public static final String STATIC_MODIFIER_STAMINA_KEY = "Stamina";
            public static final String STATIC_MODIFIER_HEALTH_KEY = "Health";
            public static final String STATIC_MODIFIER_MANA_KEY = "Mana";
        }
    }

    public static class World {
        public static class CelesteIslandWorld {
            public static String WORLD_NAME = "default";
        }
    }

    public static class Permission {
        public static class Global {
            public static String OPERATOR = "*";
            public static String BREAK_BLOCK = "break_block.permission";
            public static String PLACE_BLOCK = "place_block.permission";
            public static String ITEM_DROP = "item_drop.permission";
        }
    }

    public static class Prefix {
        // Level
        public static String LEVEL_PREFIX = "Lv.";
        public static Color LEVEL_PREFIX_COLOR = fr.chosmoz.util.Color.CHOSMOZ;

        // Server
        public static String SERVER_PREFIX = "Chosmoz";
        public static Color SERVER_PREFIX_COLOR = fr.chosmoz.util.Color.BLUE;
    }

    public static class Particle {
        public static String PLAYER_SPAWN_SPAWN = "PlayerSpawn_Spawn";
        public static String BLOCK_BREAK_MUD = "Block_Break_Mud";
    }

    public static class SoundIndex {
        public static int SFX_BATTLEAXE_T1_SWING_CHARGED = SoundEvent.getAssetMap().getIndex("SFX_Battleaxe_T1_Swing_Charged");
    }
}