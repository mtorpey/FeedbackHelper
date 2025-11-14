package view;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.Phrase;

/**
 * Phrases Panel Class.
 */
public class PhrasesPanel extends JPanel {

    // Instance variables
    private Consumer<String> onInsertPhrase;
    private PhraseType phraseType;
    private List<PhraseBox> phraseBoxes;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param phraseType The type of phrases to show on the panel.
     * @param onInsertPhrase Callback for when the user wishes to insert a phrase.
     */
    public static PhrasesPanel create(PhraseType phraseType, Consumer<String> onInsertPhrase) {
        // Create panel and data structure
        var panel = new PhrasesPanel(phraseType, onInsertPhrase);
        panel.phraseBoxes = new LinkedList<PhraseBox>();

        // Set layout and visbility
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setVisible(true);

        return panel;
    }

    private PhrasesPanel(PhraseType phraseType, Consumer<String> onInsertPhrase) {
        this.phraseType = phraseType;
        this.onInsertPhrase = onInsertPhrase;
    }

    /**
     * Get the phrase type of the panel.
     *
     * @return The phrase type of the panel.
     */
    public PhraseType getPhraseType() {
        return this.phraseType;
    }

    /**
     * Add a phrase to the panel.
     *
     * @param phrase      The phrase to add.
     * @param phraseCount The usage count of the phrase.
     */
    public void addPhrase(Phrase phrase) {
        // Create the new box
        PhraseBox phraseBox = PhraseBox.create(phrase, onInsertPhrase);

        // Insert it in the correct place in the list
        phraseBoxes.add(phraseBox);
        phraseBoxes.sort(null);

        // Add it in the correct place in the panel
        int pos = phraseBoxes.indexOf(phraseBox);
        this.add(phraseBox, pos);
        this.update();
    }

    /**
     * Remove a phrase from the panel.
     *
     * @param phrase The phrase to remove.
     */
    public void removePhrase(String phrase) {
        // Find the index of the phrase box
        int toRemove = 0;
        for (int i = 0; i < this.phraseBoxes.size(); i++) {
            if (this.phraseBoxes.get(i).getPhraseText().equals(phrase)) {
                toRemove = i;
            }
        }

        // Remove the component and then remove it from the list
        this.remove(this.phraseBoxes.get(toRemove));
        this.phraseBoxes.remove(toRemove);
        this.update();
    }

    /**
     * Update the phrase counter of a phrase.
     *
     * @param phrase The phrase to update.
     * @param phraseCount The new usage count value.
     */
    public void updatePhraseCounter(String phrase, long phraseCount) {
        // Find the phrase to update
        for (PhraseBox phraseBox : phraseBoxes) {
            if (phraseBox.getPhraseText().equals(phrase)) {
                phraseBox.setUsageCount(phraseCount);
            }
        }

        // Sort the list
        this.removeAll();
        Collections.sort(this.phraseBoxes);
        this.phraseBoxes.forEach(this::add);
        this.update();
    }

    /**
     * Clear the panel.
     */
    public void clear() {
        this.removeAll();
        this.phraseBoxes.clear();
        this.update();
    }

    /**
     * Refresh the panel.
     */
    private void update() {
        this.revalidate();
        this.repaint();
    }

    /**
     * Show phrase boxes that match a given search query, and hide all others.
     *
     * @param query The search query to filter by.
     */
    public void filterByString(String query) {
        for (PhraseBox phraseBox : this.phraseBoxes) {
            phraseBox.setVisibleBySearchQuery(query);
        }
    }
}
