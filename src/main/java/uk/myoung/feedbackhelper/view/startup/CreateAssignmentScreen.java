package uk.myoung.feedbackhelper.view.startup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.myoung.feedbackhelper.controller.AppController;
import uk.myoung.feedbackhelper.model.Assignment;
import uk.myoung.feedbackhelper.model.StudentId;
import uk.myoung.feedbackhelper.view.feedbackscreen.FeedbackScreen;
import uk.myoung.feedbackhelper.view.style.BorderCreator;
import uk.myoung.feedbackhelper.view.style.Fonts;
import uk.myoung.feedbackhelper.view.style.LogoIcon;

/**
 * Configuration window for creating a new assignment
 */
public class CreateAssignmentScreen extends JFrame {

    // Underline styles map
    private static final Map<String, String> UNDERLINE_STYLES = Collections.unmodifiableMap(
        new LinkedHashMap<String, String>() {
            {
                put("No underline", "");
                put("Single underline (---)", "-");
                put("Double underline (===)", "=");
            }
        }
    );

    // Heading styles map
    private static final Map<String, String> HEADING_STYLES = Collections.unmodifiableMap(
        new LinkedHashMap<String, String>() {
            {
                put("No decoration", "");
                put("Single hash (#)", "#");
                put("Double hash (##)", "##");
            }
        }
    );

    // Style constants
    private static final int SPACING = 5;
    private static final int GRID_WIDTH = 3;

    // Instance variables
    private final AppController controller;
    private JPanel configFormPanel;
    private JLabel studentListIndicator;
    private DocumentListener studentListListener;

    // How many cells on the config panel are filled
    private int gridBagCounter;

    // Components with user inputs
    private JTextField assignmentTitleField, assignmentDirectoryField, studentListField;
    private JTextArea assignmentHeadingsTextArea;
    private JComboBox<String> headingStyleChooser, underlineChooser, lineMarkerChooser;
    private JComboBox<Integer> spacingChooser;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param controller The controller.
     */
    public static CreateAssignmentScreen create(AppController controller) {
        CreateAssignmentScreen screen = new CreateAssignmentScreen(controller);

        // Set icon
        LogoIcon.applyIcon(screen);

        // Setup components
        screen.setLayout(new BorderLayout(SPACING, SPACING));
        screen.setupFrameTitle();
        screen.setupConfigForm();
        screen.setupConfirmationPanel();

        // Finish setting up as a JFrame
        screen.pack(); // resize to fit components
        screen.setMinimumSize(screen.getSize());
        screen.setLocationRelativeTo(null); // center
        screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        screen.setVisible(true);

        return screen;
    }

    private CreateAssignmentScreen(AppController controller) {
        super("Create assignment");
        this.controller = controller;
    }

    /**
     * Setup the title label at the top of the frame.
     */
    public void setupFrameTitle() {
        JLabel title = new JLabel("Assignment Configuration", JLabel.CENTER);
        title.setFont(Fonts.getTitleFont());
        title.setBorder(BorderCreator.emptyBorderMedium());
        add(title, BorderLayout.NORTH);
    }

    /**
     * Setup the config form.
     */
    private void setupConfigForm() {
        this.configFormPanel = new JPanel(new GridBagLayout());
        this.gridBagCounter = 0;

        setupInfoLabel();
        setupAssignmentTitleControls();
        setupAssignmentDirectoryControls();
        setupAssignmentHeadingsControls();
        setupHeadingStyleControls();
        setupHeadingUnderlineControls();
        setupLineMarkerControls();
        setupHeadingLineSpacingControls();
        setupStudentListControls();
        setupStudentListIndicator();

        add(this.configFormPanel, BorderLayout.CENTER);
    }

    private void setupInfoLabel() {
        addToConfigForm(new JLabel("Hover over an item for more information.", JLabel.CENTER), GRID_WIDTH);
    }

    /**
     * Setup the assignment title controls.
     */
    private void setupAssignmentTitleControls() {
        String tooltip =
            "A name that identifies this assignment, used for the name of the save file and export directory.";
        addLabelToConfigForm("Assignment title:", tooltip);
        assignmentTitleField = new JTextField("CS5000-P1");
        assignmentTitleField.setToolTipText(tooltip);
        addToConfigForm(assignmentTitleField, 2);
    }

    /**
     * Setup the assignment directory controls.
     */
    private void setupAssignmentDirectoryControls() {
        String tooltip =
            "The directory where the save file and exported feedback and grades will be placed.\nIf this directory contains the student submissions, the tool will attempt to use this for the list of students below.";
        addLabelToConfigForm("Assignment directory:", tooltip);

        // Text field
        String defaultDir = System.getProperty("user.home");
        assignmentDirectoryField = new JTextField(defaultDir);
        assignmentDirectoryField.setToolTipText(tooltip);
        addToConfigForm(assignmentDirectoryField, true);

        // Directory chooser
        JButton assignmentDirectoryChooser = new JButton("Select directory");
        assignmentDirectoryChooser.addActionListener(e ->
            assignmentDirectoryField.setText(
                selectPathWithDialog(
                    assignmentDirectoryField.getText(),
                    JFileChooser.DIRECTORIES_ONLY,
                    "Select assignment directory...",
                    "Submit"
                ).toString()
            )
        );
        assignmentDirectoryChooser.setToolTipText(tooltip);
        addToConfigForm(assignmentDirectoryChooser);
    }

