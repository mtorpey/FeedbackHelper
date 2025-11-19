package view;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * Border Creator Class.
 */
public abstract class BorderCreator {

    // Constants for number of pixels in 100% scaled display
    public static final int PADDING_1_PIXEL = 1;
    public static final int PADDING_5_PIXELS = 5;
    public static final int PADDING_15_PIXELS = 15;

    public static final Border textAreaBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            emptyBorder5Pixels()
        );
    }

    public static Border statusBarBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            emptyBorder1Pixel()
        );
    }

    public static Border emptyBorder1Pixel() {
        return BorderFactory.createEmptyBorder(PADDING_1_PIXEL, PADDING_1_PIXEL, PADDING_1_PIXEL, PADDING_1_PIXEL);
    }

    public static Border emptyBorder5Pixels() {
        return BorderFactory.createEmptyBorder(PADDING_5_PIXELS, PADDING_5_PIXELS, PADDING_5_PIXELS, PADDING_5_PIXELS);
    }

    public static Border emptyBorder15Pixels() {
        return BorderFactory.createEmptyBorder(PADDING_15_PIXELS, PADDING_15_PIXELS, PADDING_15_PIXELS, PADDING_15_PIXELS);
    }
}
