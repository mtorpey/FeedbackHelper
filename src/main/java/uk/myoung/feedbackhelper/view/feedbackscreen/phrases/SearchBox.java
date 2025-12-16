package uk.myoung.feedbackhelper.view.feedbackscreen.phrases;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Search box used to filter phrases by a user-entered string.
 */
public class SearchBox extends JTextField {

    /**
     * Interface used by any object that wants to be notified of changes.
     */
    public interface Listener {
        /**
         * Called whenever the contents of the search box are modified.
         *
         * @param query The new contents of the search box.
         */
        public void searchBoxUpdate(String query);
    }

    private Listener listener;
    private static final String PROMPT = "🔎 Filter by phrase";

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param listener The object that will handle changes and perform a search.
     */
    public static SearchBox create(Listener listener) {
        var box = new SearchBox(listener);
        box.setupListeners();

        box.setVisible(true);

        return box;
    }

    private SearchBox(Listener listener) {
        // Start with prompt displayed to user
        super(PROMPT);
        this.listener = listener;
    }

    private void setupListeners() {
        // Inform listener of all changes to contents
        getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    updated();
                }

                public void removeUpdate(DocumentEvent e) {
                    updated();
                }

                public void insertUpdate(DocumentEvent e) {
                    updated();
                }
            }
        );

        // Display prompt only when empty and unfocused
        addFocusListener(
            new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(PROMPT);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (getText().equals(PROMPT)) {
                        setText("");
                    }
                }
            }
        );
    }

    /**
     * Clear the contents of the box, ending the current search.
     */
    public void clear() {
        setText(PROMPT);
    }

    /**
     * Inform listener of recent change. Called whenever contents are changed.
     */
    private void updated() {
        String text = getText();
        if (!text.equals(PROMPT)) {
            listener.searchBoxUpdate(getText());
        }
    }
}
