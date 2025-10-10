package view;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import controller.IAppController;

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
    private final IAppController controller;
    private JLabel label;
    private JSpinner chooser;
    private String studentId;

    /**
     * Constructor.
     *
     * @param controller The controller.
     */
    public GradeBox(IAppController controller) {
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
        this.chooser = new JSpinner(new SpinnerNumberModel(MINIMUM, MINIMUM, MAXIMUM, STEP));

        this.chooser.addChangeListener(e -> saveGrade());
        this.add(this.chooser);
    }

    private void saveGrade() {
        try {
            getGrade();
            this.controller.saveFeedbackDocument(this.studentId);
        } catch (NumberFormatException e) {}
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
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
