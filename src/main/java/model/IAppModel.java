package model;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.List;

import view.PhraseType;

/**
 * App Model Interface.
 */
public interface IAppModel {
    /**
     * Allow an observer to subscribe for changes to the model.
     *
     * @param listener The observer that is subscribing to the model's changes.
     */
    void subscribe(PropertyChangeListener listener);

    /**
     * Send a property change notification to subscribers.
     *
     * @param property     The message indicating the change.
     * @param notification The new String value of the thing that has changed.
     */
    void notifySubscribers(String property, String notification);

    /**
     * Send a property change notification to subscribers.
     *
     * @param property     The message indicating the change.
     * @param notification The new Object value of the thing that has changed.
     */
    void notifySubscribers(String property, Object notification);

    /**
     * Send a property change notification to subscribers.
     *
     * @param property     The message indicating the change.
     * @param oldNotification The old Object value of the thing that changed.
     * @param notification The new Object value of the thing that has changed.
     */
    void notifySubscribers(String property, Object oldNotification, Object notification);

    /**
     * Create an assignment.
     *
     * @param assignmentTitle         The title of the assignment.
     * @param assignmentHeadings      The headings of the feedback document.
     * @param studentManifestFile     The student list file.
     * @param assignmentDirectoryPath The directory location to save assignment related documents.
     * @return - The Assignment object that was created.
     */
    Assignment createAssignment(
        String assignmentTitle,
        String assignmentHeadings,
        Path studentListFile,
        Path assignmentDirectory
    ) throws NotDirectoryException, IOException;

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
    void setCurrentDocumentInView(StudentId studentId, boolean changeDoc);

    /**
     * Get the current feedback box heading being edited.
     *
     * @return The current heading being edited.
     */
    String getCurrentHeadingBeingEdited();

    /**
     * Set the current feedback box heading being edited.
     *
     * @param currentHeadingBeingEdited The current heading being edited.
     */
    void setCurrentHeadingBeingEdited(String currentHeadingBeingEdited);

    /**
     * Get the previous feedback box heading that was edited.
     *
     * @return The previous heading that was edited.
     */
    String getPreviousHeadingBeingEdited();

    /**
     * Export the feedback documents.
     *
     * @param assignment The assignment the feedback documents belong to.
     */
    void exportFeedbackDocuments(Assignment assignment);

    /**
     * Export the grades of an assignment as a text file.
     *
     * @param assignment The assignment grades to export.
     */
    void exportGrades(Assignment assignment);

    /**
     * Create an ordered list of grades.
     *
     * @param assignment The assignment the grades are for.
     * @return A list of grades.
     */
    List<Integer> getGrades(Assignment assignment);

    /**
     * Insert a phrase into the current feedback box being edited.
     *
     * @param phrase The string representation of the phrase to be inserted.
     */
    void insertPhraseIntoCurrentFeedbackBox(String phrase);

    /**
     * Add new phrase.
     *
     * @param phrase The phrase to add.
     */
    void addNewPhraseToView(Phrase phrase);

    /**
     * Add new custom phrase.
     *
     * @param phrase The custom phrase to add.
     */
    void addNewCustomPhraseToView(Phrase phrase);

    /**
     * Update the counter on a phrase.
     *
     * @param phrase The phrase to update.
     */
    void updatePhraseCounterInView(Phrase phrase);

    /**
     * Remove a phrase.
     *
     * @param phrase The phrase to remove.
     */
    void removePhraseFromView(Phrase phrase);

    /**
     * Reset the phrases panel.
     */
    void resetPhrasesPanel(PhraseType phraseType);

    /**
     * Get the list of phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @return A list of phrases for the given heading.
     */
    List<Phrase> getCurrentPhraseSet(String heading);

    /**
     * Set the list of phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @param phrases A list of phrases for the given heading.
     */
    void setCurrentHeadingPhraseSet(String heading, List<Phrase> phrases);

    /**
     * Set the list of phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @param phrases A list of phrases for the given heading.
     */
    void setPreviousHeadingPhraseSet(String heading, List<Phrase> phrases);

    /**
     * Set the phrase panel the user is currently viewing.
     *
     * @param currentPhrasePanelInView The phrase panel type.
     */
    void setCurrentPhrasePanelInView(PhraseType currentPhrasePanelInView);
}
