package view;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
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
public class FeedbackScreen extends JFrame implements AssignmentListener {

    // References to other parts of program
    private Assignment assignment;
    private final AppController controller;
    private Collection<Thread> saveThreads;

    // Part of model currently selected for viewing
    private StudentId currentStudent;
    private String currentHeading;

    // Swing components
    private JSplitPane mainSplitPane;
    private JSplitPane leftSplitPane;
    private JScrollPane previewPanelScrollPane;
    private PreviewPanel previewPanel;
    private JScrollPane editorPanelScrollPane;
    private EditorPanel editorPanel;
    private StatusBar statusBar;
    private EditingPopupMenu editingPopupMenu;
    private PhrasesSection phrasesSection;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param controller The controller.
     */
    public static FeedbackScreen create(AppController controller) {
        var screen = new FeedbackScreen(controller);

        // Create pool of threads that must be run to completion.
        screen.saveThreads = new ArrayList<>();

        // Set the icon
        LogoIcon.applyIcon(screen);

        // Get the status bar ready for updates
        screen.setLayout(new BorderLayout());
        screen.setupStatusBar();

        // Subscribe to model for changes, and store a reference to it for querying.
        screen.assignment = controller.registerWithModel(screen);

        // Setup components
        screen.setup();
        screen.setupMenuBar();
        screen.setupPreviewPanel();
        screen.setupEditorPanel();
        screen.setupPhrasesSection();

        // Add components to main (triple) split pane in middle of screen
        screen.setupSplitPanes();
        screen.add(screen.mainSplitPane, BorderLayout.CENTER);

        // Do visual stuff and display
        screen.initialResize();
        screen.setVisible(true);

        return screen;
    }

    private void initialResize() {
        // Resize according to contents
        pack();
        pack();

        // Clamp using monitor size (should work for multi-monitor setups okay)
        Dimension preferred = getContentPane().getPreferredSize();
        Rectangle monitor = getGraphicsConfiguration().getBounds();
        setSize(
            Math.min((preferred.width * 5) / 4, (monitor.width * 4) / 5),
            Math.min((preferred.height * 5) / 4, (monitor.height * 4) / 5)
        );

        // Center on screen
        setLocationRelativeTo(null);
    }

    private FeedbackScreen(AppController controller) {
        super();
        this.controller = controller;
    }

