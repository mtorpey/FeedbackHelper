package view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import model.Phrase;

/**
 * Phrases Section Class.
 */
public class PhrasesSection extends JPanel implements SearchBox.Listener {

    // Instance variables
    private SearchBox searchBox;
    private JTabbedPane tabbedPane;
    private List<JScrollPane> phrasesPanelScrollPanes;
    private Map<PhraseType, PhrasesPanel> phrasesPanelsByType;

    /**
     * Create and return a new object of this class, including setup.
     */
    public static PhrasesSection create() {
        var phrasesSection = new PhrasesSection();
        phrasesSection.phrasesPanelScrollPanes = new ArrayList<>();
        phrasesSection.phrasesPanelsByType = new HashMap<PhraseType, PhrasesPanel>();

        // Set layout, contents and visibility
        phrasesSection.setLayout(new BorderLayout());
        phrasesSection.searchBox = SearchBox.create(phrasesSection);
        phrasesSection.add(phrasesSection.searchBox, BorderLayout.NORTH);
        phrasesSection.tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        phrasesSection.add(phrasesSection.tabbedPane, BorderLayout.CENTER);
        phrasesSection.setVisible(true);

        return phrasesSection;
    }

    /**
     * Add a phrases panel.
     *
     * @param phrasesPanel The phrases panel to add.
     */
    public void addPhrasesTab(PhrasesPanel phrasesPanel, Consumer<String> onNewCustomPhrase) {
        PhrasesTab tab = PhrasesTab.create(phrasesPanel, onNewCustomPhrase);
        this.phrasesPanelsByType.put(phrasesPanel.getPhraseType(), phrasesPanel);
        this.tabbedPane.addTab(phrasesPanel.getPhraseType().getPhraseTypeAsString(), tab);
        this.update();
    }

    /**
     * Reset both phrases panels.
     */
    public void resetPhrasesPanels() {
        phrasesPanelsByType.values().forEach(PhrasesPanel::clear);
        update();
    }

    /**
     * Add a frequently used phrase to the appropriate panel.
     *
     * @param phrase The phrase to add.
     */
    public void addPhraseToPanel(Phrase phrase) {
        addPhraseToPanelForType(phrase, PhraseType.FREQUENTLY_USED);
    }

    /**
     * Add a custom phrase to the appropriate panel.
     *
     * @param phrase The phrase to add.
     */
    public void addCustomPhraseToPanel(Phrase phrase) {
        addPhraseToPanelForType(phrase, PhraseType.CUSTOM);
    }

    private void addPhraseToPanelForType(Phrase phrase, PhraseType type) {
        this.phrasesPanelsByType.get(type).addPhrase(phrase);
        for (JScrollPane scrollPane : this.phrasesPanelScrollPanes) {
            SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
            scrollPane.getVerticalScrollBar().setValue(0);
        }
        this.searchBox.clear(); // new phrase added, so current search is over
    }

    /**
     * Remove a frequently used phrase from the appropriate panel.
     *
     * @param phrase The phrase to remove.
     */
    public void removePhraseFromPanel(Phrase phrase) {
        this.phrasesPanelsByType.get(PhraseType.FREQUENTLY_USED).removePhrase(phrase.getPhraseAsString());
    }

    /**
     * Refresh the panels.
     */
    private void update() {
        this.revalidate();
        this.repaint();
    }

    /**
     * Update the phrase counter of a phrase.
     *
     * @param phraseType  The phrase panel the phrase is on.
     * @param phrase      The phrase to update.
     * @param phraseCount The new usage count value.
     */
    public void updatePhraseCounter(Phrase phrase) {
        phrasesPanelsByType
            .values()
            .forEach(panel -> panel.updatePhraseCounter(phrase.getPhraseAsString(), phrase.getUsageCount()));
        this.searchBox.clear(); // phrase inserted, so current search is over
    }

    /**
     * Set the highlight of a pane.
     *
     * @param index The pane to highlight.
     */
    public void setHighlightedPane(int index) {
        if (index >= 0) {
            this.tabbedPane.setSelectedIndex(index);
        }
    }

    /**
     * Filter the displayed phrases by the contents of the search box.
     *
     * This is called whenever the search box is modified by the user, and is
     * part of the SearchBox.Listener interface.
     *
     * @param query The string to use as a filter.
     */
    public void searchBoxUpdate(String query) {
        for (PhrasesPanel panel : this.phrasesPanelsByType.values()) {
            panel.filterByString(query);
        }
    }
}
