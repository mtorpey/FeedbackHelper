package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
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

    // Green arrow icon
    private static ImageIcon insertIcon = loadInsertIcon();

    /**
     * Constructor.
     *
     * @param phrase     The phrase to display.
     * @param usageCount The usage count of the phrase.
     * @param onInsertPhrase Callback for when the user wishes to insert a phrase.
     */
    public PhraseBox(Phrase phrase, Consumer<String> onInsertPhrase) {
        this.phrase = phrase;
        this.phraseTextArea = new JTextArea();

        this.insertButton = new JButton(insertIcon);

        this.setLayout(new BorderLayout());

        setupInsertButton(onInsertPhrase);
        setupPhraseTextArea();
        setupUsageCountLabel();

        this.setMaximumSize(new Dimension(300, 100));
        this.setVisible(true);
    }

    /**
     * Load the green arrow "insert" icon.
     * To be used once, statically, at the beginning of the program.
     */
    private static ImageIcon loadInsertIcon() {
        // Green arrow image from: https://commons.wikimedia.org/wiki/File:Eo_circle_green_arrow-left.svg
        // (creative commons license)
        URL url = PhraseBox.class.getResource("/green_arrow.png");
        Image image = new ImageIcon(url).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        return new ImageIcon(image);
    }

    /**
     * Setup the insert button.
     */
    private void setupInsertButton(Consumer<String> onInsertPhrase) {
        this.insertButton.addActionListener(l -> onInsertPhrase.accept(phrase.getPhraseAsString()));
        this.add(this.insertButton, BorderLayout.LINE_START);
    }

    /**
     * Setup the usage count label.
     */
    private void setupUsageCountLabel() {
        this.usageCountLabel = new JLabel(String.valueOf(phrase.getUsageCount()));
        this.add(this.usageCountLabel, BorderLayout.BEFORE_FIRST_LINE);
    }

    /**
     * Setup the phrase text area.
     */
    private void setupPhraseTextArea() {
        // Set properties
        this.phraseTextArea.setRows(5);
        this.phraseTextArea.setColumns(10);
        this.phraseTextArea.setBorder(BorderCreator.createEmptyBorderLeavingBottom(BorderCreator.PADDING_10_PIXELS));
        this.phraseTextArea.setText(phrase.getPhraseAsString());
        this.phraseTextArea.setLineWrap(true);
        this.phraseTextArea.setWrapStyleWord(true);
        this.phraseTextArea.setEditable(false);

        // Add the text area and some padding to the bottom of the panel
        this.add(this.phraseTextArea, BorderLayout.CENTER);
        this.setBorder(BorderCreator.createEmptyBorderLeavingBottom(BorderCreator.PADDING_20_PIXELS));
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
