package view;

import controller.AppController;
import model.StudentId;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Grade Box Class.
 */
public class GradeBox extends JPanel {

    // Class variable
    private static final String GRADE = "Grade: ";
    private static final double MINIMUM = 0.0;
    private static final double MAXIMUM = 20.0;
    private static final double STEP = 0.5;

    // Instance variables
    private final AppController controller;
    private JLabel label;
    private JSpinner chooser;
    private StudentId studentId;

    /**
     * Constructor.
     *
     * @param controller The controller.
     */
    public GradeBox(AppController controller) {
        this.controller = controller;

        this.setupLabel();
        this.setupChooser();

        // Add some padding to the bottom on the panel and make it visible
        this.setBorder(BorderCreator.createEmptyBorderLeavingTop(BorderCreator.PADDING_20_PIXELS));
        this.setVisible(true);
    }

    private void setupLabel() {
        this.label = new JLabel(GRADE);
        this.add(this.label);
    }

    private void setupChooser() {
        chooser = new JSpinner(new SpinnerNumberModel(MINIMUM, MINIMUM, MAXIMUM, STEP));
        chooser.addChangeListener(e -> controller.updateGrade(studentId, getGrade()));
        add(this.chooser);
    }

    /**
     * Get the grade value.
     *
     * @return The grade value.
     */
    public double getGrade() throws NumberFormatException {
        // Get the string version of the grade
        double grade = (double) this.chooser.getValue();

        // Parse string into a valid double
        if (grade < MINIMUM || grade > MAXIMUM) {
            throw new NumberFormatException();
        }
        return grade;
    }

    /**
     * Set the grade on the UI.
     *
     * @param grade The grade value to set.
     */
    public void setGrade(double grade) {
        this.chooser.setValue(grade);
    }

    /**
     * Set the student ID that the grade is for.
     *
     * @param studentId The student ID that the grade is for.
     */
    public void setStudentId(StudentId studentId) {
        this.studentId = studentId;
    }
}
