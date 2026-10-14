package uk.myoung.feedbackhelper.view.feedbackscreen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import uk.myoung.feedbackhelper.controller.AppController;
import uk.myoung.feedbackhelper.model.AssignmentReadOnly;
import uk.myoung.feedbackhelper.view.style.BorderCreator;

/**
 * Dialog window for editing headers (adding, renaming, deleting, reordering).
 */
public class EditHeadersDialog extends JDialog {

    private static final int SPACING = 5;
    private static final int PADDING = 10;

    private final AppController controller;
    private final AssignmentReadOnly assignment;
    private DefaultListModel<String> listModel;
    private JList<String> headersList;
    private JButton addButton;
    private JButton renameButton;
    private JButton deleteButton;
    private JButton closeButton;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param parent     The parent frame (or null).
     * @param controller The controller.
     * @param assignment The assignment being edited.
     */
    public static EditHeadersDialog create(JFrame parent, AppController controller, AssignmentReadOnly assignment) {
        EditHeadersDialog dialog = new EditHeadersDialog(parent, controller, assignment);
        dialog.setupComponents();
        dialog.pack();
        dialog.setMinimumSize(dialog.getSize());
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        return dialog;
    }

    public static EditHeadersDialog create(JFrame parent, AssignmentReadOnly assignment) {
        return create(parent, null, assignment);
    }

    private EditHeadersDialog(JFrame parent, AppController controller, AssignmentReadOnly assignment) {
        super(parent, "Edit Headers", true);
        this.controller = controller;
        this.assignment = assignment;
    }

    private void setupComponents() {
        setLayout(new BorderLayout(SPACING, SPACING));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Left: List of headers
        listModel = new DefaultListModel<>();
        if (assignment != null && assignment.getHeadings() != null) {
            assignment.getHeadings().forEach(listModel::addElement);
        }
        headersList = new JList<>(listModel);
        headersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        headersList.setBorder(BorderCreator.emptyBorderSmall());

        if (!listModel.isEmpty()) {
            headersList.setSelectedIndex(0);
        }

        JScrollPane scrollPane = new JScrollPane(headersList);
        scrollPane.setBorder(BorderCreator.textAreaBorder());
        scrollPane.setPreferredSize(new Dimension(240, 180));
        add(scrollPane, BorderLayout.CENTER);

        // Right: Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 0, SPACING));
        addButton = new JButton("Add");
        renameButton = new JButton("Rename");
        deleteButton = new JButton("Delete");
        closeButton = new JButton("Close");

        closeButton.addActionListener(e -> dispose());
        addButton.addActionListener(e -> addHeader());
        renameButton.addActionListener(e -> renameSelectedHeader());
        deleteButton.addActionListener(e -> deleteSelectedHeader());

        buttonsPanel.add(addButton);
        buttonsPanel.add(renameButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(closeButton);

        add(buttonsPanel, BorderLayout.EAST);
    }

    private void deleteSelectedHeader() {
        int selectedIndex = headersList.getSelectedIndex();
        if (selectedIndex == -1) {
            return;
        }

        if (listModel.size() <= 1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cannot delete the only header.",
                    "Delete Header",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String heading = listModel.get(selectedIndex);

        int studentsWithData = assignment.numberOfStudentsWithHeadingContent(heading);

        String message = "Are you sure you want to delete '" + heading + "'?";
        if (studentsWithData > 0) {
            message += "\nYou will lose data for "
                    + studentsWithData + (studentsWithData == 1 ? " student." : " students.");
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                message,
                "Delete Header",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            controller.deleteHeading(heading);

            listModel.remove(selectedIndex);

            if (!listModel.isEmpty()) {
                headersList.setSelectedIndex(Math.min(selectedIndex, listModel.size() - 1));
            }
        }
    }

    private void addHeader() {
        String input = JOptionPane.showInputDialog(
                this,
                "Enter the new header name",
                "Add Header",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null) {
            return;
        }

        String heading = input.trim();
        if (heading.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Header name cannot be blank.",
                    "Add Header",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (listModel.contains(heading)) {
            JOptionPane.showMessageDialog(
                    this,
                    "The header '" + heading + "' already exists.",
                    "Add Header",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (controller != null) {
            controller.addHeading(heading);
        }
        listModel.addElement(heading);
        headersList.setSelectedValue(heading, true);
    }

    private void renameSelectedHeader() {
        int selectedIndex = headersList.getSelectedIndex();
        if (selectedIndex == -1) {
            return;
        }

        String currentHeading = listModel.get(selectedIndex);

        String input = (String) JOptionPane.showInputDialog(
                this,
                "Enter the new header name",
                "Rename Header",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                currentHeading);
        if (input == null) {
            return;
        }

        String newHeading = input.trim();
        if (newHeading.equals(currentHeading)) {
            return;
        }

        if (newHeading.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Header name cannot be blank.",
                    "Rename Header",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (listModel.contains(newHeading)) {
            JOptionPane.showMessageDialog(
                    this,
                    "The header '" + newHeading + "' already exists.",
                    "Rename Header",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (controller != null) {
            controller.editHeading(currentHeading, newHeading);
        }

        listModel.set(selectedIndex, newHeading);
        headersList.setSelectedIndex(selectedIndex);
    }
}
