package controller;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import configuration.UserPreferences;
import model.AppModel;
import model.Assignment;
import model.FeedbackDocument;
import model.Phrase;
import model.StudentId;
import view.PhraseType;
import visualisation.Visualisations;

/**
 * App Controller Class.
 */
public class AppController {

    // Instance variables
    private final AppModel appModel;

    /**
     * Constructor.
     *
     * @param appModel - The model to interact with.
     */
    public AppController(AppModel appModel) {
        this.appModel = appModel;
    }

    /**
     * Register as a subscriber of the model.
     *
     * @param propertyChangeListener The listener object to register with the model.
     */
    public void registerWithModel(PropertyChangeListener propertyChangeListener) {
        appModel.subscribe(propertyChangeListener);
    }

    /* ASSIGNMENT METHODS */

    /**
     * Create a new assignment in the model.
     *
     * @param assignmentTitle         The title of the assignment.
     * @param headings                The headings of the feedback document.
     * @param studentListFile     The student list file.
     * @param assignmentDirectory The directory location to save assignment related documents.
     * @return - The Assignment object that was created.
     */
    public Assignment createAssignment(
        String assignmentTitle,
        String headings,
        Path studentListFile,
        Path assignmentDirectory
    ) throws IOException {
        // Create assignment in the model
        Assignment assignment = appModel.createAssignment(
            assignmentTitle,
            headings,
            studentListFile,
            assignmentDirectory
        );
        assignment.save();

        return assignment;
    }

    /**
     * Set the style preferences for an assignment's exports.
     *
     * @param headingStyle   The heading style.
     * @param underlineStyle The heading underline style.
     * @param lineSpacing    The line spacing after each section.
     * @param lineMarker     The line marker for each new line.
     */
    public void setAssignmentPreferences(
        String headingStyle,
        String underlineStyle,
        int lineSpacing,
        String lineMarker
    ) {
        appModel.setAssignmentPreferences(headingStyle, underlineStyle, lineSpacing, lineMarker);
    }

    /**
     * Load an assignment from an FHT file.
     *
     * @param fhtFile Path to the assignment's FHT file
     * @return The Assignment object for the assignment.
     */
    public Assignment loadAssignment(Path fhtFile) {
        // Load the assignment
        Assignment assignment = appModel.loadAssignment(fhtFile);

        // Since this was successful, remember it as the default for next load
        UserPreferences.setLastOpenedAssignment(fhtFile);

        return assignment;
    }

    /**
     * Save an assignment.
     *
     * @param assignment The assignment to save.
     */
    public void saveAssignment(Assignment assignment) {
        appModel.saveAssignment(assignment);
    }

    /**
     * Get the line marker to use for denoting new lines.
     *
     * @return The line marker.
     */
    public String getLineMarker() {
        return appModel.getLineMarker();
    }

    /**
     * Get the heading style to use for headings when files are exported.
     *
     * @return The heading style.
     */
    public String getHeadingStyle() {
        return appModel.getHeadingStyle();
    }

    /**
     * Get the heading underline style to use for headings when files are exported.
     *
     * @return The heading underline style.
     */
    public String getUnderlineStyle() {
        return appModel.getUnderlineStyle();
    }

    /**
     * Get the number of line spaces to use between sections when files are exported.
     *
     * @return The number of line spaces.
     */
    public int getLineSpacing() {
        return appModel.getLineSpacing();
    }

    /* FEEDBACK DOCUMENT METHODS */

    public void updateFeedback(StudentId studentId, String heading, String contents) {
        Map<String, String> headingsAndData = Map.of(heading, contents);
        appModel.updateFeedback(studentId, headingsAndData);
    }

    /**
     * Ask the model to save a feedback document.
     *
     * @param studentId The ID of the feedback document to save.
     */
    public void saveFeedbackDocument(StudentId studentId) {
        appModel.notifySubscribers("saveDoc", studentId);
    }

    /**
     * Save the feedback document.
     *
     * @param assignment      The assignment the feedback document belows to
     * @param studentId       The ID of the feedback document to save.
     * @param headingsAndData The feedback data to save.
     * @param grade           The grade to save.
     */
    public void saveFeedbackDocument(
        Assignment assignment,
        StudentId studentId,
        Map<String, String> headingsAndData,
        double grade
    ) {
        assignment.updateFeedback(studentId, headingsAndData);
        assignment.updateGrade(studentId, grade);
        try {
            assignment.save();
        } catch (IOException e) {
            error(e.toString());
        }
    }

    /**
     * Get the last document ID that was edited.
     *
     * @return The last document's ID.
     */
    public StudentId getLastDocumentInView() {
        return appModel.getLastDocumentInView();
    }

    /**
     * Get the document ID of the current document being edited.
     *
     * @return The current document's ID.
     */
    public StudentId getCurrentDocumentInView() {
        return appModel.getCurrentDocumentInView();
    }

    /**
     * Update the model with the current ID of the document that is being edited.
     *
     * @param studentId The current document's ID.
     */
    public void setCurrentDocumentInView(StudentId studentId) {
        appModel.setCurrentDocumentInView(studentId, false);
    }

