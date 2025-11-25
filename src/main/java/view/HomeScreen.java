package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import configuration.UserPreferences;
import controller.AppController;

/**
 * Welcome screen showing initial options.
 */
public class HomeScreen extends JFrame {

    // Instance variables
    private final AppController controller;

    // Styling
    private static final int SPACING = 15;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param controller The controller.
     */
    public static HomeScreen create(AppController controller) {
        var homeScreen = new HomeScreen(controller);

        // Set icon
        LogoIcon.applyIcon(homeScreen);

        // Warn about beta
        JOptionPane.showMessageDialog(
            homeScreen,
            "This is a beta release and has not been thoroughly tested. Back up often using the Export option and consider copying output to another directory just in case.",
            "Warning",
            JOptionPane.WARNING_MESSAGE
        );

        // Setup the components and display the screen
        homeScreen.setLayout(new BorderLayout(SPACING, SPACING));
        homeScreen.createComponents();

        // To pick a dimension that fits your UI,
        // You simply call pack() and Swing gives it a try.
        homeScreen.pack();

        // If line-wrapped components don't look very nice,
        // Then how do you solve it? You just call pack() twice!
        homeScreen.pack();

        // Do Windows or Mac machines work in this way?
        // I have no idea. Let's just hope and pray.

        // Finish setting up JFrame
        homeScreen.setMinimumSize(homeScreen.getSize());
        homeScreen.setLocationRelativeTo(null); // center
        homeScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        homeScreen.setVisible(true);

        return homeScreen;
    }

    private HomeScreen(AppController controller) {
        super("Feedback Helper Tool");
        this.controller = controller;
    }

    /**
     * Create the home screen components.
     */
    public void createComponents() {
        createFrameTitle();
        createDescriptionArea();
        createButtons();
    }

    /**
     * Create the title label.
     */
    public void createFrameTitle() {
        JLabel title = new JLabel("Feedback Helper", JLabel.CENTER);
        title.setFont(Configuration.getTitleFont());
        title.setBorder(BorderCreator.emptyBorderMedium());
        add(title, BorderLayout.NORTH);
    }

    /**
     * Create the description area.
     */
    public void createDescriptionArea() {
        JTextArea description = new JTextArea();
        description.setText(
            """
            Welcome to the Feedback Helper Tool!

            To get started with creating feedback documents click the 'Start New Assignment' button. You will then be prompted for some details to set up the project.

            To resume work on an existing assignment, click the 'Load Assignment' button and select your '.fht' file."""
        );
        description.setEditable(false);
        description.setFocusable(false); 
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setOpaque(false);
        description.setBorder(BorderFactory.createEmptyBorder(SPACING, SPACING, SPACING, SPACING));
        add(description, BorderLayout.CENTER);
    }

    /** Create both buttons and arrange them. */
    public void createButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        createStartButton(buttonPanel);
        createLoadButton(buttonPanel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Create the start button.
     */
    private void createStartButton(JPanel parent) {
        JButton startButton = new JButton("Start New Assignment");
        startButton.addActionListener(e -> {
            dispose();
            CreateAssignmentScreen.create(controller);
        });
        parent.add(startButton);
    }

    /**
     * Create the load button.
     */
    private void createLoadButton(JPanel parent) {
        JButton loadButton = new JButton("Load Assignment");
        loadButton.addActionListener(event -> {
            // Show a file chooser
            Path lastPath = UserPreferences.getLastOpenedAssignmentPath();
            if (lastPath == null) {
                lastPath = Path.of(System.getProperty("user.home"));
            }
            JFileChooser fileChooser = new JFileChooser(lastPath.toString());
            fileChooser.setDialogTitle("Choose an assignment to resume");

            // Only allow FHT files to be selected
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Assignment Files", "fht");
            fileChooser.addChoosableFileFilter(filter);

            // Get the selected file
            int returnValue = fileChooser.showDialog(this, "Open");
            Path assignmentFilePath = null;
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                assignmentFilePath = fileChooser.getSelectedFile().toPath();

                // Try to load the assignment
                try {
                    controller.loadAssignment(assignmentFilePath);
                    FeedbackScreen.create(controller);
                    dispose();
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(
                        this,
                        exception.toString(),
                        "Problem loading assignment",
                        JOptionPane.ERROR_MESSAGE
                    );
                    exception.printStackTrace();
                }
            }
        });
        parent.add(loadButton);
    }
}
