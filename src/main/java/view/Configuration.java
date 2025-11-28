package view;

import java.awt.Font;

import javax.swing.UIManager;

/**
 * Visual configuration options for the tool.
 *
 * We're trying to move away from this sort of hard-coded stuff and leave more
 * to Swing's look-and-feel (LAF) system.
 */
public abstract class Configuration {

    /**
     * Return a font that can be used for titles, based on standard Label font
     */
    public static Font getTitleFont() {
        return getSizedFont(3);
    }

    /**
     * Return a font that can be used for subtitles, based on standard Label font
     */
    public static Font getSubtitleFont() {
        return getSizedFont(2);
    }

    /**
     * Return a tiny font
     */
    public static Font getTinyFont() {
        return getSizedFont(0.5f);
    }

    private static Font getSizedFont(float scale) {
        Font baseFont = UIManager.getFont("Label.font");
        Font titleFont = baseFont.deriveFont(Font.BOLD, baseFont.getSize() * scale);
        return titleFont;
    }
}
