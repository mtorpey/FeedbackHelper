package view;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import controller.IAppController;
import model.LinkedPhrases;

/**
 * Insight Box Class.
 */
public class InsightBox extends JPanel {

    // Instance variables
    private final IAppController controller;
    private LinkedPhrases linkedPhrases;
    private PhraseBox phraseBox1;
    private PhraseBox phraseBox2;

    /**
     * Constructor.
     *
     * @param controller    The controller.
     * @param linkedPhrases The pair of linked phrases.
     */
    public InsightBox(IAppController controller, LinkedPhrases linkedPhrases) {
        this.controller = controller;
        this.linkedPhrases = linkedPhrases;

        // Set layout and border
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setBorder(new CompoundBorder(
                new EmptyBorder(20, 10, 0, 10),
                new LineBorder(Configuration.COLOR_INSIGHT_BORDER, 1)
        ));

        // Setup components
        setupInfoText();
        setupPhraseBoxes();

        // Set size and visibility
        this.setMaximumSize(new Dimension(260, 300));
        this.setVisible(true);
    }

    /**
     * Setup the phrase boxes.
     */
    private void setupPhraseBoxes() {
        this.phraseBox1 = new PhraseBox(this.controller, this.linkedPhrases.getFirst().getPhraseAsString(), this.linkedPhrases.getFirst().getUsageCount());
        this.phraseBox2 = new PhraseBox(this.controller, this.linkedPhrases.getSecond().getPhraseAsString(), this.linkedPhrases.getSecond().getUsageCount());
        this.add(this.phraseBox1);
        this.add(this.phraseBox2);
    }

    /**
     * Setup the info text.
     */
    private void setupInfoText() {
        JTextArea infoText = new JTextArea("The following phrases have been paired together " + this.linkedPhrases.getCount() + " times.");
        infoText.setBorder(BorderCreator.createAllSidesEmptyBorder(BorderCreator.PADDING_10_PIXELS));
        infoText.setWrapStyleWord(true);
        infoText.setLineWrap(true);
        this.add(infoText);
    }

}
