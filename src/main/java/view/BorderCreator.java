package view;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Border Creator Class.
 */
public abstract class BorderCreator {

    // Constants
    public static final int PADDING_0_PIXELS = 0;
    public static final int PADDING_1_PIXEL = 1;
    public static final int PADDING_5_PIXELS = 5;
    public static final int PADDING_10_PIXELS = 10;
    public static final int PADDING_20_PIXELS = 20;
    public static final int PADDING_50_PIXELS = 50;

    /**
     * Create an empty border leaving the bottom side as 0 pixels.
     *
     * @param padding The number of padding pixels to use.
     * @return The empty border with the desired number of pixels used.
     */
    public static Border createEmptyBorderLeavingBottom(int padding) {
        return new EmptyBorder(padding, padding, PADDING_0_PIXELS, padding);
    }

    /**
     * Create an empty border only on the bottom side, leaving all other sides as 0 pixels.
     *
     * @param padding The number of padding pixels to use.
     * @return The empty border with the desired number of pixels used.
     */
    public static Border createEmptyBorderBottomOnly(int padding) {
        return new EmptyBorder(PADDING_0_PIXELS, PADDING_0_PIXELS, padding, PADDING_0_PIXELS);
    }

    /**
     * Create an empty border leaving the top side as 0 pixels.
     *
     * @param padding The number of padding pixels to use.
     * @return The empty border with the desired number of pixels used.
     */
    public static Border createEmptyBorderLeavingTop(int padding) {
        return new EmptyBorder(PADDING_0_PIXELS, padding, padding, padding);
    }

    /**
     * Create an empty border using the desired number of pixels.
     *
     * @param padding The number of padding pixels to use.
     * @return The empty border with the desired number of pixels used.
     */
    public static Border createAllSidesEmptyBorder(int padding) {
        return new EmptyBorder(padding, padding, padding, padding);
    }

    /**
     * Create a compound border, to show a component as unselected.
     *
     * @return The compound border in a light gray colour.
     */
    public static Border unselectedBorder() {
        return new CompoundBorder(
                new MatteBorder(PADDING_1_PIXEL, PADDING_5_PIXELS, PADDING_1_PIXEL, PADDING_1_PIXEL, Configuration.COLOR_BORDER_UNSELECTED),
                new EmptyBorder(PADDING_10_PIXELS, PADDING_10_PIXELS, PADDING_10_PIXELS, PADDING_10_PIXELS)
        );
    }

    /**
     * Create a compound border, to show a component as selected.
     *
     * @return The compound border in a green colour.
     */
    public static Border selectedBorder() {
        return new CompoundBorder(
                new MatteBorder(PADDING_1_PIXEL, PADDING_5_PIXELS, PADDING_1_PIXEL, PADDING_1_PIXEL, Configuration.COLOR_BORDER_SELECTED),
                new EmptyBorder(PADDING_10_PIXELS, PADDING_10_PIXELS, PADDING_10_PIXELS, PADDING_10_PIXELS)
        );
    }

}