    /**
     * Setup the feedback screen.
     */
    private void setup() {
        setTitle("Feedback Helper – " + assignment.getTitle());
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    exitProgram();
                }
            }
        );
    }

    /**
     * Setup the menubar and add it to the screen.
     */
    private void setupMenuBar() {
        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create the file menu and items
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveOption = new JMenuItem("Save assignment");
        JMenuItem addStudentOption = new JMenuItem("Add new student");
        JMenuItem exportDocsOption = new JMenuItem("Export grades and feedback documents");
        JMenuItem visGradesOption = new JMenuItem("Visualise grades");
        JMenuItem exitOption = new JMenuItem("Exit");

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

        // Exit program option
        exitOption.addActionListener(e -> exitProgram());

        // Show the 'about' dialog window
        aboutOption.addActionListener(l -> AboutDialog.create(this));

        // Add all options to menus
        fileMenu.add(saveOption);
        fileMenu.add(addStudentOption);
        fileMenu.add(exportDocsOption);
        fileMenu.add(visGradesOption);
        fileMenu.addSeparator();
        fileMenu.add(exitOption);
        helpMenu.add(aboutOption);

        // Add the menus to the menu bar
        menuBar.add(fileMenu);
        menuBar.add(preferencesMenu);
        menuBar.add(helpMenu);

        // Add the menu bar to the screen
        setJMenuBar(menuBar);
    }

    /**
     * Setup the preview panel and put it in the scroll pane, replacing any old panel that was there.
     */
    private void setupPreviewPanel() {
        previewPanel = PreviewPanel.create(this::switchStudentIfNeeded);
        for (StudentId studentId : assignment.getStudentIds()) {
            previewPanel.addStudent(studentId, assignment.getGrade(studentId), assignment.getFeedbackLength(studentId));
        }

        // Create scroll pane with this panel
        previewPanelScrollPane = new JScrollPane(previewPanel);

        // Re-add in case this is a reset
        if (leftSplitPane != null) {
            leftSplitPane.setLeftComponent(previewPanelScrollPane);
        }

        // Start with the first one open if this is first time setup (they should be sorted)
        if (currentStudent == null && !assignment.getStudentIds().isEmpty()) {
            currentStudent = assignment.getStudentIds().get(0);
        }

        // Select the current student
        previewPanel.selectStudent(currentStudent);
    }

    /**
     * Setup the editor panel.
     *
     * Should be called after setupPreviewPanel, which sets currentStudent
     */
    private void setupEditorPanel() {
        // Create editor panel with popup menu
        editorPanel = EditorPanel.create(
            assignment.getTitle(),
            assignment.getHeadings(),
            assignment.getLineMarker(),
            this::switchSection,
            controller::editHeading,
            this::updateFeedbackSection,
            this::updateGrade
        );
        this.editingPopupMenu = new EditingPopupMenu();
        this.editorPanel.registerPopupMenu(this.editingPopupMenu);

        // Set the document data
        if (currentStudent != null) {
            loadEditorPanelData();
        }

        // Make the panel scrollable
        editorPanelScrollPane = EditorPanel.newVerticalScrollPane(editorPanel);
        this.editorPanelScrollPane.getViewport().setView(this.editorPanel);
        scrollEditorPaneToTop();
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
     * Setup the phrase panels and the phrases section.
     */
    private void setupPhrasesSection() {
        this.phrasesSection = PhrasesSection.create();

        // Create panels
        PhrasesPanel customPhrasesPanel = PhrasesPanel.create(PhraseType.CUSTOM, this::insertPhrase);
        PhrasesPanel frequentlyUsedPhrasesPanel = PhrasesPanel.create(PhraseType.FREQUENTLY_USED, this::insertPhrase);

        // Add panels
        this.phrasesSection.addPhrasesTab(customPhrasesPanel, text -> controller.addCustomPhrase(currentHeading, text));
        this.phrasesSection.addPhrasesTab(frequentlyUsedPhrasesPanel, null);

        // Start on frequently used pane
        this.phrasesSection.setHighlightedPane(1);
    }

    private void setupStatusBar() {
        statusBar = StatusBar.create();
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Setup the triple split pane that sits inside the JFrame and holds the main components.
     */
    private void setupSplitPanes() {
        leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanelScrollPane, editorPanelScrollPane);
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, phrasesSection);

        leftSplitPane.setOneTouchExpandable(false);
        mainSplitPane.setOneTouchExpandable(false);

        // Resize behaviour
        leftSplitPane.setResizeWeight(0.0); // Editor panel gets extra weight, preview panel is fixed
        mainSplitPane.setResizeWeight(0.8); // Left panels get most weight, phrases panel a bit
    }

    private JMenu createPreferencesMenu() {
        JMenu preferencesMenu = new JMenu("Preferences");

        // Choose a theme
        JMenu themeMenu = new JMenu("Theme");
        for (LookAndFeelInfo theme : UIManager.getInstalledLookAndFeels()) {
            JMenuItem item = new JMenuItem(theme.getName());
            item.addActionListener(e -> setTheme(theme.getClassName()));
            themeMenu.add(item);
        }
        preferencesMenu.add(themeMenu);

        // Set UI scaling
        JMenu scaleMenu = new JMenu("Scale factor (requires restart)");
        JSpinner scaleSpinner = new JSpinner(new SpinnerNumberModel(UserPreferences.getScale(), 0.25, 4.0, 0.05));
        scaleSpinner.setEditor(new JSpinner.NumberEditor(scaleSpinner, "0%"));
        scaleSpinner.addChangeListener(e -> UserPreferences.setScale((float) (double) scaleSpinner.getValue()));
        scaleMenu.add(scaleSpinner);
        preferencesMenu.add(scaleMenu);

        return preferencesMenu;
    }

    private void setTheme(String name) {
        handleInfo("Setting theme " + name);
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
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void exitProgram() {
        handleInfo("Closing...");
        saveAssignmentForCurrentStudent();
        SwingUtilities.invokeLater(this::joinSaveThreadsAndExit);
    }

    private void joinSaveThreadsAndExit() {
        try {
            for (Thread thread : saveThreads) {
                thread.join();
            }
            System.exit(0);
        } catch (InterruptedException e) {
            handleError("Interrupted while saving", e);
        }
    }

    private void saveAssignmentForCurrentStudent() {
        // If no student is selected, simply save to disk
        if (currentStudent == null) {
            controller.saveAssignment();
            return;
        }

        // Extract anything from the view that might not have been committed
        Map<String, String> sections = editorPanel.getSections();
        double grade = editorPanel.getGrade();

        // Send it to the controller for saving
        controller.updateFeedbackAndGrade(currentStudent, sections, grade); // This saves to disk.

        // Update the left panel with the latest data
        previewPanel.updateGrade(currentStudent, grade);
        previewPanel.updateLength(currentStudent, assignment.getFeedbackLength(currentStudent));
    }

    private void addNewStudent() {
        String input = JOptionPane.showInputDialog(this, "Enter the new student id");
        try {
            controller.addNewStudent(input);
        } catch (IllegalArgumentException e) {
            handleError("Invalid student ID '" + input + "'.", e);
        }
    }

    /**
     * Change the student currently selected in the view, if not the same as the current one.
     *
     * @param studentId The ID of the student we are now viewing.
     */
    private void switchStudentIfNeeded(StudentId studentId) {
        System.out.println("Trying to switch to student " + studentId + " from " + currentStudent);

        if (!studentId.equals(currentStudent)) {
            switchStudent(studentId);
        }
    }

    /**
     * Change the student currently selected in the view.
     *
     * @param studentId The ID of the student we are now viewing.
     */
    private void switchStudent(StudentId studentId) {
        // Wrap up from last student
        editorPanel.trimFeedbackBoxes();
        saveAssignmentForCurrentStudent();
        previewPanel.updateGrade(currentStudent, assignment.getGrade(currentStudent));

        // Switch to new student
        currentStudent = studentId;
        loadEditorPanelData();
        scrollEditorPaneToTop();

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

        // Clear phrases
        phrasesSection.resetPhrasesPanels();

        // Add new phrases
        assignment.getPhrasesForHeading(currentHeading).forEach(phrase -> handlePhraseAdded(heading, phrase));
        assignment.getCustomPhrases(currentHeading).forEach(phrase -> handleCustomPhraseAdded(heading, phrase));
    }

    private void updateFeedbackSection(String heading, String text) {
        controller.updateFeedbackSection(currentStudent, heading, text);
        previewPanel.updateLength(currentStudent, assignment.getFeedbackLength(currentStudent));
    }

    private void updateGrade(double grade) {
        controller.updateGrade(currentStudent, grade);
    }

    private void insertPhrase(String phrase) {
        editorPanel.insertPhraseIntoFeedbackBox(currentHeading, phrase);
    }

    private void scrollEditorPaneToTop() {
        SwingUtilities.invokeLater(() -> editorPanelScrollPane.getVerticalScrollBar().setValue(0));
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
            previewPanel.selectStudent(studentId);
            switchStudent(studentId);
        });
    }

    @Override
    public void handleGradeUpdate(StudentId studentId, double grade) {
        SwingUtilities.invokeLater(() -> previewPanel.updateGrade(studentId, grade));
    }

    @Override
    public void handlePhraseAdded(String heading, Phrase phrase) {
        SwingUtilities.invokeLater(() -> {
            if (currentHeading == heading) {
                phrasesSection.addPhraseToPanel(phrase);
            }
        });
    }

    @Override
    public void handlePhraseDeleted(String heading, Phrase phrase) {
        SwingUtilities.invokeLater(() -> {
            if (currentHeading == heading) {
                phrasesSection.removePhraseFromPanel(phrase);
            }
        });
    }

    @Override
    public void handlePhraseCounterUpdated(String heading, Phrase phrase) {
        SwingUtilities.invokeLater(() -> {
            if (currentHeading == heading) {
                phrasesSection.updatePhraseCounter(phrase);
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

    /**
     * Received a notification that the assignment is being saved in another thread.
     *
     * This method takes the thread and stores a reference to it, so it can be
     * joined at shutdown to avoid interrupting it.
     */
    @Override
    public void handleSaveThread(Thread saveThread) {
        SwingUtilities.invokeLater(() -> saveThreads.add(saveThread));
    }

    @Override
    public void handleExported(Path outputDirectory) {
        // Try to open the file manager to show the exported files
        if (Files.isDirectory(outputDirectory) && Desktop.isDesktopSupported()) {
            var desktop = Desktop.getDesktop();
            try {
                desktop.open(outputDirectory.toFile());
            } catch (IOException e) {
                handleError("Failed to open file manager", e);
            }
        }
    }

    @Override
    public void handleInfo(String message) {
        System.out.println(message);
        statusBar.showMessage(message);
    }

    @Override
    public void handleError(String description, Exception exception) {
        String message = description + ": " + exception.toString();
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, message, "Error!", JOptionPane.ERROR_MESSAGE)
        );
    }
}