    /**
     * Setup the assignment headings controls.
     */
    private void setupAssignmentHeadingsControls() {
        String tooltip = "Headings for the sections in the feedback documents.";
        addLabelToConfigForm("Assignment headings:", tooltip);
        assignmentHeadingsTextArea = new JTextArea("Code\nReport\nOverall") {
            @Override
            public Dimension getMinimumSize() {
                Dimension minimum = super.getMinimumSize();
                Dimension preferred = getPreferredSize();
                return new Dimension(minimum.width, preferred.height);
            }
        };
        assignmentHeadingsTextArea.setToolTipText(tooltip);
        assignmentHeadingsTextArea.setRows(7);
        assignmentHeadingsTextArea.setBorder(BorderCreator.textAreaBorder());
        addToConfigForm(assignmentHeadingsTextArea, 2);
    }

    /**
     * Setup the heading style controls.
     */
    private void setupHeadingStyleControls() {
        String tooltip = "Characters that will appear before a heading in exported feedback documents.";
        addLabelToConfigForm("Heading style:", tooltip);
        headingStyleChooser = new JComboBox<String>(HEADING_STYLES.keySet().toArray(new String[0]));
        headingStyleChooser.setToolTipText(tooltip);
        addToConfigForm(headingStyleChooser, 2);
    }

    /**
     * Setup the heading underline controls.
     */
    private void setupHeadingUnderlineControls() {
        String tooltip = "Character that will be used to underline a heading in exported feedback documents.";
        addLabelToConfigForm("Heading underline:", tooltip);
        underlineChooser = new JComboBox<>(UNDERLINE_STYLES.keySet().toArray(new String[0]));
        underlineChooser.setToolTipText(tooltip);
        addToConfigForm(underlineChooser, 2);
    }

    /**
     * Setup the line marker controls.
     */
    private void setupLineMarkerControls() {
        String tooltip = "Marker that will be placed at the beginning of each bullet point in feedback.";
        addLabelToConfigForm("Line marker style:", tooltip);
        lineMarkerChooser = new JComboBox<>(new String[] { "-", "•", "*", "+", "->", "=>" });
        lineMarkerChooser.setToolTipText(tooltip);
        addToConfigForm(lineMarkerChooser, 2);
    }

    /**
     * Setup heading line spacing controls.
     */
    private void setupHeadingLineSpacingControls() {
        String tooltip = "Number of blank lines inserted between sections in exported feedback documents.";
        addLabelToConfigForm("Line spacing between sections:", tooltip);
        spacingChooser = new JComboBox<>(new Integer[] { 1, 2, 3 });
        spacingChooser.setToolTipText(tooltip);
        addToConfigForm(spacingChooser, 2);
    }

    /**
     * Setup the student list controls.
     */
    private void setupStudentListControls() {
        String tooltip = """
            Text file containing all the student IDs for this assignment, separated by commas or whitespace.
            MMS provides a suitable file for this: see "Download a template file to fill here" on MMS.
            If no file is selected, the tool will attempt to guess the IDs from the assignment directory (above).""";
        addLabelToConfigForm("Student list file:", tooltip);

        // Text field
        studentListField = new JTextField();
        studentListField.setToolTipText(tooltip);
        addToConfigForm(studentListField, true);

        // Button
        JButton studentListFileButton = new JButton("Select file");
        studentListFileButton.addActionListener(e ->
            studentListField.setText(
                selectPathWithDialog(
                    studentListField.getText(),
                    JFileChooser.FILES_ONLY,
                    "Choose a student manifest file...",
                    "Select"
                ).toString()
            )
        );
        studentListFileButton.setToolTipText(tooltip);
        addToConfigForm(studentListFileButton);
    }

    /** Setup the indicator for the expected list of students that will be imported. */
    private void setupStudentListIndicator() {
        String tooltip =
            "This shows the student IDs that will be used, based on 'Assignment directory' and 'Student list file' above.";

        // Add the indicator to the panel
        studentListIndicator = new JLabel("", JLabel.CENTER);
        studentListIndicator.setToolTipText(tooltip);
        addToConfigForm(studentListIndicator, true, 3);

        // Set up a listener that will update the indicator when the user's selection changes
        studentListListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateStudentListIndicator();
            }

            public void insertUpdate(DocumentEvent e) {
                updateStudentListIndicator();
            }

