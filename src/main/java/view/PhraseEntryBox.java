package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Phrase Entry Box Class.
 */
public class PhraseEntryBox extends JPanel {

    // Instance variables
    private final Consumer<String> onSubmit;
    private JTextArea textArea;
    private JButton submitButton;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param onSubmit Callback for submitting a new phrase.
     */
    public static PhraseEntryBox create(Consumer<String> onSubmit) {
        var box = new PhraseEntryBox(onSubmit);

        // Setup components
        box.setLayout(new BorderLayout());
        box.setupTextArea();
        box.setupSubmitButton();

        // Set border and visibility
        box.setBorder(BorderCreator.emptyBorderSmall());
        box.setVisible(true);

        return box;
    }

    private PhraseEntryBox(Consumer<String> onSubmit) {
        this.onSubmit = onSubmit;
    }

    /**
     * Setup the text area.
     */
    private void setupTextArea() {
        textArea = new JTextArea();
        textArea.setBorder(BorderCreator.textAreaBorder());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMinimumSize(new Dimension(0, 0));
        add(textArea, BorderLayout.CENTER);
    }

    /**
     * Setup the submit button.
     */
    private void setupSubmitButton() {
        this.submitButton = new JButton("⬆");
        submitButton.setFont(Configuration.getSubtitleFont());
        this.add(this.submitButton, BorderLayout.LINE_END);

        this.submitButton.addActionListener(l -> {
            onSubmit.accept(textArea.getText());
            textArea.setText("");
        });
    }
}
