package fr.chosmoz.constant;

import java.awt.*;

public class Constant {
    public static class World {
        public static class CelesteIslandWorld {
            public static String WorldName = "default";
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
        public static String LevelPrefix = "Lv.";
        public static Color LevelPrefixColor = fr.chosmoz.util.Color.CHOSMOZ;

        // Server
        public static String ServerPrefix = "Chosmoz";
        public static Color ServerPrefixColor = fr.chosmoz.util.Color.BLUE;
    }

    public static class Particle {
        public static String PLAYER_SPAWN_SPAWN = "PlayerSpawn_Spawn";
    }
}