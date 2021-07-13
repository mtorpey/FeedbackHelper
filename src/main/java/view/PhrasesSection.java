package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhrasesSection extends JPanel {

    private JTabbedPane tabbedPane;
    private List<JScrollPane> phrasesPanelsScrollPanes;

    private Map<PhraseType, PhrasesPanel> phrasesPanelsByType;

    public PhrasesSection() {
        this.tabbedPane = new JTabbedPane();
        this.phrasesPanelsScrollPanes = new ArrayList<JScrollPane>();
        this.phrasesPanelsByType = new HashMap<PhraseType, PhrasesPanel>();

        setupTabbedPane();

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(tabbedPane);
        this.setVisible(true);
    }

    public void addPhrasesPanel(PhrasesPanel phrasesPanel) {
        JScrollPane phrasesPanelScrollPane = new JScrollPane(phrasesPanel);
        phrasesPanelsByType.put(phrasesPanel.getPhraseType(), phrasesPanel);
        tabbedPane.addTab(phrasesPanel.getPhraseType().getPhraseTypeAsString(), phrasesPanelScrollPane);
        updatePhrasesSection();
    }

    public void addPhraseToPanel(String phrase, PhraseType phrasePanelType) {
        this.phrasesPanelsByType.get(phrasePanelType).addPhrase(phrase);
    }

    private void updatePhrasesSection() {
        this.revalidate();
        this.repaint();
    }

    public void setupTabbedPane() {
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getTabCount() > 0) {
                tabbedPane.setForeground(Color.BLACK);
            }

            for (int i = 0; i< tabbedPane.getTabCount(); i++) {
                tabbedPane.setBackgroundAt(i, Color.LIGHT_GRAY);
            }

            if (tabbedPane.getSelectedIndex() >= 0) {
                tabbedPane.setBackgroundAt(tabbedPane.getSelectedIndex(), Color.BLUE);
            }
        });
    }

}
