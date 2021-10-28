package view;

import controller.IAppController;
import model.Assignment;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;

/**
 * Setup Options Screen Class.
 */
public class SetupOptionsScreen {

    // Instance variables
    private final IAppController controller;
    private JFrame setupOptionsScreen;
    private JPanel setupOptionsScreenPanel;
    private JButton useConfigFileButton;
    private JButton useManualSetupButton;
    private JButton backButton;
    private JLabel titleLabel;
    private JTextPane instructions;

    /**
     * Constructor.
     *
     * @param controller The controller.
     */
    public SetupOptionsScreen(IAppController controller) {
        this.controller = controller;

        // Setup components
        setupSetupOptionsScreen();
        setupTitleLabel();
        setupConfigFileButton();
        setupManualButton();
        setupBackButton();
        setupInstructions();
        setupSetupOptionScreenPanel();

        // Add the options screen panel to the main screen and set visibility
        this.setupOptionsScreen.add(this.setupOptionsScreenPanel);
        this.setupOptionsScreen.setVisible(true);
    }

    /**
     * Setup the back button.
     */
    private void setupBackButton() {
        this.backButton = new JButton("Back");
        this.backButton.setMinimumSize(new Dimension(200, 50));
        this.backButton.setPreferredSize(new Dimension(200, 50));
        this.backButton.setMaximumSize(new Dimension(200, 50));
        this.backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.backButton.addActionListener(l -> {
            this.setupOptionsScreen.dispose();
            new HomeScreen(this.controller);
        });
    }

    /**
     * Setup the manual setup button.
     */
    private void setupManualButton() {
        this.useManualSetupButton = new JButton("Use manual setup");
        this.useManualSetupButton.setMinimumSize(new Dimension(200, 50));
        this.useManualSetupButton.setPreferredSize(new Dimension(200, 50));
        this.useManualSetupButton.setMaximumSize(new Dimension(200, 50));
        this.useManualSetupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.useManualSetupButton.addActionListener(l -> {
            this.setupOptionsScreen.dispose();
            new CreateAssignmentScreen(this.controller);
        });
    }

    /**
     * Setup config file button.
     */
    private void setupConfigFileButton() {
        this.useConfigFileButton = new JButton("Use config file...");
        this.useConfigFileButton.setMinimumSize(new Dimension(200, 50));
        this.useConfigFileButton.setPreferredSize(new Dimension(200, 50));
        this.useConfigFileButton.setMaximumSize(new Dimension(200, 50));
        this.useConfigFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.useConfigFileButton.addActionListener(l -> {
            // Show file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose a config file...");

            // Only accept JSON files
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
            fileChooser.addChoosableFileFilter(filter);

            // Get the selected file and continue if it is valid
            int returnValue = fileChooser.showDialog(this.useConfigFileButton, "Select this config file");
            String configFilePath = null;
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                configFilePath = fileChooser.getSelectedFile().getPath();
                File configFile = new File(configFilePath);

                // Check file exists
                if (configFile.exists()) {
                    this.setupOptionsScreen.dispose();
                    new CreateAssignmentScreen(this.controller, configFilePath);
                } else {
                    JOptionPane.showMessageDialog(this.setupOptionsScreen, "Please select a JSON config file!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Setup the setup options screen panel.
     */
    private void setupSetupOptionScreenPanel() {
        // Create the panel
        this.setupOptionsScreenPanel = new JPanel();
        this.setupOptionsScreenPanel.setLayout(new BoxLayout(this.setupOptionsScreenPanel, BoxLayout.PAGE_AXIS));
        this.setupOptionsScreenPanel.setBorder(BorderCreator.createEmptyBorderLeavingBottom(50));

        // Add all the components
        this.setupOptionsScreenPanel.add(this.titleLabel);
        this.setupOptionsScreenPanel.add(Box.createRigidArea(new Dimension(100, 20)));
        this.setupOptionsScreenPanel.add(this.instructions);
        this.setupOptionsScreenPanel.add(Box.createRigidArea(new Dimension(100, 20)));
        this.setupOptionsScreenPanel.add(this.useConfigFileButton);
        this.setupOptionsScreenPanel.add(Box.createRigidArea(new Dimension(100, 20)));
        this.setupOptionsScreenPanel.add(this.useManualSetupButton);
        this.setupOptionsScreenPanel.add(Box.createRigidArea(new Dimension(100, 20)));
        this.setupOptionsScreenPanel.add(this.backButton);
    }

    /**
     * Setup the title label
     */
    private void setupTitleLabel() {
        this.titleLabel = new JLabel("Setup Options");
        this.titleLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 28));
        this.titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.titleLabel.setBorder(BorderCreator.createEmptyBorderBottomOnly(BorderCreator.PADDING_20_PIXELS));
    }

    /**
     * Setup the setup options screen.
     */
    private void setupSetupOptionsScreen() {
        this.setupOptionsScreen = new JFrame("Please select a setup option");
        this.setupOptionsScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setupOptionsScreen.setSize(800, 600);

        // Centre the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - this.setupOptionsScreen.getWidth()) / 2;
        int y = (screenSize.height - this.setupOptionsScreen.getHeight()) / 2;
        this.setupOptionsScreen.setLocation(x, y);
    }

    /**
     * Setup the instructions text area.
     */
    public void setupInstructions() {
        this.instructions = new JTextPane();

        this.instructions.setText(
                "Please select 'Use config file...' if you have a JSON config file ready. " +
                        "Otherwise, please select 'Use manual setup' where you will be guided through a configuration setup."
        );

        this.instructions.setMaximumSize(new Dimension(400, 130));
        this.instructions.setPreferredSize(new Dimension(400, 130));
        this.instructions.setMinimumSize(new Dimension(400, 130));
        this.instructions.setBorder(BorderCreator.createEmptyBorderLeavingBottom(20));
        this.instructions.setFont(new Font("Helvetica Neue", Font.PLAIN, 18));
        this.instructions.setEditable(false);
        this.instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

}
