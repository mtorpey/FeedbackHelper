package uk.myoung.feedbackhelper.view.feedbackscreen.editor;

import java.text.ParseException;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import uk.myoung.feedbackhelper.view.style.BorderCreator;

/**
 * Grade Box Class.
 */
public class GradeBox extends JPanel {

    // Class variable
    private static final String LABEL_TEXT = "Grade: ";
    private static final double MINIMUM = 0.0;
    private static final double MAXIMUM = 20.0;
    private static final double STEP = 0.5;

    // Instance variables
    private Consumer<Double> onUpdateGrade;
    private JSpinner chooser;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param onUpdateGrade Callback to update the grade in the model.
     */
    public static GradeBox create(Consumer<Double> onUpdateGrade) {
        GradeBox box = new GradeBox(onUpdateGrade);

        // Add components
        box.add(new JLabel(LABEL_TEXT));
        box.setupChooser();

        // Add some padding to the bottom on the panel and make it visible
        box.setBorder(BorderCreator.emptyBorderSmall());
        box.setVisible(true);

        return box;
    }

    private GradeBox(Consumer<Double> onUpdateGrade) {
        this.onUpdateGrade = onUpdateGrade;
    }

    private void setupChooser() {
        chooser = new JSpinner(new SpinnerNumberModel(MINIMUM, MINIMUM, MAXIMUM, STEP));
        chooser.addChangeListener(e -> onUpdateGrade.accept(getGrade()));
        chooser.setEnabled(false);
        add(this.chooser);
    }

    public void enableChooser() {
        chooser.setEnabled(true);
    }

    /**
     * Get the grade value.
     *
     * @return The grade value.
     */
    public double getGrade() throws NumberFormatException {
        try {
            chooser.commitEdit();
        } catch (ParseException e) {
            // Reset to a known value
        }

        // Get the string version of the grade
        double grade = (double) chooser.getValue();
        setGrade(grade); // in case an invalid value is in the cell

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
}