            public void removeUpdate(DocumentEvent e) {
                updateStudentListIndicator();
            }
        };
        assignmentDirectoryField.getDocument().addDocumentListener(studentListListener);
        studentListField.getDocument().addDocumentListener(studentListListener);

        updateStudentListIndicator();
    }

    /** Show an indication of the list of students that would currently be used with the present settings. */
    private void updateStudentListIndicator() {
        List<StudentId> studentIds = Assignment.findStudentIds(
            Path.of(studentListField.getText()),
            Path.of(assignmentDirectoryField.getText())
        );
        String message;
        int nrStudents = studentIds.size();
        if (nrStudents == 0) {
            message = "No student IDs found";
        } else {
            message = "Found " + nrStudents + " student IDs: ";
            message += studentIds.stream().limit(3).map(StudentId::toString).collect(Collectors.joining(", "));
            if (nrStudents > 3) {
                message += ", and " + (nrStudents - 3) + " others";
            }
        }
        studentListIndicator.setText(message);
    }

    /** Add a label component to the config form, aligned right. */
    private void addLabelToConfigForm(String text, String tooltip) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setToolTipText(tooltip);
        addToConfigForm(label);
    }

    /** Add a component to the config form (the main part of the window). */
    private void addToConfigForm(JComponent component) {
        addToConfigForm(component, false);
    }

    /** Add a component to the config form (the main part of the window). */
    private void addToConfigForm(JComponent component, boolean fill) {
        addToConfigForm(component, fill, 1);
    }

    /** Add a component to the config form (the main part of the window). */
    private void addToConfigForm(JComponent component, int width) {
        addToConfigForm(component, false, width);
    }

    /**
     * Add a component to the config form (the main part of the window).
     *
     * This allows the GridBagLayout to do its thing, and handles rows and columns.
     * @param component The component to add
     * @param fill Whether this component should be maximised horizontally
     * @param width How many cells wide this should be
     */
    private void addToConfigForm(JComponent component, boolean fill, int width) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; // Expand to fill cell
        gbc.insets = new Insets(SPACING, SPACING, SPACING, SPACING); // Padding around components
        gbc.gridx = gridBagCounter % GRID_WIDTH; // Grid is 3 cells wide
        gbc.gridy = gridBagCounter / GRID_WIDTH;
        gbc.gridwidth = width; // How many columns it spans
        gbc.weightx = fill ? 1 : 0; // Make it stretch evenly
        gbc.weighty = 1;

        gridBagCounter += width;
        this.configFormPanel.add(component, gbc);
    }

    /** Prompt the user to pick a file, and return the result. */
    private Path selectPathWithDialog(String startPath, int fileSelectionMode, String title, String submit) {
        // Open file chooser
        JFileChooser fileChooser = new JFileChooser(startPath);
        fileChooser.setFileSelectionMode(fileSelectionMode);
        fileChooser.setDialogTitle(title);

        // Store the chosen file path
        int returnValue = fileChooser.showDialog(this, submit);
        Path path = null;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().toPath();
        }
        return path;
    }

    /**
     * Setup the confirmation and back buttons.
     */
    private void setupConfirmationPanel() {
        // Create panel and buttons
        JPanel confirmationPanel = new JPanel();
        JButton backButton = new JButton("Back");
        JButton confirmButton = new JButton("Confirm");
        confirmationPanel.add(backButton);
        confirmationPanel.add(confirmButton);

        // On confirm, save user preferences
        confirmButton.addActionListener(this::confirmClicked);

        // On back button press go back to the setup options screen
        backButton.addActionListener(e -> {
            HomeScreen.create(controller);
            dispose();
        });
        add(confirmationPanel, BorderLayout.SOUTH);
    }

    /** Create new assignment using the supplied user options. */
    private void confirmClicked(ActionEvent event) {
        // Load all the user preferences
        String assignmentTitle = assignmentTitleField.getText();
        String assignmentHeadings = assignmentHeadingsTextArea.getText();
        Path assignmentDirectory = Path.of(assignmentDirectoryField.getText());
        String headingStyle = headingStyleChooser.getItemAt(headingStyleChooser.getSelectedIndex());
        String headingUnderlineStyle = underlineChooser.getItemAt(underlineChooser.getSelectedIndex());
        int lineSpacing = spacingChooser.getItemAt(spacingChooser.getSelectedIndex());
        String lineMarker = lineMarkerChooser.getItemAt(lineMarkerChooser.getSelectedIndex());
        Path studentListFile = Path.of(studentListField.getText());

        // Create the assignment
        try {
            controller.createAssignment(
                assignmentTitle,
                assignmentHeadings,
                studentListFile,
                assignmentDirectory,
                HEADING_STYLES.get(headingStyle),
                UNDERLINE_STYLES.get(headingUnderlineStyle),
                lineSpacing,
                lineMarker
            );

            // Create the feedback screen
            FeedbackScreen.create(controller);

            // Delete this screen
            dispose();
        } catch (IOException exception) {
            // Something went wrong: explain what
            JOptionPane.showMessageDialog(
                this,
                "Error creating assignment: " + exception.toString(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            exception.printStackTrace();
        }
    }
}
