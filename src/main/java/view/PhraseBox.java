package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import model.Phrase;

/**
 * Phrase Box Class.
 */
public class PhraseBox extends JPanel implements Comparable<PhraseBox> {

    // Instance variables
    private Phrase phrase;
    private JTextArea phraseTextArea;
    private JButton insertButton;
    private JLabel usageCountLabel;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param phrase     The phrase to display.
     * @param onInsertPhrase Callback for when the user wishes to insert a phrase.
     */
    public static PhraseBox create(Phrase phrase, Consumer<String> onInsertPhrase) {
        var box = new PhraseBox(phrase);

        // Set up some components
        box.setLayout(new BorderLayout());
        box.setupInsertButton(onInsertPhrase);
        box.setupPhraseTextArea();
        box.setupUsageCountLabel();

        // Arrange for viewing
        box.setVisible(true);

        return box;
    }

    private PhraseBox(Phrase phrase) {
        this.phrase = phrase;
    }

    /**
     * Setup the insert button.
     */
    private void setupInsertButton(Consumer<String> onInsertPhrase) {
        insertButton = new JButton("⬅");
        insertButton.setFont(Configuration.getSubtitleFont());
        insertButton.setToolTipText("Insert this phrase into the document.");
        insertButton.addActionListener(l -> onInsertPhrase.accept(phrase.getPhraseAsString()));
        add(insertButton, BorderLayout.LINE_START);
    }

    /**
     * Setup the phrase text area.
     */
    private void setupPhraseTextArea() {
        phraseTextArea = new JTextArea();

        // Set properties
        phraseTextArea.setBorder(BorderCreator.textAreaBorder());
        phraseTextArea.setText(phrase.getPhraseAsString());
        phraseTextArea.setLineWrap(true);
        phraseTextArea.setWrapStyleWord(true);
        phraseTextArea.setEditable(false);
        phraseTextArea.setFocusable(false);

        // Add the text area and some padding to the bottom of the panel
        add(phraseTextArea, BorderLayout.CENTER);
    }

    /**
     * Setup the usage count label.
     */
    private void setupUsageCountLabel() {
        usageCountLabel = new JLabel(String.valueOf(phrase.getUsageCount()));
        usageCountLabel.setBorder(BorderCreator.emptyBorderSmall());
        add(usageCountLabel, BorderLayout.LINE_END);
    }

    /**
     * Get the phrase in the box.
     *
     * @return The phrase in the box.
     */
    public String getPhraseText() {
        return phrase.getPhraseAsString();
    }

    /**
     * Get the usage count of the phrase.
     *
     * @return The usage count of the phrase.
     */
    public long getUsageCount() {
        return phrase.getUsageCount();
    }

    /**
     * Set the usage count of the phrase.
     *
     * @param usageCount The usage count of the phrase.
     */
    public void setUsageCount(long usageCount) {
        this.phrase.setUsageCount(usageCount);
        this.usageCountLabel.setText(String.valueOf(usageCount));

        // Refresh the UI
        this.repaint();
        this.revalidate();
    }

    /**
     * Make this box visible if and only if its phrase matches the provided
     * search query.
     *
     * A phrase matches the search if it contains the query as a substring, case
     * insensitive.
     *
     * @param query The search query to check.
     */
    public void setVisibleBySearchQuery(String query) {
        String contents = this.getPhraseText().toLowerCase();
        query = query.toLowerCase();
        this.setVisible(contents.contains(query));
    }

    /**
     * Get the maximum size of this object for display.
     *
     * This is overridden so that the object is never taller than it needs to
     * be. That is, its maximum height is always equal to its preferred height.
     */
    @Override
    public Dimension getMaximumSize() {
        Dimension maximum = super.getMaximumSize();
        Dimension preferred = getPreferredSize();
        return new Dimension(maximum.width, preferred.height);
    }

    /**
     * Compare this phrase box to another.
     *
     * @param o The other phrase box to compare to.
     * @return An integer >0, <0 or 0.
     */
    @Override
    public int compareTo(PhraseBox o) {
        return this.phrase.compareTo(o.phrase);
    }
}
