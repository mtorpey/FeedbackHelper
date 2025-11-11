package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import configuration.UserPreferences;
import controller.AppController;
import model.Assignment;
import model.AssignmentListener;
import model.Phrase;
import model.StudentId;

/**
 * Main window for an assignment in progress, containing all components.
 */
public class FeedbackScreen implements AssignmentListener {

    //Remember Scrolling, not ideal because reset at restart, but quick fix that helps a lot
    private static Map<StudentId, Integer> scrollbarValues = new HashMap<>(); // TODO: is this the problem?

    // References to model and controller
    private final Assignment assignment;
    private final AppController controller;

    // Part of model currently selected for viewing
    private StudentId currentStudent;
    private String currentHeading;

    // Swing components
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

    /**
     * Constructor.
     *
     * @param controller The controller.
     * @param assignment The assignment.
     */
    public FeedbackScreen(AppController controller) {
        // Remember the controller, for writing changes to the model.
        this.controller = controller;

        // Subscribe to model for changes, and store a reference to it for querying.
        this.assignment = controller.registerWithModel(this);

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
     * Setup the feedback screen.
     */
    private void setupFeedbackScreen() {
        this.feedbackScreen = new JFrame("Feedback Composition");
        this.feedbackScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.feedbackScreen.setSize(1200, 800);
        this.feedbackScreen.setLayout(new BorderLayout());
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
        // Submitting a custom phrase adds it via the controller
        this.phraseEntryBox = new PhraseEntryBox(text -> controller.addCustomPhrase(currentHeading, text));
        this.phraseEntryBox.disablePhraseEntryBox();
        this.phrasesAndPhraseEntrySplitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            this.phrasesSection,
            this.phraseEntryBox
        );
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
        this.phrasesSection = new PhrasesSection();

        // Create panels
        PhrasesPanel customPhrasesPanel = new PhrasesPanel(PhraseType.CUSTOM, this::insertPhrase);
        PhrasesPanel frequentlyUsedPhrasesPanel = new PhrasesPanel(PhraseType.FREQUENTLY_USED, this::insertPhrase);

        // Add panels
        this.phrasesSection.addPhrasesPanel(customPhrasesPanel);
        this.phrasesSection.addPhrasesPanel(frequentlyUsedPhrasesPanel);

        // Start on frequently used pane
        this.phrasesSection.setHighlightedPane(1);
    }

