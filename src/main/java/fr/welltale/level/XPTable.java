package fr.welltale.level;

public class XPTable {
    private static final double EXPONENT = 2.8;
    private static final double A = 35.0;

    public static final int MAX_LEVEL = 200;
    public static final int START_LEVEL = 1;

    private XPTable() {}

    public static int getLevelForXP(long totalXP) {
        if (totalXP <= 0) return START_LEVEL;
        double lvl = Math.pow(totalXP / A, 1.0 / EXPONENT);
        return Math.min((int)Math.ceil(lvl), MAX_LEVEL);
    }

    public static long getXPForLevel(int level) {
        if (level <= START_LEVEL) return 0;
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        return (long) (A * Math.pow(level, EXPONENT));
    }

    public static long getXPInCurrentLevel(long totalXP) {
        var level = getLevelForXP(totalXP);
        return totalXP - getXPForLevel(level);
    }

    public static long getXPToNextLevel(long totalXP) {
        int level = getLevelForXP(totalXP);
        if (level >= MAX_LEVEL) return 0;
        return getXPForLevel(level + 1) - getXPForLevel(level);
    }

    public static float getProgressToNextLevel(long totalXP) {
        int level = getLevelForXP(totalXP);
        if (level >= MAX_LEVEL) return 1.0f;

        long xpCurrent = getXPInCurrentLevel(totalXP);
        long xpNeeded = getXPToNextLevel(totalXP);
        return xpNeeded > 0 ? (float) xpCurrent / xpNeeded : 0.0f;
    }

}
