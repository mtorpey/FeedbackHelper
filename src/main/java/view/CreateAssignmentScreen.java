package view;

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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.AppController;
import model.Assignment;
import model.StudentId;

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
                put("No hash", "");
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
     * Constructor.
     *
     * @param controller The controller.
     */
    public CreateAssignmentScreen(AppController controller) {
        // Setup as a JFrame
        super("Create assignment");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(SPACING, SPACING));

        // Store attributes
        this.controller = controller;

        // Setup components
        setupFrameTitle();
        setupConfigForm();
        setupConfirmationPanel();

        // Finish setting up as a JFrame
        pack(); // resize to fit components
        setLocationRelativeTo(null); // center
        setVisible(true);
    }

    /**
     * Setup the title label at the top of the frame.
     */
    public void setupFrameTitle() {
        JLabel title = new JLabel("Assignment Configuration", JLabel.CENTER);
        title.setFont(Configuration.getTitleFont());
        add(title, BorderLayout.NORTH);
    }

    /**
     * Setup the config form.
     */
    private void setupConfigForm() {
        this.configFormPanel = new JPanel(new GridBagLayout());
        this.gridBagCounter = 0;

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

    /**
     * Setup the assignment title controls.
     */
    private void setupAssignmentTitleControls() {
        addLabelToConfigForm("Assignment title:");
        assignmentTitleField = new JTextField("CS5000-P1");
        addToConfigForm(assignmentTitleField, 2);
    }

    /**
     * Setup the assignment directory controls.
     */
    private void setupAssignmentDirectoryControls() {
        addLabelToConfigForm("Assignment directory:");

        // Text field
        String defaultDir = System.getProperty("user.home");
        assignmentDirectoryField = new JTextField(defaultDir);
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
        addToConfigForm(assignmentDirectoryChooser);
    }

    /**
     * Setup the assignment headings controls.
     */
    private void setupAssignmentHeadingsControls() {
        addLabelToConfigForm("Assignment headings:");
        assignmentHeadingsTextArea = new JTextArea(7, 30);
        JScrollPane scrollPane = new JScrollPane(assignmentHeadingsTextArea);
        scrollPane.setMinimumSize(new Dimension(0, 100)); // stop this collapsing
        addToConfigForm(scrollPane, 2);
    }

    /**
     * Setup the heading style controls.
     */
    private void setupHeadingStyleControls() {
        addLabelToConfigForm("Heading style:");
        headingStyleChooser = new JComboBox<String>(HEADING_STYLES.keySet().toArray(new String[0]));
        addToConfigForm(headingStyleChooser, 2);
    }

    /**
     * Setup the heading underline controls.
     */
    private void setupHeadingUnderlineControls() {
        addLabelToConfigForm("Heading underline:");
        underlineChooser = new JComboBox<>(UNDERLINE_STYLES.keySet().toArray(new String[0]));
        addToConfigForm(underlineChooser, 2);
    }

    /**
     * Setup the line marker controls.
     */
    private void setupLineMarkerControls() {
        addLabelToConfigForm("Line marker style:");
        lineMarkerChooser = new JComboBox<>(new String[] { "-", "->", "=>", "*", "+" });
        addToConfigForm(lineMarkerChooser, 2);
    }

    /**
     * Setup heading line spacing controls.
     */
    private void setupHeadingLineSpacingControls() {
        addLabelToConfigForm("Line spacing between sections:");
        spacingChooser = new JComboBox<>(new Integer[] { 1, 2, 3 });
        addToConfigForm(spacingChooser, 2);
    }

    /**
     * Setup the student list controls.
     */
    private void setupStudentListControls() {
        addLabelToConfigForm("Student list file:");

        // Text field
        studentListField = new JTextField();
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
        addToConfigForm(studentListFileButton);
    }

    /** Setup the indicator for the expected list of students that will be imported. */
    private void setupStudentListIndicator() {
        // Add the indicator to the panel
        studentListIndicator = new JLabel("", JLabel.CENTER);
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
    private void addLabelToConfigForm(String text) {
        addToConfigForm(new JLabel(text, SwingConstants.RIGHT));
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
            new HomeScreen(this.controller);
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
            controller.createAssignment(assignmentTitle, assignmentHeadings, studentListFile, assignmentDirectory);
            controller.setFeedbackStyle(
                HEADING_STYLES.get(headingStyle),
                UNDERLINE_STYLES.get(headingUnderlineStyle),
                lineSpacing,
                lineMarker
            );
            controller.saveAssignment();

            // Create the feedback screen
            new FeedbackScreen(controller);

            // Delete this screen
            dispose();
        } catch (IOException exception) {
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
