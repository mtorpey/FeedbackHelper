package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import controller.IAppController;
import model.Assignment;

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
    private final IAppController controller;
    private JPanel configFormPanel;
    private Map<String, Component> editableComponents;

    // How many cells on the config panel are filled
    private int gridBagCounter;

    /**
     * Constructor.
     *
     * @param controller The controller.
     */
    public CreateAssignmentScreen(IAppController controller) {
        this(controller, null);
    }

    /**
     * Constructor.
     *
     * @param controller The controller.
     * @param configPath Config path either null or to pre-fill the screen
     */
    public CreateAssignmentScreen(IAppController controller, String configPath) {
        // Setup as a JFrame
        super("Create assignment");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(SPACING, SPACING));

        // Store attributes
        this.controller = controller;
        this.editableComponents = new HashMap<String, Component>();

        // Setup components
        setupTitleLabel();
        setupConfigForm();
        writeConfigOptions(configPath);
        setupConfirmationPanel();

        // Finish setting up as a JFrame
        pack(); // resize to fit components
        setLocationRelativeTo(null); // center
        setVisible(true);
    }

    /**
     * Setup the title label.
     */
    public void setupTitleLabel() {
        JLabel titleLabel = new JLabel("Assignment Configuration", JLabel.CENTER);
        titleLabel.setFont(Configuration.getTitleFont());
        add(titleLabel, BorderLayout.NORTH);
    }

    /**
     * Setup the config form.
     */
    private void setupConfigForm() {
        this.configFormPanel = new JPanel();
        this.configFormPanel.setLayout(new GridBagLayout());
        this.gridBagCounter = 0;
        
        // Setup config components
        setupAssignmentTitlePanel();
        setupAssignmentDirectoryPanel();
        setupAssignmentHeadingsPanel();
        setupHeadingStylePanel();
        setupHeadingUnderlinePanel();
        setupLineMarkerPanel();
        setupHeadingLineSpacingPanel();
        setupStudentManifestPanel();

        // Add config panel to the screen panel
        add(this.configFormPanel, BorderLayout.CENTER);
    }

    private void addToConfigForm(JComponent component) {
        addToConfigForm(component, false);
    }

    private void addToConfigForm(JComponent component, boolean fill) {
        addToConfigForm(component, fill, 1);
    }

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
        gbc.fill = GridBagConstraints.HORIZONTAL; // Expand to fill cell
        gbc.insets = new Insets(SPACING, SPACING, SPACING, SPACING); // Padding around components
        gbc.gridx = gridBagCounter % GRID_WIDTH; // Grid is 3 cells wide
        gbc.gridy = gridBagCounter / GRID_WIDTH;
        gbc.gridwidth = width; // How many columns it spans
        gbc.weightx = fill ? 1 : 0;   // Make it stretch evenly
        gbc.weighty = 1;

        gridBagCounter += width;
        this.configFormPanel.add(component, gbc);
    }

    /**
     * Setup the confirmation panel.
     *
     * @param configPath for prefilling values (can be null)
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
            new SetupOptionsScreen(this.controller);
            dispose();
        });
        add(confirmationPanel, BorderLayout.SOUTH);
    }

    private void confirmClicked(ActionEvent e) {
        // Load all the user preferences
        String assignmentTitle = ((JTextField) this.editableComponents.get("assignmentTitle")).getText();
        String assignmentHeadings = ((JTextArea) this.editableComponents.get("assignmentHeadings")).getText();
        String assignmentDirectoryPath = ((JTextField) this.editableComponents.get(
                "assignmentDirectory")).getText();
        String headingStyle = (String) ((JComboBox<String>) this.editableComponents.get(
                "headingStyle")).getSelectedItem();
        String headingUnderlineStyle = (String) ((JComboBox<String>) this.editableComponents.get(
                "headingUnderlineStyle")).getSelectedItem();
        int lineSpacing = (Integer) ((JComboBox<Integer>) this.editableComponents.get(
                "headingLineSpacing")).getSelectedItem();
        String lineMarker = (String) ((JComboBox<String>) this.editableComponents.get(
                "lineMarker")).getSelectedItem();
        File studentManifestFile = new File(((JTextField) this.editableComponents.get("studentManifest")).getText());

        // If no student list exists before creating the feedback screen, inform the
        // user that the
        // software will try to guess
        if (studentManifestFile == null || !studentManifestFile.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No student manifest file defined! Will try to guess student ids by directory names in " +
                            "the assignment directory!",
                    "Warning!",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        // Setup assignment and db for it
        dispose();

        // Create the assignment
        Assignment assignmentFromInputs = this.controller.createAssignment(
                assignmentTitle,
                assignmentHeadings,
                studentManifestFile,
                assignmentDirectoryPath
        );
        this.controller.setAssignmentPreferences(
                HEADING_STYLES.get(headingStyle),
                UNDERLINE_STYLES.get(headingUnderlineStyle),
                lineSpacing,
                lineMarker
        );
        this.controller.saveAssignment(
                assignmentFromInputs,
                assignmentFromInputs.getAssignmentTitle().toLowerCase().replace(" ", "-")
        );

        // Create the feedback screen
        new FeedbackScreen(this.controller, assignmentFromInputs);
    }

    private void writeConfigOptions(String configPath) {
        //prefilled values
        if (configPath != null) {
            try {
                JSONObject configDoc = (JSONObject) new JSONParser().parse(new FileReader(configPath));

                // Extract styling options
                ((JTextField) this.editableComponents.get("assignmentTitle")).setText((String) configDoc.get("title"));
                StringJoiner sj = new StringJoiner("\n");
                ((JSONArray) configDoc.get("headings")).forEach(h -> sj.add((String) h));
                ((JTextArea) this.editableComponents.get("assignmentHeadings")).setText(sj.toString());
                ((JTextField) this.editableComponents.get("assignmentDirectory")).setText(
                    (String) configDoc.get("assignment_location")
                );

                Map headingStyle = ((Map) configDoc.get("heading_style"));
                ((JComboBox<String>) this.editableComponents.get("headingStyle")).setSelectedItem(
                    getKey(HEADING_STYLES, (String) headingStyle.get("heading_marker"))
                );
                ((JComboBox<String>) this.editableComponents.get("headingUnderlineStyle")).setSelectedItem(
                    getKey(UNDERLINE_STYLES, (String) headingStyle.get("heading_underline_style"))
                );
                ((JComboBox<Integer>) this.editableComponents.get("headingLineSpacing")).setSelectedItem(
                    ((Long) headingStyle.get("num_lines_after_section_ends")).intValue()
                );
                ((JComboBox<String>) this.editableComponents.get("lineMarker")).setSelectedItem(
                    configDoc.get("line_marker")
                );
            } catch (IOException | ParseException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Could not find or parse config file, have to manually configure."
                    + "Could not parse config file",
                    "Warning!",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    /**
     * Gets the key for a value in a map
     *
     * @param map Map to search in
     * @param value Value to find key for
     */
    private <K, V> K getKey(Map<K, V> map, V value) {
        return map
            .entrySet()
            .stream()
            .filter(entry -> value.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    /**
     * Setup the assignment title panel.
     */
    private void setupAssignmentTitlePanel() {
        addToConfigForm(new JLabel("Assignment title: ", SwingConstants.RIGHT));
        JTextField assignmentTitleTextField = new JTextField("CS5000-P1");
        this.editableComponents.put("assignmentTitle", assignmentTitleTextField);
        addToConfigForm(assignmentTitleTextField, 2);
    }

    /**
     * Setup the assignment directory panel.
     */
    private void setupAssignmentDirectoryPanel() {
        addToConfigForm(new JLabel("Assignment directory: ", SwingConstants.RIGHT));

        // Text field
        String defaultDir = System.getProperty("user.home");
        JTextField assignmentDirectoryTextField = new JTextField(defaultDir);
        addToConfigForm(assignmentDirectoryTextField, true);

        // Directory chooser
        JButton assignmentDirectoryChooser = new JButton("Select directory");
        assignmentDirectoryChooser.addActionListener(
            e -> assignmentDirectoryTextField.setText(
                selectPathWithDialog(
                    assignmentDirectoryTextField.getText(),
                    JFileChooser.DIRECTORIES_ONLY,
                    "Select assignment directory...",
                    "Submit"
                )
            )
        );
        this.editableComponents.put("assignmentDirectory", assignmentDirectoryTextField);
        addToConfigForm(assignmentDirectoryChooser);
    }

    /**
     * Setup the assignment headings panel.
     */
    private void setupAssignmentHeadingsPanel() {
        addToConfigForm(new JLabel("Assignment headings: ", SwingConstants.RIGHT));
        JTextArea assignmentHeadingsTextArea = new JTextArea(7, 30);
        this.editableComponents.put("assignmentHeadings", assignmentHeadingsTextArea);
        addToConfigForm(new JScrollPane(assignmentHeadingsTextArea), 2);
    }

    /**
     * Setup the student list panel.
     */
    private void setupStudentManifestPanel() {
        addToConfigForm(new JLabel("Student manifest file: ", SwingConstants.RIGHT));

        // Text field
        JTextField studentManifestTextField = new JTextField();
        this.editableComponents.put("studentManifest", studentManifestTextField);
        addToConfigForm(studentManifestTextField, true);

        // Button
        JButton studentManifestFileButton = new JButton("Select file");
        studentManifestFileButton.addActionListener(
            e -> studentManifestTextField.setText(
                selectPathWithDialog(
                    studentManifestTextField.getText(),
                    JFileChooser.FILES_ONLY,
                    "Choose a student manifest file...",
                    "Select"
                )
            )
        );
        addToConfigForm(studentManifestFileButton);
    }

    /**
     * Setup the heading style panel.
     */
    private void setupHeadingStylePanel() {
        addToConfigForm(new JLabel("Heading style: ", SwingConstants.RIGHT));
        JComboBox<String> selections = new JComboBox<String>(
            HEADING_STYLES.keySet().toArray(new String[0])
        );
        this.editableComponents.put("headingStyle", selections);
        addToConfigForm(selections, 2);
    }

    /**
     * Setup the heading underline panel.
     */
    private void setupHeadingUnderlinePanel() {
        addToConfigForm(new JLabel("Heading underline: ", SwingConstants.RIGHT));
        JComboBox<String> selections = new JComboBox<String>(
            UNDERLINE_STYLES.keySet().toArray(new String[0])
        );
        this.editableComponents.put("headingUnderlineStyle", selections);
        addToConfigForm(selections, 2);
    }

    /**
     * Setup heading line spacing panel.
     */
    private void setupHeadingLineSpacingPanel() {
        addToConfigForm(new JLabel("Line spacing between sections: ", SwingConstants.RIGHT));
        JComboBox<Integer> selections = new JComboBox<>(new Integer[] {1, 2, 3});
        this.editableComponents.put("headingLineSpacing", selections);
        addToConfigForm(selections, 2);
    }

    /**
     * Setup the line marker panel.
     */
    private void setupLineMarkerPanel() {
        addToConfigForm(new JLabel("Line marker style:", SwingConstants.RIGHT));
        JComboBox<String> selections = new JComboBox<>(new String[] { "-", "->", "=>", "*", "+" });
        this.editableComponents.put("lineMarker", selections);
        addToConfigForm(selections, 2);
    }

    private String selectPathWithDialog(String startPath, int fileSelectionMode, String title, String submit) {
        // Open file chooser
        JFileChooser fileChooser = new JFileChooser(startPath);
        fileChooser.setFileSelectionMode(fileSelectionMode);
        fileChooser.setDialogTitle(title);

        // Store the chosen file path
        int returnValue = fileChooser.showDialog(this, submit);
        String path = null;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().getPath();
        }
        return path;
    }
}
