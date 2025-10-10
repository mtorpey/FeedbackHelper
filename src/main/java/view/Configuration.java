package view;

import java.awt.Color;

/**
 * Visual configuration options for the tool.
 *
 * Currently this just specifies some colours that are used in the view, so that
 * they can be changed altogether in one place.
 *
 * In future, there should be a programmatic way to change these, rather than
 * using hard-coded constants (e.g. a dark mode option).  We should also set
 * some more colours explicitly, since a lot of the view currently uses the
 * default Swing colours.
 */
public abstract class Configuration {

    /** Colour used for the border of an unselected component. */
    public static final Color COLOR_BORDER_UNSELECTED = Color.LIGHT_GRAY;

    /** Colour used for the border of a component when it is selected. */
    public static final Color COLOR_BORDER_SELECTED = Color.GREEN;

    /** Colour used for the border of an insight box. */
    public static final Color COLOR_INSIGHT_BORDER = Color.BLACK;
}
