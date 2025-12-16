package uk.myoung.feedbackhelper.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

/** JPanel for use inside a vertical JScrollPane. Will not expand horizontally. */
public class VerticalScrollablePanel extends JPanel implements Scrollable {

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
        return AppView.SCROLL_SPEED;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
        return AppView.SCROLL_SPEED * 3; // Scroll three times as fast when "paging"
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true; // Width should fit to viewport.
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false; // Height should be scrollable.
    }

    /**
     * Create and return a new vertical-only scroll pane, appropriate for use
     * with a VerticalScrollablePanel.
     */
    public static JScrollPane newVerticalScrollPane(Component view) {
        return new JScrollPane(view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
