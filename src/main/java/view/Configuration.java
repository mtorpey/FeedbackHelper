package view;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;

/**
 * Visual configuration options for the tool.
 *
 * We're trying to move away from this sort of hard-coded stuff and leave more
 * to Swing's look-and-feel (LAF) system.
 */
public abstract class Configuration {

    /** Colour used for the border of an unselected component. */
    public static final Color COLOR_BORDER_UNSELECTED = Color.LIGHT_GRAY;

    /** Colour used for the border of a component when it is selected. */
    public static final Color COLOR_BORDER_SELECTED = Color.GREEN;

    /**
     * Return a font that can be used for titles, following the current
     * look-and-feel as much as possible.
     */
    public static Font getTitleFont() {
        // Look for a large font from the L&F
        Font titleFont = UIManager.getFont("Label.large.font");

        // If not found, make a big bold version of the standard font
        if (titleFont == null) {
            Font baseFont = UIManager.getFont("Label.font");
            titleFont = baseFont.deriveFont(Font.BOLD, baseFont.getSize() * 2f);
        }
        return titleFont;
    }
}
