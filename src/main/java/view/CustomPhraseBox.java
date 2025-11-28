package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

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
    public static CustomPhraseBox create(Phrase phrase, Consumer<String> onInsertPhrase) {
        var box = new CustomPhraseBox(phrase);
        box.setup(onInsertPhrase);
        return box;
    }

    private CustomPhraseBox(Phrase phrase) {
        super(phrase);
    }
}
