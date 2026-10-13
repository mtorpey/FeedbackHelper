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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import uk.myoung.feedbackhelper.model.AssignmentReadOnly;
import uk.myoung.feedbackhelper.view.style.BorderCreator;

/**
 * Dialog window for editing headers (adding, renaming, deleting, reordering).
 */
public class EditHeadersDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final int SPACING = 5;
    private static final int PADDING = 10;

    private final AssignmentReadOnly assignment;

    private DefaultListModel<String> listModel;
    private JList<String> headersList;
    private JButton addButton;
    private JButton renameButton;
    private JButton deleteButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton closeButton;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param parent The parent frame (or null).
     * @param assignment The assignment being edited.
     */
    public static EditHeadersDialog create(JFrame parent, AssignmentReadOnly assignment) {
        EditHeadersDialog dialog = new EditHeadersDialog(parent, assignment);
        dialog.setupComponents();

        dialog.pack();
        dialog.setMinimumSize(dialog.getSize());
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        return dialog;
    }

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param assignment The assignment being edited.
     */
    public static EditHeadersDialog create(AssignmentReadOnly assignment) {
        return create(null, assignment);
    }

    private EditHeadersDialog(JFrame parent, AssignmentReadOnly assignment) {
        super(parent, "Edit Headers", true);
        this.assignment = assignment;
    }

    /**
     * Setup dialog components.
     */
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

        JScrollPane scrollPane = new JScrollPane(headersList);
        scrollPane.setBorder(BorderCreator.textAreaBorder());
        scrollPane.setPreferredSize(new Dimension(240, 180));
        add(scrollPane, BorderLayout.CENTER);

        // Right: Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(6, 1, 0, SPACING));
        addButton = new JButton("Add");
        renameButton = new JButton("Rename");
        deleteButton = new JButton("Delete");
        moveUpButton = new JButton("Move Up");
        moveDownButton = new JButton("Move Down");
        closeButton = new JButton("Close");

        closeButton.addActionListener(e -> dispose());

        buttonsPanel.add(addButton);
        buttonsPanel.add(renameButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(moveUpButton);
        buttonsPanel.add(moveDownButton);
        buttonsPanel.add(closeButton);

        add(buttonsPanel, BorderLayout.EAST);
    }
}
