package view;

import java.awt.BorderLayout;
import java.util.function.Consumer;

import javax.swing.JButton;

import model.Phrase;

/**
 * Phrase Box for a custom phrase
 */
public class CustomPhraseBox extends PhraseBox {

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param phrase     The phrase to display.
     * @param onInsertPhrase Callback for when the user wishes to insert a phrase.
     */
    public static CustomPhraseBox create(Phrase phrase, Consumer<String> onInsertPhrase, Consumer<String> onDeleteCustomPhrase) {
        var box = new CustomPhraseBox(phrase);
        box.setup(onInsertPhrase);
        box.setupDeleteButton(onDeleteCustomPhrase);
        return box;
    }

    private void setupDeleteButton(Consumer<String> onDeleteCustomPhrase) {
        JButton deleteButton = new JButton("❌");
        deleteButton.setToolTipText("Delete this phrase.");
        deleteButton.addActionListener(e -> onDeleteCustomPhrase.accept(getPhraseText()));
        add(deleteButton, BorderLayout.LINE_END);
    }
    
    private CustomPhraseBox(Phrase phrase) {
        super(phrase);
    }
}
