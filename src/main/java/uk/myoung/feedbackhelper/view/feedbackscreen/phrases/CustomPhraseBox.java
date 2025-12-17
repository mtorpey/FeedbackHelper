package uk.myoung.feedbackhelper.view.feedbackscreen.phrases;

import java.awt.BorderLayout;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import uk.myoung.feedbackhelper.model.Phrase;
import uk.myoung.feedbackhelper.view.style.Fonts;

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
    public static CustomPhraseBox create(
        Phrase phrase,
        Consumer<String> onInsertPhrase,
        Consumer<String> onDeleteCustomPhrase,
        BiConsumer<String, Integer> onReorderPhrase
    ) {
        var box = new CustomPhraseBox(phrase);
        box.setup(onInsertPhrase);
        box.setupControls(onDeleteCustomPhrase, onReorderPhrase);
        return box;
    }

    private void setupControls(Consumer<String> onDeleteCustomPhrase, BiConsumer<String, Integer> onReorderPhrase) {
        // Make controls panel
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));

        // Make arrow buttons
        JButton up = new JButton("🡡");
        JButton down = new JButton("🡣");
        up.setFont(Fonts.getTinyFont());
        down.setFont(Fonts.getTinyFont());
        up.addActionListener(e -> onReorderPhrase.accept(getPhraseText(), -1));
        down.addActionListener(e -> onReorderPhrase.accept(getPhraseText(), +1));

        // Make delete button
        JButton deleteButton = new JButton("❌");
        deleteButton.setFont(Fonts.getTinyFont());
        deleteButton.setToolTipText("Delete this phrase.");
        deleteButton.addActionListener(e -> onDeleteCustomPhrase.accept(getPhraseText()));

        // Add buttons to panel
        controls.add(up);
        controls.add(deleteButton);
        controls.add(down);

        // Arrange controls properly
        controls.setMaximumSize(controls.getPreferredSize());
        controls.setAlignmentY(0.5f);

        // Place controls at east of box, with padding
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.PAGE_AXIS));
        eastPanel.add(Box.createVerticalGlue());
        eastPanel.add(controls);
        eastPanel.add(Box.createVerticalGlue());
        add(eastPanel, BorderLayout.LINE_END);
    }

    private CustomPhraseBox(Phrase phrase) {
        super(phrase);
    }
}
