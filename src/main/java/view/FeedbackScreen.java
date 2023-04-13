package view;

import controller.IAppController;
import model.Assignment;
import model.LinkedPhrases;
import model.Phrase;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import configuration.UserPreferences;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * Feedback Screen Class.
 */
public class FeedbackScreen implements PropertyChangeListener {
    // Class variable
    private static final int INSIGHT_LEVEL = 3;
    
    //Remember Scrolling, not ideal because reset at restart, but quick fix that helps a lot
    private static Map<String, Integer> scrollbarValues = new HashMap();

    // Instance variables
    private final IAppController controller;
    private JFrame feedbackScreen;
    private JPanel feedbackScreenPanel;
    private JSplitPane previewAndEditorSplitPane;
    private JScrollPane previewPanelScrollPane;
    private PreviewPanel previewPanel;
    private JScrollPane editorPanelScrollPane;
    private EditorPanel editorPanel;
    private EditingPopupMenu editingPopupMenu;
    private JSplitPane phrasesAndPhraseEntrySplitPane;
    private PhrasesSection phrasesSection;
    private PhraseEntryBox phraseEntryBox;
    private GridBagConstraints gridBagConstraints;
    private Assignment assignment;

    /**
     * Constructor.
     *
     * @param controller The controller.
     * @param assignment The assignment.
     */
    public FeedbackScreen(IAppController controller, Assignment assignment) {
        this.controller = controller;
        this.controller.registerWithModel(this);
        this.assignment = assignment;

        // Setup components
        setupFeedbackScreen();
        setupFeedbackScreenPanel();
        setupPreviewPanel();
        setupEditorPanel();
        setupPhrasesSection();
        setupPreviewAndEditorSplitPane();
        setupPhrasesAndPhraseEntrySplitPane();
        setupMenuBar();
        positionEditorSplitPane();
        positionPhrasesSplitPane();

        // Add the main panel to the screen and set visibility
        this.feedbackScreen.add(this.feedbackScreenPanel, BorderLayout.CENTER);
        this.feedbackScreen.setVisible(true);
    }

    /**
     * Position the phrases split pane with the gridbag constraints.
     */
    private void positionPhrasesSplitPane() {
        this.gridBagConstraints.fill = GridBagConstraints.BOTH;
        this.gridBagConstraints.gridx = 2;
        this.gridBagConstraints.gridy = 0;
        this.feedbackScreenPanel.add(this.phrasesAndPhraseEntrySplitPane, this.gridBagConstraints);
    }

    /**
     * Position the editor split pane with the gridbag constraints.
     */
    private void positionEditorSplitPane() {
        this.gridBagConstraints.fill = GridBagConstraints.BOTH;
        this.gridBagConstraints.gridx = 0;
        this.gridBagConstraints.gridy = 0;
        this.feedbackScreenPanel.add(this.previewAndEditorSplitPane, this.gridBagConstraints);
    }

    /**
     * Setup the feedback screen.
     */
    private void setupFeedbackScreen() {
        this.feedbackScreen = new JFrame("Feedback Composition");
        this.feedbackScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.feedbackScreen.setSize(1200, 800);
        this.feedbackScreen.setLayout(new BorderLayout());

        // Centre the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - this.feedbackScreen.getWidth()) / 2;
        int y = 20;  // Just below top of screen
        this.feedbackScreen.setLocation(x, y);
    }

    /**
     * Setup the feedback screen panel.
     */
    private void setupFeedbackScreenPanel() {
        this.feedbackScreenPanel = new JPanel(new GridBagLayout());
        this.gridBagConstraints = new GridBagConstraints();
        this.gridBagConstraints.weightx = 1.0;
        this.gridBagConstraints.weighty = 1.0;
    }

