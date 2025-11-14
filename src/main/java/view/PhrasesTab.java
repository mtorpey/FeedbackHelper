package view;

import java.awt.Dimension;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class PhrasesTab extends JPanel {

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param phraseType The type of phrases to show on the panel.
     * @param onInsertPhrase Callback for when the user wishes to insert a phrase.
     * @param onNewCustomPhrase Callback for when the user submits a new custom phrase (may be null).
     */
    public static PhrasesTab create(
        PhrasesPanel phrasesPanel,
        Consumer<String> onNewCustomPhrase
    ) {
        PhrasesTab tab = new PhrasesTab();

        // Make scroll pane for phrases panel
        JScrollPane scrollPane = new JScrollPane(phrasesPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(AppView.SCROLL_SPEED);

        // Make phrase entry box if applicable
        if (onNewCustomPhrase == null) {
            // Just the scroll pane
            tab.add(scrollPane);
        } else {
            // Split pane of scroll pane and phrase entry box
            PhraseEntryBox phraseEntryBox = PhraseEntryBox.create(onNewCustomPhrase);
            JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                scrollPane,
                phraseEntryBox
            );
            splitPane.setOneTouchExpandable(false);
            splitPane.setDividerLocation(600);
            splitPane.setMaximumSize(new Dimension(300, 800));
            splitPane.setPreferredSize(new Dimension(300, 800));
            splitPane.setMinimumSize(new Dimension(300, 800));
            tab.add(splitPane);
        }

        // Set layout and visbility
        tab.setVisible(true);

        return tab;
    }
}
