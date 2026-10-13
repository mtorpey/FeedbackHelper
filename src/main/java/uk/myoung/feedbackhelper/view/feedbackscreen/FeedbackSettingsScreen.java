package uk.myoung.feedbackhelper.view.feedbackscreen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import uk.myoung.feedbackhelper.model.AssignmentReadOnly;
import uk.myoung.feedbackhelper.view.style.BorderCreator;
import uk.myoung.feedbackhelper.view.style.LogoIcon;

/**
 * Configuration window for editing feedback settings
 */
public class FeedbackSettingsScreen extends JFrame {

    // Style constants
    private static final int SPACING = 5;
    private static final int GRID_WIDTH = 3;

    // Instance variables
    private final AssignmentReadOnly assignment;
    private JPanel configFormPanel;

    // How many cells on the config panel are filled
    private int gridBagCounter;

    // Components with user inputs
    private JList<String> headingsList;
    private JButton editHeadersButton;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param controller The controller.
     * @param assignment The assignment.
     */
    public static FeedbackSettingsScreen create(AssignmentReadOnly assignment) {
        FeedbackSettingsScreen screen = new FeedbackSettingsScreen(assignment);

        // Set icon
        LogoIcon.applyIcon(screen);

        // Setup components
        screen.setLayout(new BorderLayout(SPACING, SPACING));
        ((JPanel) screen.getContentPane()).setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        screen.setupConfigForm();
        screen.setupConfirmationPanel();

        // Finish setting up as a JFrame
        screen.pack();
        screen.setMinimumSize(screen.getSize());
        screen.setLocationRelativeTo(null);
        screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        screen.setVisible(true);

        return screen;
    }

    private FeedbackSettingsScreen(AssignmentReadOnly assignment) {
        super("Feedback settings");
        this.assignment = assignment;
    }

    /**
     * Setup the config form.
     */
    private void setupConfigForm() {
        this.configFormPanel = new JPanel(new GridBagLayout());
        this.gridBagCounter = 0;

        setupAssignmentHeadingsControls();

        add(this.configFormPanel, BorderLayout.CENTER);
    }

    /**
     * Setup the assignment headings controls.
     */
    private void setupAssignmentHeadingsControls() {
        String tooltip = "Headings for the sections in the feedback documents.";
        addLabelToConfigForm("Assignment headings:", tooltip);

        JPanel headingsPanel = new JPanel(new BorderLayout(SPACING, SPACING));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        if (assignment != null) {
            assignment.getHeadings().forEach(listModel::addElement);
        }
        headingsList = new JList<>(listModel);
        headingsList.setToolTipText(tooltip);
        headingsList.setBorder(BorderCreator.textAreaBorder());

        JScrollPane scrollPane = new JScrollPane(headingsList);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        headingsPanel.add(scrollPane, BorderLayout.CENTER);

        editHeadersButton = new JButton("Edit headers");
        editHeadersButton.setToolTipText("Edit assignment headers.");
        headingsPanel.add(editHeadersButton, BorderLayout.SOUTH);

        addToConfigForm(headingsPanel, true, GRID_WIDTH);
    }

    /**
     * Setup the confirmation panel.
     */
    private void setupConfirmationPanel() {
        JPanel confirmationPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        confirmationPanel.add(closeButton);
        add(confirmationPanel, BorderLayout.SOUTH);
    }

    /**
     * Add a label component to the config form, centered and spanning full width.
     */
    private void addLabelToConfigForm(String text, String tooltip) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setToolTipText(tooltip);
        addToConfigForm(label, GRID_WIDTH);
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
     * 
     * @param component The component to add
     * @param fill      Whether this component should be maximised horizontally
     * @param width     How many cells wide this should be
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
}
