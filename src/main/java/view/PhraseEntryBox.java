package view;

import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import controller.AppController;

/**
 * Phrase Entry Box Class.
 */
public class PhraseEntryBox extends JPanel {

    // Instance variables
    private final AppController controller;
    private JTextArea textArea;
    private JButton submitButton;

    /**
     * Constructor.
     *
     * @param controller The controller.
     */
    public PhraseEntryBox(AppController controller) {
        this.controller = controller;

        // Setup components
        this.setLayout(new BorderLayout());
        this.setupTextArea();
        this.setupSubmitButton();

        // Set border and visibility
        this.setBorder(BorderCreator.createAllSidesEmptyBorder(BorderCreator.PADDING_10_PIXELS));
        this.setVisible(true);
    }

    /**
     * Setup the text area.
     */
    private void setupTextArea() {
        this.textArea = new JTextArea();
        this.textArea.setBorder(BorderCreator.createAllSidesEmptyBorder(BorderCreator.PADDING_10_PIXELS));
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.add(this.textArea, BorderLayout.CENTER);
    }

    /**
     * Setup the submit button.
     */
    private void setupSubmitButton() {
        // Following resize code is adapted from:
        // https://stackoverflow.com/questions/6714045/how-to-resize-jlabel-imageicon
        // Submit arrow image from: https://commons.wikimedia.org/wiki/File:Eo_circle_green_arrow-left.svg
        // Submit arrow image has a creative commons license
        this.submitButton = new JButton(
            new ImageIcon(
                new ImageIcon(this.getClass().getResource("/submit_arrow.png"))
                    .getImage()
                    .getScaledInstance(25, 25, Image.SCALE_DEFAULT)
            )
        );
        this.add(this.submitButton, BorderLayout.LINE_END);

        this.submitButton.addActionListener(l -> {
            String phrase = this.textArea.getText();
            this.controller.addNewCustomPhraseFromView(controller.getCurrentHeadingBeingEdited(), phrase);
            this.textArea.setText("");
        });
    }

    /**
     * Disable the phrase entry box.
     */
    public void disablePhraseEntryBox() {
        this.submitButton.setEnabled(false);
        this.textArea.setEnabled(false);

        // Refresh the UI
        this.repaint();
        this.revalidate();
    }

    /**
     * Enable the phrase entry box.
     */
    public void enablePhraseEntryBox() {
        this.submitButton.setEnabled(true);
        this.textArea.setEnabled(true);

        // Refresh the UI
        this.repaint();
        this.revalidate();
    }
}