    /**
     * Setup the phrases section and phrase entry box.
     */
    private void setupPhrasesAndPhraseEntrySplitPane() {
        this.phraseEntryBox = new PhraseEntryBox(this.controller);
        this.phraseEntryBox.disablePhraseEntryBox();
        this.phrasesAndPhraseEntrySplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.phrasesSection, this.phraseEntryBox);
        this.phrasesAndPhraseEntrySplitPane.setOneTouchExpandable(false);
        this.phrasesAndPhraseEntrySplitPane.setDividerLocation(600);
        this.phrasesAndPhraseEntrySplitPane.setMaximumSize(new Dimension(300, 800));
        this.phrasesAndPhraseEntrySplitPane.setPreferredSize(new Dimension(300, 800));
        this.phrasesAndPhraseEntrySplitPane.setMinimumSize(new Dimension(300, 800));
    }

    /**
     * Setup the phrase panels and the phrases section.
     */
    private void setupPhrasesSection() {
        this.phrasesSection = new PhrasesSection(this.controller);

        // Create panels
        PhrasesPanel customPhrasesPanel = new PhrasesPanel(this.controller, PhraseType.CUSTOM);
        PhrasesPanel frequentlyUsedPhrasesPanel = new PhrasesPanel(this.controller, PhraseType.FREQUENTLY_USED);
        PhrasesPanel insightsPhrasesPanel = new PhrasesPanel(this.controller, PhraseType.INSIGHTS);

        // Add panels
        this.phrasesSection.addPhrasesPanel(customPhrasesPanel);
        this.phrasesSection.addPhrasesPanel(frequentlyUsedPhrasesPanel);
        this.phrasesSection.addPhrasesPanel(insightsPhrasesPanel);

        // Start on frequently used pane
        this.phrasesSection.setHighlightedPane(1);
        this.controller.setCurrentPhrasePanelInView(PhraseType.FREQUENTLY_USED);
    }

    /**
     * Setup the preview and editor split pane.
     */
    private void setupPreviewAndEditorSplitPane() {
        this.previewAndEditorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.previewPanelScrollPane, this.editorPanelScrollPane);
        this.previewAndEditorSplitPane.setMaximumSize(new Dimension(900, 800));
        this.previewAndEditorSplitPane.setPreferredSize(new Dimension(900, 800));
        this.previewAndEditorSplitPane.setMinimumSize(new Dimension(900, 800));
        this.previewAndEditorSplitPane.setOneTouchExpandable(true);
        this.previewAndEditorSplitPane.setDividerLocation(300);
    }

    /**
     * Setup the editor panel.
     */
    private void setupEditorPanel() {
        this.editorPanelScrollPane = new JScrollPane();

        // Create editor panel with popup menu
        this.editorPanel = new EditorPanel(this.controller, this.assignment.getAssignmentTitle(), this.assignment.getAssignmentHeadings());
        this.editingPopupMenu = new EditingPopupMenu();
        this.editorPanel.registerPopupMenu(this.editingPopupMenu);

        // Set the document data if it exists
        this.editorPanel.setData(this.assignment.getFeedbackDocumentForStudent(this.controller.getCurrentDocumentInView()));

        // Make the panel scrollable
        this.editorPanelScrollPane.add(this.editorPanel);
        this.editorPanelScrollPane.getViewport().setView(this.editorPanel);
        this.editorPanelScrollPane.getVerticalScrollBar().setUnitIncrement(AppView.SCROLL_SPEED);

        SwingUtilities.invokeLater(() -> this.editorPanelScrollPane.getVerticalScrollBar().setValue(0));
    }

    /**
     * Setup the preview panel.
     */
    private void setupPreviewPanel() {
        this.previewPanelScrollPane = new JScrollPane();

        // Create preview boxes
        List<PreviewBox> previewBoxes = new ArrayList<PreviewBox>();
        this.assignment.getFeedbackDocuments().forEach(feedbackDocument -> {
            PreviewBox previewBox = new PreviewBox(this.controller, feedbackDocument.getStudentId(), feedbackDocument.getGrade(), this.controller.getFirstLineFromDocument(this.assignment, feedbackDocument.getStudentId()));
            previewBox.setAssignment(this.assignment);
            previewBoxes.add(previewBox);
        });

        // Order the preview boxes by the id if possible
        Collections.sort(previewBoxes);
        this.controller.setCurrentDocumentInView(previewBoxes.get(0).getHeading());

        // Make the preview panel scrollable
        this.previewPanel = new PreviewPanel(previewBoxes);
        this.previewPanelScrollPane.add(this.previewPanel);
        this.previewPanelScrollPane.getViewport().setView(this.previewPanel);
        this.previewPanelScrollPane.getVerticalScrollBar().setUnitIncrement(AppView.SCROLL_SPEED);

        // Set scroll position to top
        // The following line is adapted from: https://stackoverflow.com/questions/1166072/setting-scroll-bar-on-a-jscrollpane
        SwingUtilities.invokeLater(() -> this.previewPanelScrollPane.getVerticalScrollBar().setValue(0));
    }

    /**
     * Setup the menubar.
     */
    private void setupMenuBar() {
        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create the file menu and items
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveOption = new JMenuItem("Save current document");
        JMenuItem exportDocsOption = new JMenuItem("Export grades and feedback documents");
        JMenuItem visGradesOption = new JMenuItem("Visualise grades");
        JMenuItem summaryOption = new JMenuItem("Create summary");

        // Create the theme preferences menu
        JMenu preferencesMenu = createPreferencesMenu();
        
        // Create the help menu and items
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutOption = new JMenuItem("About Feedback Helper");

        // Save operation
        saveOption.addActionListener(l -> {
            JOptionPane.showMessageDialog(this.feedbackScreen, "Saving document for student: " + this.controller.getCurrentDocumentInView());
            this.controller.saveFeedbackDocument(controller.getCurrentDocumentInView());
        });

        // Export grades and documents option
        exportDocsOption.addActionListener(l -> {
            // Export feedback documents
            this.controller.exportFeedbackDocuments(this.assignment);
            // Export grades
            this.controller.exportGrades(this.assignment);
            JOptionPane.showMessageDialog(this.feedbackScreen,
                    "Exporting assignment grades and feedback documents... \n" +
                            "Please check the directory: " + this.assignment.getAssignmentDirectoryPath());
        });

        // Visualise grades option
        visGradesOption.addActionListener(l -> {
            this.controller.visualiseGrades(this.assignment);
            JOptionPane.showMessageDialog(this.feedbackScreen, "Generating visualisation of assignment grades...");
        });

        // Visualise grades option
        summaryOption.addActionListener(l -> {
            Map<String, List<String>> summary = this.controller.getSummary(this.assignment);
            DocumentViewer documentViewer = new DocumentViewer(this.controller, "Summary of All Feedback Documents");
            documentViewer.displayData(summary, this.assignment.getAssignmentHeadings());
        });

        // Show the 'about' dialog window
        aboutOption.addActionListener(l -> {
            AboutDialog aboutDialog = new AboutDialog(this.feedbackScreen);
            aboutDialog.setVisible(true);
        });


        // Add all options to menu
        fileMenu.add(saveOption);
        fileMenu.add(exportDocsOption);
        fileMenu.add(visGradesOption);
        fileMenu.add(summaryOption);

        helpMenu.add(aboutOption);

        // Add the menu bar to the screen
        menuBar.add(fileMenu);
        menuBar.add(preferencesMenu);
        menuBar.add(helpMenu);
        this.feedbackScreen.add(menuBar, BorderLayout.PAGE_START);
    }

    private JMenu createPreferencesMenu() {
        JMenu themeMenu = new JMenu("Theme");
        for (LookAndFeelInfo theme : UIManager.getInstalledLookAndFeels()) {
            JMenuItem item = new JMenuItem(theme.getName());
            item.addActionListener(e -> setTheme(theme.getClassName()));
            themeMenu.add(item);
        }
        return themeMenu;
    }

    private void setTheme(String name) {
        System.out.println("Setting theme " + name);
        UserPreferences.setTheme(name);
        try {
            UIManager.setLookAndFeel(name);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(feedbackScreen);
    }

    /**
     * Listen for change messages from the model and perform appropriate
     * action to the GUI to reflect the changes in the model.
     *
     * @param event The incoming message from the model.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // Perform action based on the incoming message
        switch (event.getPropertyName()) {
            case "docViewChange":
                scrollbarValues.put(controller.getLastDocumentInView(), 
                        this.editorPanelScrollPane.getVerticalScrollBar().getValue());
                performDocumentViewChange(event);
                SwingUtilities.invokeLater(() -> this.editorPanelScrollPane.getVerticalScrollBar()
                        .setValue(scrollbarValues
                                .getOrDefault(event.getNewValue(), 0)));
                break;
            case "saveDoc":
                performDocumentSave(event);
                break;
            case "insertPhrase":
                performInsertPhrase(event);
                break;
            case "newPhrase":
                performAddNewPhrase(event, PhraseType.FREQUENTLY_USED);
                break;
            case "deletePhrase":
                performDeletePhrase(event);
                break;
            case "updatePhraseCounter":
                performUpdatePhrase(event);
                break;
            case "resetPhrasesPanel":
                performResetPanel(event);
                break;
            case "newCustomPhrase":
                performAddNewPhrase(event, PhraseType.CUSTOM);
                break;
            case "newLinkedPhrases":
                performAddNewLinkedPhrase(event);
                break;
            case "phrasePanelChange":
                performPhrasePanelChange(event);
                break;
            case "error":
                displayError(event);
                break;
            default:
                System.out.println("Received unknown message!");
                System.out.println(event.getNewValue());
                break;
        }
    }

    /**
     * Display an error message.
     *
     * @param event The event notification from the model.
     */
    private void displayError(PropertyChangeEvent event) {
        String errorMessage = (String) event.getNewValue();
        JOptionPane.showMessageDialog(this.feedbackScreen, errorMessage, "Error!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Perform a phrase panel change.
     *
     * @param event The event notification from the model.
     */
    private void performPhrasePanelChange(PropertyChangeEvent event) {
        if (this.phraseEntryBox != null) {
            PhraseType panelInView = (PhraseType) event.getNewValue();
            // Show custom phrases
            if (panelInView == PhraseType.CUSTOM) {
                this.phrasesSection.resetPhrasesPanel(PhraseType.CUSTOM);
                this.controller.showCustomPhrases();
                this.phraseEntryBox.enablePhraseEntryBox();
            } else {
                this.phraseEntryBox.disablePhraseEntryBox();
            }

            // Show insights
            if (panelInView == PhraseType.INSIGHTS) {
                this.controller.resetPhrasesPanel(PhraseType.INSIGHTS);
                this.controller.showInsights();
            }
        }
    }

    /**
     * Reset the panels.
     */
    private void performResetPanel(PropertyChangeEvent event) {
        PhraseType phrasePanel = (PhraseType) event.getNewValue();
        this.phrasesSection.resetPhrasesPanel(phrasePanel);
    }

    /**
     * Perform an update to an existing phrase.
     *
     * @param event The event notification from the model.
     */
    private void performUpdatePhrase(PropertyChangeEvent event) {
        Phrase phraseToUpdate = (Phrase) event.getNewValue();
        this.phrasesSection.updatePhraseCounter(PhraseType.FREQUENTLY_USED, phraseToUpdate.getPhraseAsString(), phraseToUpdate.getUsageCount());
        this.phrasesSection.updatePhraseCounter(PhraseType.CUSTOM, phraseToUpdate.getPhraseAsString(), phraseToUpdate.getUsageCount());
        this.phrasesSection.updatePhraseCounter(PhraseType.INSIGHTS, phraseToUpdate.getPhraseAsString(), phraseToUpdate.getUsageCount());
    }

    /**
     * Delete a phrase from the frequently used panel.
     *
     * @param event The event notification from the model.
     */
    private void performDeletePhrase(PropertyChangeEvent event) {
        Phrase phraseToDelete = (Phrase) event.getNewValue();
        this.phrasesSection.removePhraseFromPanel(phraseToDelete.getPhraseAsString(), PhraseType.FREQUENTLY_USED);
    }

    /**
     * Add a phrase to the given panel.
     *
     * @param event      The event notification from the model.
     * @param phraseType The panel to add the phrase to.
     */
    private void performAddNewPhrase(PropertyChangeEvent event, PhraseType phraseType) {
        Phrase newPhrase = (Phrase) event.getNewValue();
        this.phrasesSection.addPhraseToPanel(newPhrase.getPhraseAsString(), newPhrase.getUsageCount(), phraseType);
    }

    /**
     * Add a phrase to the given panel.
     *
     * @param event The event notification from the model.
     */
    private void performAddNewLinkedPhrase(PropertyChangeEvent event) {
        LinkedPhrases newLinkedPhrases = (LinkedPhrases) event.getNewValue();
        if (newLinkedPhrases.getCount() >= INSIGHT_LEVEL) {
            this.phrasesSection.addInsightToInsightPanel(newLinkedPhrases);
        }
    }

    /**
     * Insert a phrase into the feedback box being currently edited.
     *
     * @param event The event notification from the model.
     */
    private void performInsertPhrase(PropertyChangeEvent event) {
        String phrase = (String) event.getNewValue();
        String heading = this.controller.getCurrentHeadingBeingEdited();
        this.editorPanel.insertPhraseIntoFeedbackBox(phrase, heading);
    }

    /**
     * Save a document.
     *
     * @param event The event notification from the model.
     */
    private void performDocumentSave(PropertyChangeEvent event) {
        String studentId = (String) event.getNewValue();
        Map<String, String> headingsAndData = this.editorPanel.saveDataAsMap();
        double grade = this.editorPanel.getGrade();
        if (grade >= 0) {
            this.controller.saveFeedbackDocument(this.assignment, studentId, headingsAndData, grade);
            this.previewPanel.updatePreviewBox(studentId, this.controller.getFirstLineFromDocument(this.assignment, studentId), grade);
        }
    }

    /**
     * Change the document in the current view.
     *
     * @param event The event notification from the model.
     */
    private void performDocumentViewChange(PropertyChangeEvent event) {
        String newDocInView = (String) event.getNewValue();
        this.editorPanel.setData(this.assignment.getFeedbackDocumentForStudent(newDocInView));

        // Update the preview boxes
        if (this.controller.getLastDocumentInView() != null) {
            this.previewPanel.updatePreviewBoxLine(
                    this.controller.getLastDocumentInView(),
                    this.controller.getFirstLineFromDocument(this.assignment, this.controller.getLastDocumentInView())
            );
            this.previewPanel.unhighlightPreviewBox(this.controller.getLastDocumentInView());
        }
        this.previewPanel.highlightPreviewBox(newDocInView);

        // Refresh UI
        this.previewPanel.repaint();
        this.previewPanel.revalidate();
    }

}
