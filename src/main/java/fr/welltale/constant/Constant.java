package fr.welltale.constant;

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
            public static final String STATIC_MODIFIER_DAMAGE_KEY = "Damage";
            public static final String STATIC_MODIFIER_CRITICAL_DAMAGE_KEY = "CriticalDamage";
            public static final String STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY = "CriticalResistance";
            public static final String STATIC_MODIFIER_CRITICAL_PCT_KEY = "Critical";
            public static final String STATIC_MODIFIER_BONUS_XP_KEY = "BonusXP";
        }
    }

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

        // Server
        public static String SERVER_PREFIX = "Welltale";
        public static Color SERVER_PREFIX_COLOR = fr.welltale.util.Color.WELLTALE;
    }

    public static class Particle {
        public static String PLAYER_SPAWN_SPAWN = "PlayerSpawn_Spawn";
    }

    public static class SoundIndex {
        public static int LEVEL_UP = SoundEvent.getAssetMap().getIndex("LevelUp");
        public static int XP_GAINED = SoundEvent.getAssetMap().getIndex("XPGained");
    }
}