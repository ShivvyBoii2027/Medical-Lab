package util;

import db.DBManager;
import java.sql.ResultSet;

public class ThemeManager {
    private static ThemeManager instance;

    private String mode = "GOATED"; // GOATED, DARK, LIGHT
    private String accentColor = "#06b6d4";
    private int fontSize = 13;
    private String bgImage = null;

    private ThemeManager() {
    }

    public static ThemeManager getInstance() {
        if (instance == null)
            instance = new ThemeManager();
        return instance;
    }

    public void reset() {
        this.mode = "GOATED";
        this.accentColor = "#06b6d4";
        this.fontSize = 13;
        this.bgImage = null;
    }

    public void loadSettings(String username) {
        try {
            ResultSet rs = DBManager.getUserSettings(username);
            if (rs.next()) {
                this.mode = rs.getString("theme_mode");
                this.accentColor = rs.getString("accent_color");
                this.fontSize = rs.getInt("font_size");
                this.bgImage = rs.getString("bg_image");
            }
        } catch (Exception e) {
            System.err.println("Failed to load theme settings: " + e.getMessage());
        }
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String color) {
        this.accentColor = color;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int size) {
        this.fontSize = size;
    }

    public String getBgImage() {
        return bgImage;
    }

    public void setBgImage(String path) {
        this.bgImage = path;
    }

    // Logic helpers for Theme.java
    public String getBgDeep() {
        return switch (mode) {
            case "LIGHT" -> "#f8fafc";
            default -> "#020817";
        };
    }

    public String getBgMid() {
        return switch (mode) {
            case "LIGHT" -> "#f1f5f9";
            default -> "#0f172a";
        };
    }

    public String getBgCard() {
        return switch (mode) {
            case "LIGHT" -> "#ffffff";
            default -> "#1e293b";
        };
    }

    public String getTextColor() {
        return switch (mode) {
            case "LIGHT" -> "#0f172a";
            case "DARK" -> "#f8fafc";
            default -> "#f8fafc"; // GOATED
        };
    }

    public String getTextSoft() {
        return switch (mode) {
            case "LIGHT" -> "#64748b";
            case "DARK" -> "#94a3b8";
            default -> "#94a3b8"; // GOATED
        };
    }
}
