package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.IAppController;
import model.Assignment;

/**
 * Welcome screen showing initial options.
 */
public class HomeScreen extends JFrame {

    // Instance variables
    private final IAppController controller;

    // Styling
    private static final int SPACING = 15;

    /**
     * Constructor.
     *
     * @param controller The controller.
     */
    public HomeScreen(IAppController controller) {
        // Setup as a JFrame
        super("Feedback Helper Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(SPACING, SPACING));

        // Store attributes
        this.controller = controller;

        // Setup the components and display the screen
        createComponents();

        // To pick a dimension that fits your UI,
        // You simply call pack() and Swing gives it a try.
        pack();

        // If line-wrapped components don't look very nice,
        // Then how do you solve it? You just call pack() twice!
        pack();

        // Do Windows or Mac machines work in this way?
        // I have no idea. Let's just hope and pray.

        // Finish setting up JFrame
        setLocationRelativeTo(null); // center
        setVisible(true);
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
        title.setBorder(BorderFactory.createEmptyBorder(SPACING, SPACING, SPACING, SPACING));
        add(title, BorderLayout.NORTH);
    }

    /**
     * Create the description area.
     */
    public void createDescriptionArea() {
        JTextArea description = new JTextArea();
        description.setText("""
            Welcome to the Feedback Helper Tool!

            To get started with creating feedback documents click the 'Start New Assignment' button. You will then be prompted for some details to set up the project.

            To resume work on an existing assignment, click the 'Load Assignment' button and select your '.fht' file."""
        );
        description.setEditable(false);
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
        //startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> {
            dispose();
            new CreateAssignmentScreen(this.controller);
        });
        parent.add(startButton);
    }

    /**
     * Create the load button.
     */
    private void createLoadButton(JPanel parent) {
        JButton loadButton = new JButton("Load Assignment");
        //loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadButton.addActionListener(e -> {
            // Show a file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose an assignment to resume");

            // Only allow FHT files to be selected
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Assignment Files", "fht");
            fileChooser.addChoosableFileFilter(filter);

            // Get the selected file
            int returnValue = fileChooser.showDialog(this, "Resume this assignment");
            String assignmentFilePath = null;
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                assignmentFilePath = fileChooser.getSelectedFile().getPath();
                dispose();
            }

            // Ensure selected file is valid and show feedback screen
            if (assignmentFilePath != null) {
                Assignment assignment = this.controller.loadAssignment(assignmentFilePath);
                new FeedbackScreen(this.controller, assignment);
            }
        });
        parent.add(loadButton);
    }
}
