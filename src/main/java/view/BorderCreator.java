package view;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import configuration.UserPreferences;

/**
 * Border Creator Class.
 */
public abstract class BorderCreator {

    // Constants for number of pixels in 100% scaled display
    public static final int PADDING_TINY = 2;
    public static final int PADDING_SMALL = 5;
    public static final int PADDING_MEDIUM = 15;

    public static final Border textAreaBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            emptyBorderSmall()
        );
    }

    public static Border statusBarBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            emptyBorderTiny()
        );
    }

    public static Border emptyBorderTiny() {
        return emptyBorderScaled(PADDING_TINY);
    }

    public static Border emptyBorderSmall() {
        return emptyBorderScaled(PADDING_SMALL);
    }

    public static Border emptyBorderMedium() {
        return emptyBorderScaled(PADDING_MEDIUM);
    }

    private static Border emptyBorderScaled(int padding) {
        padding = Math.round(padding * UserPreferences.getScale());
        return BorderFactory.createEmptyBorder(padding, padding, padding, padding);
    }

}
