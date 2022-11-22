package view;

import controller.IAppController;
import model.Sentiment;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.net.URL;

/**
 * Phrase Box Class.
 */
public class PhraseBox extends JPanel implements Comparable<PhraseBox> {

    // Instance variables
    private final IAppController controller;
    private String phrase;
    private String phraseSentiment;
    private int usageCount;
    private JTextArea phraseTextArea;
    private JButton insertButton;
    private JLabel sentimentLabel;
    private JLabel usageCountLabel;

    // Green arrow icon
    private static ImageIcon insertIcon = loadInsertIcon();

    /**
     * Constructor.
     *
     * @param controller The controller.
     * @param phrase     The phrase to display.
     * @param usageCount The usage count of the phrase.
     */
    public PhraseBox(IAppController controller, String phrase, int usageCount) {
        this.controller = controller;
        this.phrase = phrase;
        this.phraseTextArea = new JTextArea();
        this.phraseSentiment = controller.getPhraseSentiment(phrase);
        this.usageCount = usageCount;

        this.insertButton = new JButton(insertIcon);

        this.setLayout(new BorderLayout());

        setupInsertButton();
        setupPhraseTextArea();
        setupSentimentLabel();
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
    private void setupInsertButton() {
        this.insertButton.addActionListener(l -> {
            this.controller.insertPhraseIntoCurrentFeedbackBox(this.phrase);
            this.controller.saveFeedbackDocument(this.controller.getCurrentDocumentInView());
        });
        this.add(this.insertButton, BorderLayout.LINE_START);
    }

    /**
     * Setup the sentiment label.
     */
    private void setupSentimentLabel() {
        ImageIcon icon = null;
        URL emojiFilePath = null;

        // Pick the relevant emoji
        // Emoji images are custom made on powerpoint - no licence needed.
        if (this.phraseSentiment.equals(Sentiment.NEUTRAL.getSentimentAsString())) {
            emojiFilePath = this.getClass().getResource("/neutral.png");
        } else if (this.phraseSentiment.equals(Sentiment.POSITIVE.getSentimentAsString()) || this.phraseSentiment.equals(Sentiment.VERY_POSITIVE.getSentimentAsString())) {
            emojiFilePath = this.getClass().getResource("/positive.png");
        } else {
            emojiFilePath = this.getClass().getResource("/negative.png");
        }

        this.sentimentLabel = new JLabel(new ImageIcon(new ImageIcon(emojiFilePath).getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT)));
        this.add(this.sentimentLabel, BorderLayout.LINE_END);
    }

    /**
     * Setup the usage count label.
     */
    private void setupUsageCountLabel() {
        this.usageCountLabel = new JLabel(String.valueOf(this.usageCount));
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
        this.phraseTextArea.setText(this.phrase);
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
    public String getPhrase() {
        return this.phrase;
    }

    /**
     * Get the usage count of the phrase.
     *
     * @return The usage count of the phrase.
     */
    public int getUsageCount() {
        return this.usageCount;
    }

    /**
     * Set the usage count of the phrase.
     *
     * @param usageCount The usage count of the phrase.
     */
    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
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
        String contents = this.getPhrase().toLowerCase();
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
        return o.getUsageCount() - this.getUsageCount();
    }

}