    /**
     * Setup the preview and editor split pane.
     */
    private void setupPreviewAndEditorSplitPane() {
        this.previewAndEditorSplitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            this.previewPanelScrollPane,
            this.editorPanelScrollPane
        );
        this.previewAndEditorSplitPane.setMaximumSize(new Dimension(900, 800));
        this.previewAndEditorSplitPane.setPreferredSize(new Dimension(900, 800));
        this.previewAndEditorSplitPane.setMinimumSize(new Dimension(900, 800));
        this.previewAndEditorSplitPane.setOneTouchExpandable(true);
        this.previewAndEditorSplitPane.setDividerLocation(300);
    }

    /**
     * Setup the preview panel and put it in the scroll pane, replacing any old panel that was there.
     */
    private void setupPreviewPanel() {
        previewPanelScrollPane = new JScrollPane();

        // Re-add in case this is a reset
        if (previewAndEditorSplitPane != null) {
            previewAndEditorSplitPane.setLeftComponent(previewPanelScrollPane);
        }

        // Create preview boxes
        List<PreviewBox> previewBoxes = new ArrayList<PreviewBox>();
        this.assignment.getStudentIds().forEach(studentId -> {
            PreviewBox previewBox = new PreviewBox(
                studentId,
                assignment.getGrade(studentId),
                this.controller.getFirstLineFromDocument(studentId), // TODO: remove
                this::switchStudent
            );
            previewBoxes.add(previewBox);
        });

        // Make the preview panel scrollable
        this.previewPanel = new PreviewPanel(previewBoxes);
        this.previewPanelScrollPane.add(this.previewPanel);
        this.previewPanelScrollPane.getViewport().setView(this.previewPanel);
        this.previewPanelScrollPane.getVerticalScrollBar().setUnitIncrement(AppView.SCROLL_SPEED);

        // Set scroll position to top
        SwingUtilities.invokeLater(() -> this.previewPanelScrollPane.getVerticalScrollBar().setValue(0));

        // Start with the first one open if this is first time setup (they should be sorted)
        if (currentStudent == null) {
            currentStudent = previewBoxes.get(0).getStudentId();
        }
    }

    /**
     * Setup the editor panel.
     *
     * Should be called after setupPreviewPanel, which sets currentStudent
     */
    private void setupEditorPanel() {
        editorPanelScrollPane = new JScrollPane();

        // Create editor panel with popup menu
        editorPanel = new EditorPanel(
            controller,
            assignment.getTitle(),
            assignment.getHeadings(),
            assignment.getLineMarker(),
            this::switchSection,
            this::updateFeedbackSection
        );
        this.editingPopupMenu = new EditingPopupMenu();
        this.editorPanel.registerPopupMenu(this.editingPopupMenu);

        // Set the document data
        loadEditorPanelData();

        // Make the panel scrollable
        this.editorPanelScrollPane.add(this.editorPanel);
        this.editorPanelScrollPane.getViewport().setView(this.editorPanel);
        this.editorPanelScrollPane.getVerticalScrollBar().setUnitIncrement(AppView.SCROLL_SPEED);

        // TODO: does this really need an invokeLater?
        SwingUtilities.invokeLater(() -> this.editorPanelScrollPane.getVerticalScrollBar().setValue(0));
    }

    /** Update the editor panel with the ID, feedback and grade for the current student in the model. */
    private void loadEditorPanelData() {
        editorPanel.setStudentId(currentStudent);
        for (String heading : assignment.getHeadings()) {
            editorPanel.setSectionContents(heading, assignment.getSectionContents(currentStudent, heading));
        }
        editorPanel.setGrade(assignment.getGrade(currentStudent));
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
        JMenuItem addStudentOption = new JMenuItem("Add new student");
        JMenuItem exportDocsOption = new JMenuItem("Export grades and feedback documents");
        JMenuItem visGradesOption = new JMenuItem("Visualise grades");

        // Create the theme preferences menu
        JMenu preferencesMenu = createPreferencesMenu();

        // Create the help menu and items
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutOption = new JMenuItem("About");

        // Save option
        saveOption.addActionListener(e -> saveAssignmentForCurrentStudent());

        // Add student option
        addStudentOption.addActionListener(e -> addNewStudent());

        // Export grades and documents option
        exportDocsOption.addActionListener(e -> controller.exportFeedbackAndGrades());

        // Visualise grades option
        visGradesOption.addActionListener(e -> controller.visualiseGrades());

        // Show the 'about' dialog window
        aboutOption.addActionListener(l -> new AboutDialog(feedbackScreen));

        // Add all options to menus
        fileMenu.add(saveOption);
        fileMenu.add(addStudentOption);
        fileMenu.add(exportDocsOption);
        fileMenu.add(visGradesOption);
        helpMenu.add(aboutOption);

        // Add the menu bar to the screen
        menuBar.add(fileMenu);
        menuBar.add(preferencesMenu);
        menuBar.add(helpMenu);
        this.feedbackScreen.add(menuBar, BorderLayout.PAGE_START);
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
        } catch (
            ClassNotFoundException
            | InstantiationException
            | IllegalAccessException
            | UnsupportedLookAndFeelException e
        ) {
            handleError("Error setting theme", e);
        }
        SwingUtilities.updateComponentTreeUI(feedbackScreen);
    }

    private void saveAssignmentForCurrentStudent() {
        Map<String, String> sections = editorPanel.getSections();
        double grade = editorPanel.getGrade();
        controller.updateFeedbackAndGrade(currentStudent, sections, grade); // This saves to disk.
        this.previewPanel.updatePreviewBox(currentStudent, controller.getFirstLineFromDocument(currentStudent), grade);
    }

    private void addNewStudent() {
        String input = JOptionPane.showInputDialog(this.feedbackScreen, "Enter the new student id");
        try {
            controller.addNewStudent(input);
        } catch (IllegalArgumentException e) {
            handleError("Invalid student ID '" + input + "'.", e);
        }
    }

    /**
     * Change the student currently selected in the view.
     *
     * @param studentId The ID of the student we are now viewing.
     */
    private void switchStudent(StudentId studentId) {
        // Wrap up from last student
        scrollbarValues.put(currentStudent, editorPanelScrollPane.getVerticalScrollBar().getValue());
        saveAssignmentForCurrentStudent();
        previewPanel.updatePreviewBox(
            currentStudent,
            controller.getFirstLineFromDocument(currentStudent),
            assignment.getGrade(currentStudent)
        );
        previewPanel.unhighlightPreviewBox(currentStudent);

        // Switch to new student
        currentStudent = studentId;
        loadEditorPanelData();
        previewPanel.highlightPreviewBox(studentId);

        // Force scroll bar
        editorPanelScrollPane.getVerticalScrollBar().setValue(scrollbarValues.getOrDefault(studentId, 0));

        // Refresh UI
        this.previewPanel.repaint();
        this.previewPanel.revalidate();
    }

    /**
     * Change the heading currently selected in the view.
     *
     * @param heading The heading of the section we have selected.
     */
    private void switchSection(String heading) {
        // Save work so far
        saveAssignmentForCurrentStudent();

        // Change to the new heading
        currentHeading = heading;

        // Old checking code, might be obselete
        if (this.phraseEntryBox == null) {
            handleError("No phrase entry box found!", new Exception("This should never happen."));
        }

        // Clear phrases
        phrasesSection.resetPhrasesPanels();

        // Add new phrases
        assignment.getPhrasesForHeading(currentHeading).forEach(phrase -> handlePhraseAdded(heading, phrase));
        assignment.getCustomPhrases(currentHeading).forEach(phrase -> handleCustomPhraseAdded(heading, phrase));

        // TODO: handle phrase entry box
        //this.phraseEntryBox.enablePhraseEntryBox();
        //this.phraseEntryBox.disablePhraseEntryBox();
    }

    private void updateFeedbackSection(String heading, String text) {
        controller.updateFeedbackSection(currentStudent, heading, text);
    }

    private void insertPhrase(String phrase) {
        editorPanel.insertPhraseIntoFeedbackBox(currentHeading, phrase);
    }

    //
    // HANDLING MODEL UPDATES
    //
    @Override
    public void handleHeadingsUpdated(List<String> headings) {
        SwingUtilities.invokeLater(() -> editorPanel.updateHeadings(headings));
    }

    @Override
    public void handleNewStudent(StudentId studentId) {
        SwingUtilities.invokeLater(() -> {
            setupPreviewPanel();
            // for (PreviewBox pb : previewBoxes) {
            //     this.previewPanel.unhighlightPreviewBox(pb.getHeading());
            // }
            switchStudent(studentId);
        });
    }

    @Override
    public void handlePhraseAdded(String heading, Phrase phrase) {
        SwingUtilities.invokeLater(() -> {
            if (currentHeading == heading) {
                phrasesSection.addPhraseToPanel(phrase);
            } else {
                handleError("Phrase added for another section", new Exception("This should never happen."));
            }
        });
    }

    @Override
    public void handlePhraseDeleted(String heading, Phrase phrase) {
        SwingUtilities.invokeLater(() -> {
            if (currentHeading == heading) {
                phrasesSection.removePhraseFromPanel(phrase);
            } else {
                handleError("Phrase deleted for another section", new Exception("This should never happen."));
            }
        });
    }

    @Override
    public void handlePhraseCounterUpdated(String heading, Phrase phrase) {
        SwingUtilities.invokeLater(() -> {
            if (currentHeading == heading) {
                phrasesSection.updatePhraseCounter(phrase);
            } else {
                handleError("Phrase updated for another section", new Exception("This should never happen."));
            }
        });
    }

    @Override
    public void handleCustomPhraseAdded(String heading, Phrase phrase) {
        SwingUtilities.invokeLater(() -> {
            if (currentHeading == heading) {
                phrasesSection.addCustomPhraseToPanel(phrase);
            } else {
                handleError("Custom phrase added for another section", new Exception("This should never happen."));
            }
        });
    }

    @Override
    public void handleInfo(String message) {
        // TODO: display in bottom bar?
    }

    @Override
    public void handleError(String description, Exception exception) {
        String message = description + ": " + exception.toString();
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this.feedbackScreen, message, "Error!", JOptionPane.ERROR_MESSAGE)
        );
    }
}
