package view;

import controller.IAppController;
import model.Assignment;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

/**
 * Home Screen Class.
 */
public class HomeScreen {

    // Instance variables
    private final IAppController controller;
    private JFrame homeScreen;
    private JPanel homeScreenPanel;
    private JButton startNewButton;
    private JLabel titleLabel;
    private JTextPane descriptionLabel;
    private JButton loadButton;

    /**
     * Constructor.
     *
     * @param controller The controller.
     */
    public HomeScreen(IAppController controller) {
        this.controller = controller;

        // Create the home screen jFrame
        this.homeScreen = new JFrame("Feedback Helper Tool");
        this.homeScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.homeScreen.setSize(800, 600);

        // Setup the components and display the screen
        createHomeScreenComponents();
        displayHomeScreen();
    }

    /**
     * Create the home screen components.
     */
    public void createHomeScreenComponents() {
        createHomeScreenPanel();
        createTitleLabel();
        createDescriptionArea();
        createButtons();
    }

    /**
     * Display the home screen.
     */
    public void displayHomeScreen() {
        // Add components
        this.homeScreenPanel.add(this.titleLabel);
        this.homeScreenPanel.add(Box.createRigidArea(new Dimension(100, 20)));
        this.homeScreenPanel.add(this.descriptionLabel);
        this.homeScreenPanel.add(Box.createRigidArea(new Dimension(100, 20)));
        this.homeScreenPanel.add(this.startNewButton);
        this.homeScreenPanel.add(Box.createRigidArea(new Dimension(100, 20)));
        this.homeScreenPanel.add(this.loadButton);
        this.homeScreenPanel.add(Box.createRigidArea(new Dimension(100, 20)));

        // Centre the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - this.homeScreen.getWidth()) / 2;
        int y = (screenSize.height - this.homeScreen.getHeight()) / 2;
        this.homeScreen.setLocation(x, y);

        // Add home screen panel to home screen frame and set visibility
        this.homeScreen.add(this.homeScreenPanel);
        this.homeScreen.setVisible(true);
    }

    /**
     * Create the home screen panel.
     */
    public void createHomeScreenPanel() {
        this.homeScreenPanel = new JPanel();

        // Layout from top to bottom
        this.homeScreenPanel.setLayout(new BoxLayout(this.homeScreenPanel, BoxLayout.PAGE_AXIS));
        this.homeScreenPanel.setBorder(BorderCreator.createEmptyBorderLeavingBottom(BorderCreator.PADDING_50_PIXELS));
    }

    /**
     * Create the title label.
     */
    public void createTitleLabel() {
        this.titleLabel = new JLabel();
        this.titleLabel.setText("Feedback Helper Tool");
        this.titleLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 28));
        this.titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.titleLabel.setBorder(BorderCreator.createEmptyBorderBottomOnly(BorderCreator.PADDING_20_PIXELS));
    }

    /**
     * Create the description area.
     */
    public void createDescriptionArea() {
        this.descriptionLabel = new JTextPane();
        this.descriptionLabel.setText(
                "Welcome to the Feedback Helper Tool! " +
                        "To get started with creating feedback documents click the 'Start New Assignment' button. " +
                        "You will then be prompted to setup your assignment via a JSON configuration file or through a manual guided setup. " +
                        "To resume creating feedback documents click the 'Load Assignment' button and select your '.fht' file."
        );
        this.descriptionLabel.setBorder(BorderCreator.createAllSidesEmptyBorder(20));
        this.descriptionLabel.setMaximumSize(new Dimension(500, 210));
        this.descriptionLabel.setPreferredSize(new Dimension(500, 210));
        this.descriptionLabel.setMinimumSize(new Dimension(500, 210));
        this.descriptionLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 18));
        this.descriptionLabel.setEditable(false);
        this.descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    /**
     * Create the buttons.
     */
    public void createButtons() {
        createStartButton();
        createLoadButton();
    }

    /**
     * Create the load button.
     */
    private void createLoadButton() {
        this.loadButton = new JButton("Resume Assignment");
        this.loadButton.setMaximumSize(new Dimension(300, 50));
        this.loadButton.setPreferredSize(new Dimension(300, 50));
        this.loadButton.setMinimumSize(new Dimension(300, 50));
        this.loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.loadButton.addActionListener(e -> {
            // Show a file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose an assignment to resume");

            // Only allow FHT files to be selected
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Assignment Files", "fht");
            fileChooser.addChoosableFileFilter(filter);

            // Get the selected file
            int returnValue = fileChooser.showDialog(this.homeScreen, "Resume this assignment");
            String assignmentFilePath = null;
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                assignmentFilePath = fileChooser.getSelectedFile().getPath();
                this.homeScreen.dispose();
            }

            // Ensure selected file is valid and show feedback screen
            if (assignmentFilePath != null) {
                Assignment assignment = this.controller.loadAssignment(assignmentFilePath);
                new FeedbackScreen(this.controller, assignment);
            }
        });
    }

    /**
     * Create the start button.
     */
    private void createStartButton() {
        this.startNewButton = new JButton("Start New Assignment");
        this.startNewButton.setMaximumSize(new Dimension(300, 50));
        this.startNewButton.setPreferredSize(new Dimension(300, 50));
        this.startNewButton.setMinimumSize(new Dimension(300, 50));
        this.startNewButton.addActionListener(e -> {
            this.homeScreen.dispose();
            new SetupOptionsScreen(this.controller);
        });
        this.startNewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

}