    /**
     * Process a request to display a new document to edit.
     *
     * @param assignment The assignment the document belongs to.
     * @param studentId  The ID of the document to display.
     */
    public void displayNewDocument(Assignment assignment, StudentId studentId) {
        // Get the latest data for the requested document
        appModel.setCurrentDocumentInView(studentId, true);
    }

    /**
     * Get the first line of a feedback document.
     *
     * @param assignment The assignment the document belongs to.
     * @param studentId  The student id of the document.
     * @return The first line of the document if it exists or a default message.
     */
    public String getFirstLineFromDocument(Assignment assignment, StudentId studentId) {
        // Set the default text
        String returnString = "<no preview available>";

        // Get the student's feedback document
        FeedbackDocument feedbackDocumentForStudent = assignment.getFeedbackDocument(studentId);

        // Find the first line of the document
        for (String heading : assignment.getHeadings()) {
            if (!feedbackDocumentForStudent.getSectionContents(heading).isEmpty()) {
                List<String> dataAsList = Arrays.stream(
                    feedbackDocumentForStudent.getSectionContents(heading).split("\n")
                )
                    .filter(line -> line.startsWith(getLineMarker()))
                    .collect(Collectors.toList());

                // Get the line and remove the line marker
                if (dataAsList.size() > 0) {
                    return heading + ": " + dataAsList.get(0).replace(getLineMarker(), "");
                }
            }
        }

        return returnString;
    }

    /* HEADING MANAGEMENT METHODS */

    /**
     * Notify the model of the current feedback box heading being edited.
     *
     * @param currentHeadingBeingEdited The current heading being edited.
     */
    public void updateCurrentHeadingBeingEdited(String currentHeadingBeingEdited) {
        appModel.setCurrentHeadingBeingEdited(currentHeadingBeingEdited);
    }

    /**
     * Get the current feedback box heading being edited.
     *
     * @return The current heading being edited.
     */
    public String getCurrentHeadingBeingEdited() {
        return appModel.getCurrentHeadingBeingEdited();
    }

    /**
     * Determine whether the user has navigated to a new feedback box.
     *
     * @return True if a new heading is being edited, false otherwise.
     */
    public boolean headingChanged() {
        return !appModel.getCurrentHeadingBeingEdited().equals(appModel.getPreviousHeadingBeingEdited());
    }

    /**
     * Change the current feedback box heading.
     *
     * @param previousHeading           The current feedback box heading being edited.
     * @param currentHeading            The new feedback box heading
     */
    public void editHeading(String previousHeading, String newHeading) {
        newHeading = newHeading.replaceAll("\n", "").trim(); // Remove all new lines

        // Try to edit the heading
        try {
            appModel.editHeading(previousHeading, newHeading);
        } catch (IllegalArgumentException e) {
            error(e.toString());
        }
        appModel.resetFeedbackBoxes();
    }

    /* USER EXPORTS AND OPERATIONS */

    /**
     * Export the feedback documents.
     *
     * @param assignment The assignment the feedback documents belong to.
     */
    public void exportFeedbackDocuments(Assignment assignment) {
        appModel.exportFeedbackDocuments(assignment);
    }

    /**
     * Export the grade document.
     *
     * @param assignment The assignment the grade document belongs to.
     */
    public void exportGrades(Assignment assignment) {
        appModel.exportGrades(assignment);
    }

    /**
     * Create a bar chart visualisation of the grades.
     *
     * @param assignment The assignment grades to visualise.
     */
    public void visualiseGrades(Assignment assignment) {
        List<Integer> grades = appModel.getGrades(assignment);
        Visualisations.createBarChart(grades);
    }

    /* PHRASE MANAGEMENT METHODS */

    /**
     * Insert a phrase into the current feedback box being edited.
     *
     * @param phrase The string representation of the phrase to be inserted.
     */
    public void insertPhraseIntoCurrentFeedbackBox(String phrase) {
        appModel.insertPhraseIntoCurrentFeedbackBox(phrase);
    }

    /**
     * Show all the frequently used phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     */
    public void showPhrasesForHeading(String heading) {
        List<Phrase> currentPhraseSet = appModel.getCurrentPhraseSet(heading);
        if (currentPhraseSet != null) {
            currentPhraseSet.forEach(phrase -> appModel.addNewPhraseToView(heading, phrase));
        }
    }

    /**
     * Show all the custom phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     */
    public void showCustomPhrases(String heading) {
        appModel.showCustomPhrases(heading);
    }

    /**
     * Add a custom phrase from the GUI.
     *
     * @param phrase The string representation of the phrase to be added.
     */
    public void addNewCustomPhraseFromView(String heading, String phrase) {
        // Filter out empty lines
        if (!phrase.trim().isEmpty() && !phrase.trim().equals(getLineMarker())) {
            appModel.addNewCustomPhrase(heading, phrase);
        }
    }

    /**
     * Ask the model to reset the phrases panel.
     */
    public void resetPhrasesPanel(PhraseType phrasePanel) {
        appModel.resetPhrasesPanel(phrasePanel);
    }

    /**
     * Show the user an error message.
     *
     * @param errorMessage The error message to show.
     */
    public void error(String errorMessage) {
        appModel.notifySubscribers("error", errorMessage);
    }
}
