package fr.welltale.util;

import javax.annotation.Nonnull;

public class Color {
    public static final java.awt.Color BLACK = new java.awt.Color(0, 0, 0);
    public static final java.awt.Color DARK_BLUE = new java.awt.Color(0, 0, 170);
    public static final java.awt.Color DARK_GREEN = new java.awt.Color(0, 170, 0);
    public static final java.awt.Color DARK_AQUA = new java.awt.Color(0, 170, 170);
    public static final java.awt.Color DARK_RED = new java.awt.Color(170, 0, 0);
    public static final java.awt.Color DARK_PURPLE = new java.awt.Color(170, 0, 170);
    public static final java.awt.Color GOLD = new java.awt.Color(255, 170, 0);
    public static final java.awt.Color GRAY = new java.awt.Color(170, 170, 170);
    public static final java.awt.Color DARK_GRAY = new java.awt.Color(85, 85, 85);
    public static final java.awt.Color BLUE = new java.awt.Color(85, 85, 255);
    public static final java.awt.Color GREEN = new java.awt.Color(85, 255, 85);
    public static final java.awt.Color AQUA = new java.awt.Color(85, 255, 255);
    public static final java.awt.Color RED = new java.awt.Color(255, 85, 85);
    public static final java.awt.Color LIGHT_PURPLE = new java.awt.Color(255, 85, 255);
    public static final java.awt.Color YELLOW = new java.awt.Color(255, 255, 85);
    public static final java.awt.Color WHITE = new java.awt.Color(255, 255, 255);
    public static final java.awt.Color WELLTALE = new java.awt.Color(62, 121, 246);

    public static java.awt.Color getColor(@Nonnull String color) {
        return switch (color.toUpperCase()) {
            case "BLACK" -> BLACK;
            case "DARK_BLUE" -> DARK_BLUE;
            case "DARK_GREEN" -> DARK_GREEN;
            case "DARK_AQUA" -> DARK_AQUA;
            case "DARK_RED" -> DARK_RED;
            case "DARK_PURPLE" -> DARK_PURPLE;
            case "GOLD" -> GOLD;
            case "GRAY" -> GRAY;
            case "DARK_GRAY" -> DARK_GRAY;
            case "BLUE" -> BLUE;
            case "GREEN" -> GREEN;
            case "AQUA" -> AQUA;
            case "RED" -> RED;
            case "LIGHT_PURPLE" -> LIGHT_PURPLE;
            case "YELLOW" -> YELLOW;
            case "WHITE" -> WHITE;
            case "WELLTALE" -> WELLTALE;
            default -> GRAY;
        };

    }
}
