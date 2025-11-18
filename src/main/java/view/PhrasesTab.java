package view;

import java.awt.Component;
import java.util.function.Consumer;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class PhrasesTab extends JSplitPane {

    /**
     * Create and return a new object of this class, including setup.
     *
     * If onNewCustomPhrase is non-null, this pane will include a phrase entry box at the bottom.
     *
     * @param phrasesPanel The phrases panel to include at the top.
     * @param onNewCustomPhrase Callback for when the user submits a new custom phrase (may be null).
     */
    public static PhrasesTab create(PhrasesPanel phrasesPanel, Consumer<String> onNewCustomPhrase) {

        // Make scroll pane for phrases panel
        JScrollPane scrollPane = PhrasesPanel.newVerticalScrollPane(phrasesPanel);

        PhrasesTab tab;
        // Make phrase entry box if applicable
        if (onNewCustomPhrase == null) {
            tab = new PhrasesTab(scrollPane, null);
        } else {
            // Split pane of scroll pane and phrase entry box
            PhraseEntryBox phraseEntryBox = PhraseEntryBox.create(onNewCustomPhrase);
            tab = new PhrasesTab(scrollPane, phraseEntryBox);
        }

        tab.setOneTouchExpandable(false);
        tab.setResizeWeight(1.0);  // All extra space to top component, bottom is fixed
        tab.setVisible(true);

        return tab;
    }

    private PhrasesTab(Component newLeftComponent, Component newRightComponent) {
        super(PhrasesTab.VERTICAL_SPLIT, newLeftComponent, newRightComponent);
    }
}
