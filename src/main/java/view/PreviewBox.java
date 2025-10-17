package view;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import controller.IAppController;
import model.Assignment;
import model.StudentId;

/**
 * Preview Box Class.
 */
public class PreviewBox extends JPanel implements Comparable<PreviewBox> {

    // Instance variables
    private final IAppController controller;
    private StudentId heading;
    private String firstLine;
    private double grade;
    private JTextArea textPane;
    private Border unselectedBorder;
    private Border selectedBorder;
    private Assignment assignment;

    /**
     * Constructor.
     *
     * @param controller The controller.
     * @param heading    The heading of the preview box (a student ID)
     * @param grade      The grade of the student.
     * @param firstLine  A unique line from the student's feedback document.
     */
    public PreviewBox(IAppController controller, StudentId heading, double grade, String firstLine) {
        // Store variables
        this.controller = controller;
        this.heading = heading;
        this.firstLine = firstLine;
        this.grade = grade;

        // Layout components from top to bottom on this panel
        this.setLayout(new BorderLayout());

        // Setup components
        setupBorders();
        setupTextArea();

        // Add some padding to the bottom on the panel and make it visible
        this.setBorder(BorderCreator.createEmptyBorderLeavingBottom(BorderCreator.PADDING_20_PIXELS));
        this.setVisible(true);
    }

    /**
     * Setup the borders for when a box is selected and unselected.
     */
    private void setupBorders() {
        this.unselectedBorder = BorderCreator.unselectedBorder();
        this.selectedBorder = BorderCreator.selectedBorder();
    }

    /**
     * Set the assignment.
     *
     * @param assignment The assignment.
     */
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    /**
     * Setup the text area.
     */
    private void setupTextArea() {
        // Set properties
        this.textPane = new JTextArea();
        this.textPane.setRows(5);
        this.textPane.setBorder(unselectedBorder);
        this.textPane.setEditable(false);
        this.textPane.setLineWrap(false);

        // Listen for clicks on the text area
        this.textPane.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    controller.displayNewDocument(assignment, heading);
                }
            }
        );

        // Set the contents of the preview box
        this.textPane.setText(this.heading + "\n\n" + this.firstLine + "\n\n" + "Grade: " + this.grade);
        this.add(this.textPane, BorderLayout.CENTER);
    }

    /**
     * Get the heading of the preview box.
     *
     * @return The heading of the preview box.
     */
    public StudentId getHeading() {
        return this.heading;
    }

    /**
     * Highlight the preview box.
     */
    public void highlight() {
        this.textPane.setBorder(this.selectedBorder);
        this.textPane.repaint();
        this.textPane.revalidate();
    }

    /**
     * Unhighlight the preview box.
     */
    public void unhighlight() {
        this.textPane.setBorder(this.unselectedBorder);
        this.textPane.repaint();
        this.textPane.revalidate();
    }

    /**
     * Set the grade in the preview box.
     *
     * @param grade The grade to display.
     */
    public void setGrade(double grade) {
        this.grade = grade;
        this.updatePreviewBox();
    }

    /**
     * Set the unique line in the preview box.
     *
     * @param line The unique line to display.
     */
    public void setFirstLine(String line) {
        this.firstLine = line;
        this.updatePreviewBox();
    }

    /**
     * Update the preview box.
     */
    private void updatePreviewBox() {
        this.textPane.setText("");
        this.textPane.setText(this.heading + "\n\n" + this.firstLine + "\n\n" + "Grade: " + this.grade);
        this.textPane.repaint();
        this.textPane.revalidate();
    }

    /**
     * Compare the preview box to another preview box.
     *
     * Uses headings.
     */
    @Override
    public int compareTo(PreviewBox other) {
        return getHeading().compareTo(other.getHeading());
    }
}
