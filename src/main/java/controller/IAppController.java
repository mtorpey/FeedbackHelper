package controller;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import model.Assignment;
import model.StudentId;
import view.PhraseType;

/**
 * App Controller Interface.
 */
public interface IAppController {
    /**
     * Register as a subscriber of the model.
     *
     * @param propertyChangeListener The listener object to register with the model.
     */
    void registerWithModel(PropertyChangeListener propertyChangeListener);

    /**
     * Create the feedback files for the assignment in the document database.
     *
     * @param assignment              The assignment.
     */
    public void createFeedbackDocuments(Assignment assignment);

    /**
     * Create an assignment in the model.
     *
     * @param assignmentTitle         The title of the assignment.
     * @param headings                The headings of the feedback document.
     * @param studentListFile     The student list file.
     * @param assignmentDirectory The directory location to save assignment related documents.
     * @return - The Assignment object that was created.
     */
    Assignment createAssignment(
        String assignmentTitle,
        String headings,
        Path studentListFile,
        Path assignmentDirectory
    ) throws IOException;

    /**
     * Set the style preferences for an assignment's exports.
     *
     * @param headingStyle   The heading style.
     * @param underlineStyle The heading underline style.
     * @param lineSpacing    The line spacing after each section.
     * @param lineMarker     The line marker for each new line.
     */
    void setAssignmentPreferences(String headingStyle, String underlineStyle, int lineSpacing, String lineMarker);

    /**
     * Load an assignment from an FHT file.
     *
     * @param fhtFile Path to the assignment's FHT file.
     * @return The Assignment object for the assignment.
     */
    Assignment loadAssignment(Path fhtFile);

    /**
     * Save an assignment.
     *
     * @param assignment The assignment to save.
     */
    void saveAssignment(Assignment assignment);

    /**
     * Get the line marker to use for denoting new lines.
     *
     * @return The line marker.
     */
    String getLineMarker();

    /**
     * Get the heading style to use for headings when files are exported.
     *
     * @return The heading style.
     */
    String getHeadingStyle();

    /**
     * Get the heading underline style to use for headings when files are exported.
     *
     * @return The heading underline style.
     */
    String getUnderlineStyle();

    /**
     * Get the number of line spaces to use between sections when files are exported.
     *
     * @return The number of line spaces.
     */
    int getLineSpacing();

    /**
     * Ask the model to save a feedback document.
     *
     * @param studentId The ID of the feedback document to save.
     */
    void saveFeedbackDocument(StudentId studentId);

    /**
     * Save the feedback document.
     *
     * @param assignment      The assignment the feedback document belows to
     * @param studentId       The ID of the feedback document to save.
     * @param headingsAndData The feedback data to save.
     * @param grade           The grade to save.
     */
    void saveFeedbackDocument(
        Assignment assignment,
        StudentId studentId,
        Map<String, String> headingsAndData,
        double grade
    );

    /**
     * Get the last document ID that was edited.
     *
     * @return The last document's ID.
     */
    StudentId getLastDocumentInView();

    /**
     * Get the document ID of the current document being edited.
     *
     * @return The current document's ID.
     */
    StudentId getCurrentDocumentInView();

    /**
     * Update the model with the current ID of the document that is being edited.
     *
     * @param studentId The current document's ID.
     */
    void setCurrentDocumentInView(StudentId studentId);

    /**
     * Process a request to display a new document to edit.
     *
     * @param assignment The assignment the document belongs to.
     * @param studentId  The ID of the document to display.
     */
    void displayNewDocument(Assignment assignment, StudentId studentId);

    /**
     * Get the first line of a feedback document.
     *
     * @param assignment The assignment the document belongs to.
     * @param studentId  The student id of the document.
     * @return The first line of the document if it exists or a default message.
     */
    String getFirstLineFromDocument(Assignment assignment, StudentId studentId);

    /**
     * Notify the model of the current feedback box heading being edited.
     *
     * @param currentHeadingBeingEdited The current heading being edited.
     */
    void updateCurrentHeadingBeingEdited(String currentHeadingBeingEdited);

    /**
     * Get the current feedback box heading being edited.
     *
     * @return The current heading being edited.
     */
    String getCurrentHeadingBeingEdited();

    /**
     * Determine whether the user has navigated to a new feedback box.
     *
     * @return True if a new heading is being edited, false otherwise.
     */
    boolean headingChanged();

    /**
     * Check the current feedback box heading.
     *
     * @param previousheading           The current feedback box heading being edited.
     * @param currentHeading            The new feedback box heading
     */
    void checkHeading(String previousheading, String newHeading);

    /**
     * Change the current feedback box heading.
     *
     * @param previousheading           The current feedback box heading being edited.
     * @param currentHeading            The new feedback box heading
     */
    void updateHeading(String previousheading, String newHeading);

    /**
     * Export the feedback documents.
     *
     * @param assignment The assignment the feedback documents belong to.
     */
    void exportFeedbackDocuments(Assignment assignment);

    /**
     * Export the grade document.
     *
     * @param assignment The assignment the grade document belongs to.
     */
    void exportGrades(Assignment assignment);

    /**
     * Create a bar chart visualisation of the grades.
     *
     * @param assignment The assignment grades to visualise.
     */
    void visualiseGrades(Assignment assignment);

    /**
     * Get a summary of all the feedback documents.
     *
     * @param assignment The assignment to summarise.
     * @return A map of headings and the 3 most used phrases for those headings.
     */
    Map<String, List<String>> getSummary(Assignment assignment);

    /**
     * Insert a phrase into the current feedback box being edited.
     *
     * @param phrase The string representation of the phrase to be inserted.
     */
    void insertPhraseIntoCurrentFeedbackBox(String phrase);

    /**
     * Show all the frequently used phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     */
    void showPhrasesForHeading(String heading);

    /**
     * Add a custom phrase from the GUI.
     *
     * @param phrase The string representation of the phrase to be added.
     */
    void addNewCustomPhraseFromView(String phrase);

    /**
     * Manage the links between phrases.
     *
     * @param heading             The current feedback box heading being edited.
     * @param previousBoxContents The last list of phrases for the feedback box.
     * @param currentBoxContents  The current list of phrases for the feedback box.
     */
    void managePhraseLinks(String heading, List<String> previousBoxContents, List<String> currentBoxContents);

    /**
     * Update the phrases stored in the graph database.
     *
     * @param heading             The current feedback box heading being edited.
     * @param previousBoxContents The last list of phrases for the feedback box.
     * @param currentBoxContents  The current list of phrases for the feedback box.
     */
    void updatePhrases(String heading, List<String> previousBoxContents, List<String> currentBoxContents);

    /**
     * Ask the model to reset the phrases panel.
     */
    void resetPhrasesPanel(PhraseType phrasePanel);

    /**
     * Get the custom phrases and display them.
     */
    void showCustomPhrases();

    /**
     * Set the phrase panel the user is currently viewing.
     *
     * @param currentPhrasePanelInView The phrase panel type.
     */
    void setCurrentPhrasePanelInView(PhraseType currentPhrasePanelInView);

    /**
     * Show the user an error message.
     *
     * @param errorMessage The error message to show.
     */
    void error(String errorMessage);
}
