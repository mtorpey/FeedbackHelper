package view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

/**
 * Editing Popup Menu Class.
 */
public class EditingPopupMenu {

    // Instance variables
    private JPopupMenu popupMenu;
    private String selectedText;
    private JMenuItem copy = new JMenuItem("Copy");
    private JMenuItem paste = new JMenuItem("Paste");

    /**
     * Constructor.
     */
    public EditingPopupMenu() {
        setupPopupMenu();
    }

    /**
     * Setup the popup menu.
     */
    private void setupPopupMenu() {
        this.popupMenu = new JPopupMenu();

        this.popupMenu.add(this.copy);
        setupCopyOperation();

        this.popupMenu.add(this.paste);
        setupPasteOperation();
    }

    /**
     * Register the popup menu with a given feedback box.
     *
     * @param feedbackBox The feedback box to register.
     */
    public void registerFeedbackBox(FeedbackBox feedbackBox) {
        feedbackBox.getTextArea().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.setInvoker(feedbackBox.getTextArea());
                    popupMenu.show(feedbackBox.getTextArea(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Setup the copy operation.
     */
    private void setupCopyOperation() {
        this.copy.addActionListener(e -> {
            this.selectedText = ((JTextArea) this.popupMenu.getInvoker()).getSelectedText();
        });
    }

    /**
     * Setup the paste operation.
     */
    private void setupPasteOperation() {
        this.paste.addActionListener(e -> {
            try {
                JTextArea invoker = (JTextArea) this.popupMenu.getInvoker();
                invoker.getDocument().insertString(invoker.getCaretPosition(), this.selectedText, null);
            } catch (BadLocationException badLocationException) {
                badLocationException.printStackTrace();
            }
        });
    }

}
