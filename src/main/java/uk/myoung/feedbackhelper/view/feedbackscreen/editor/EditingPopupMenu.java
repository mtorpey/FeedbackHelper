package uk.myoung.feedbackhelper.view.feedbackscreen.editor;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import uk.myoung.feedbackhelper.model.SpellCheckReadOnly;
import uk.myoung.feedbackhelper.model.SpellCheckReadOnly.Misspelling;

/**
 * Editing Popup Menu Class.
 */
public class EditingPopupMenu {

    private static final int MAX_SUGGESTIONS = 5;

    private final SpellCheckReadOnly spellChecker;
    private final Consumer<String> onAddWord;
    private JPopupMenu popupMenu;
    private String selectedText;
    private JMenuItem copy = new JMenuItem("Copy");
    private JMenuItem paste = new JMenuItem("Paste");

    /**
     * Constructor.
     */
    public EditingPopupMenu(SpellCheckReadOnly spellChecker, Consumer<String> onAddWord) {
        this.spellChecker = spellChecker;
        this.onAddWord = onAddWord;
        setupPopupMenu();
    }

    private void setupPopupMenu() {
        this.popupMenu = new JPopupMenu();
        setupCopyOperation();
        setupPasteOperation();
    }

    /**
     * Register the popup menu with a given feedback box.
     *
     * @param feedbackBox The feedback box to register.
     */
    public void registerFeedbackBox(FeedbackBox feedbackBox) {
        JTextArea textArea = feedbackBox.getTextArea();
        textArea.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        rebuildMenu(feedbackBox, textArea, textArea.viewToModel2D(e.getPoint()));
                        popupMenu.setInvoker(textArea);
                        popupMenu.show(textArea, e.getX(), e.getY());
                    }
                }
            }
        );
    }

    private void rebuildMenu(FeedbackBox feedbackBox, JTextArea textArea, int offset) {
        popupMenu.removeAll();
        if (spellChecker != null && spellChecker.isEnabled() && textArea.isEditable() && textArea.isEnabled()) {
            feedbackBox
                .getMisspellingAt(offset)
                .ifPresent(misspelling -> addSpellingMenuItems(feedbackBox, misspelling));
        }
        popupMenu.add(this.copy);
        popupMenu.add(this.paste);
    }

    private void addSpellingMenuItems(FeedbackBox feedbackBox, Misspelling misspelling) {
        addSuggestionItems(feedbackBox, misspelling);

        JMenuItem addWordItem = new JMenuItem("Add \"" + misspelling.word() + "\" to dictionary");
        addWordItem.addActionListener(e -> onAddWord.accept(misspelling.word()));
        popupMenu.add(addWordItem);

        popupMenu.addSeparator();
    }

    private void addSuggestionItems(FeedbackBox feedbackBox, Misspelling misspelling) {
        List<String> suggestions = spellChecker.suggest(misspelling.word(), MAX_SUGGESTIONS);
        if (suggestions.isEmpty()) {
            JMenuItem none = new JMenuItem("No suggestions");
            none.setEnabled(false);
            popupMenu.add(none);
            return;
        }
        for (String suggestion : suggestions) {
            JMenuItem item = new JMenuItem(suggestion);
            item.setFont(item.getFont().deriveFont(Font.BOLD));
            item.addActionListener(e -> feedbackBox.replaceText(misspelling.start(), misspelling.end(), suggestion));
            popupMenu.add(item);
        }
    }

    private void setupCopyOperation() {
        this.copy.addActionListener(e -> {
            this.selectedText = ((JTextArea) this.popupMenu.getInvoker()).getSelectedText();
        });
    }

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
