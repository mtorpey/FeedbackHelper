package controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import configuration.UserPreferences;
import model.Assignment;
import model.AssignmentListener;
import model.FeedbackStyle;
import model.StudentId;

/**
 * App Controller Class.
 */
public class AppController {

    // Instance variables
    private Assignment assignment;

    /**
     * Register the given view as a listener of the model.
     *
     * @param listener The listener object to register with the model.
     * @return The model itself, so the view has a reference to it.
     */
    public Assignment registerWithModel(AssignmentListener listener) {
        assignment.addListener(listener);
        return assignment;
    }

    /* ASSIGNMENT METHODS */

    /**
     * Create a new assignment, set it as the model of this controller, and return it.
     *
     * @param title The title of the assignment.
     * @param headings The headings of the feedback document.
     * @param studentListFile The student list file.
     * @param directory The directory location to save assignment related documents.
     * @param headingStyle   The heading style.
     * @param underlineStyle The heading underline style.
     * @param lineSpacing    The line spacing after each section.
     * @param lineMarker     The line marker for each new line.
     * @return The Assignment object that was created.
     */
    public Assignment createAssignment(String title, String headings, Path studentListFile, Path directory, String headingStyle, String underlineStyle, int lineSpacing, String lineMarker)
        throws IOException {
        if (assignment != null) {
            throw new RuntimeException("Cannot create new assignment when one already exists.");
        }
        FeedbackStyle feedbackStyle = new FeedbackStyle(headingStyle, underlineStyle, lineSpacing, lineMarker);
        assignment = new Assignment(title, headings, studentListFile, directory, feedbackStyle);

        // Since this was successful, remember it as the default for next load
        UserPreferences.setLastOpenedAssignment(directory);

        return this.assignment;
    }

    /**
     * Load an assignment from an FHT file, set it as the model of this controller, and return it.
     */
    public Assignment loadAssignment(Path fhtFile) throws IOException, ClassNotFoundException, ClassCastException {
        this.assignment = Assignment.load(fhtFile);

        // Since this was successful, remember it as the default for next load
        UserPreferences.setLastOpenedAssignment(fhtFile);

        return assignment;
    }

    /** Whether this controller has an assignment it is working on yet. */
    public boolean hasAssignment() {
        return assignment != null;
    }

    /**
     * Save the assignment to disk.
     */
    public void saveAssignment() {
        assignment.save();
    }

    /* FEEDBACK DOCUMENT METHODS */

    public void updateFeedbackSection(StudentId studentId, String heading, String contents) {
        Map<String, String> headingsAndData = Map.of(heading, contents);
        assignment.updateFeedback(studentId, headingsAndData);
        assignment.save();
    }

    public void updateGrade(StudentId studentId, double grade) {
        updateFeedbackAndGrade(studentId, Map.of(), grade);
    }

    /**
     * Update the feedback document and grade for a particular student in the model, and save to disk.
     *
     * @param studentId       The ID of the feedback document to save.
     * @param headingsAndData The feedback data to save.
     * @param grade           The grade to save.
     */
    public void updateFeedbackAndGrade(StudentId studentId, Map<String, String> headingsAndData, double grade) {
        assignment.updateFeedback(studentId, headingsAndData);
        assignment.updateGrade(studentId, grade);
        assignment.save();
    }

    /**
     * Attempt to add a new student with the given ID to the model.
     *
     * @throws IllegalArgumentException if the input is not a valid student ID.
     */
    public void addNewStudent(String input) throws IllegalArgumentException {
        StudentId studentId = new StudentId(input);
        assignment.addStudent(studentId);
    }

    /* HEADING MANAGEMENT METHODS */

    /**
     * Change the current feedback box heading.
     *
     * @param previousHeading           The current feedback box heading being edited.
     * @param currentHeading            The new feedback box heading
     */
    public void editHeading(String previousHeading, String newHeading) {
        newHeading = newHeading.replaceAll("\n", "").trim(); // Remove all new lines
        assignment.editHeading(previousHeading, newHeading);
    }

    /* USER EXPORTS AND OPERATIONS */

    /**
     * Export the feedback documents and grades.
     */
    public void exportFeedbackAndGrades() {
        assignment.export();
    }

    /* PHRASE MANAGEMENT METHODS */

    /**
     * Add a custom phrase from the GUI.
     *
     * @param phrase The string representation of the phrase to be added.
     */
    public void addCustomPhrase(String heading, String phrase) {
        // Filter out empty lines
        phrase = phrase.trim();
        if (!phrase.isEmpty() && !phrase.equals(assignment.getLineMarker())) {
            assignment.addCustomPhrase(heading, phrase);
        }
    }

    public void deleteCustomPhrase(String heading, String phrase) {
        assignment.deleteCustomPhrase(heading, phrase);
    }
}
