package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import view.PhraseType;

/**
 * App Model Class.
 */
public class AppModel {

    /** Name of file used when writing out grades. */
    private final String GRADES_FILENAME = "grades.csv";

    // Messages
    private final String PHRASE_PANEL_CHANGE_MESSAGE = "phrasePanelChange";
    private final String OLD_VALUE_DUMMY_MESSAGE = "oldValue";
    private final String ASSIGNMENT_MESSAGE = "assignment";
    private final String CREATED_MESSAGE = "created";
    private final String DOC_VIEW_CHANGE_MESSAGE = "docViewChange";
    private final String ERROR_MESSAGE = "error";
    private final String INSERT_PHRASE_MESSAGE = "insertPhrase";
    private final String NEW_PHRASE_MESSAGE = "newPhrase";
    private final String NEW_CUSTOM_PHRASE_MESSAGE = "newCustomPhrase";
    private final String UPDATE_PHRASE_COUNTER_MESSAGE = "updatePhraseCounter";
    private final String DELETE_PHRASE_MESSAGE = "deletePhrase";
    private final String RESET_PHRASES_PANEL_MESSAGE = "resetPhrasesPanel";
    private final String RESET_FEEDBACK_BOXES_MESSAGE = "resetFeedbackBoxes";

    // Instance variables
    private Assignment assignment;
    private StudentId currentStudentId;
    private StudentId lastStudentId;
    private String currentHeadingBeingEdited;
    private String previousHeadingBeingEdited;
    private Map<String, List<Phrase>> currentHeadingAndUsedPhrases;
    private Map<String, List<Phrase>> previousHeadingAndUsedPhrases;
    private PropertyChangeSupport subscribers;

    /**
     * Constructor.
     */
    public AppModel() {
        this.subscribers = new PropertyChangeSupport(this);
        this.currentHeadingAndUsedPhrases = new HashMap<String, List<Phrase>>();
        this.previousHeadingAndUsedPhrases = new HashMap<String, List<Phrase>>();
    }

    /* SUBSCRIBER METHODS */

    /**
     * Allow an observer to subscribe for changes to the model.
     *
     * @param listener The observer that is subscribing to the model's changes.
     */
    public void subscribe(PropertyChangeListener listener) {
        subscribers.addPropertyChangeListener(listener);
    }

    /**
     * Send a property change notification to subscribers.
     *
     * @param property     The message indicating the change.
     * @param notification The new String value of the thing that has changed.
     */
    public void notifySubscribers(String property, String notification) {
        subscribers.firePropertyChange(property, OLD_VALUE_DUMMY_MESSAGE, notification);
    }

    /**
     * Send a property change notification to subscribers.
     *
     * @param property     The message indicating the change.
     * @param notification The new Object value of the thing that has changed.
     */
    public void notifySubscribers(String property, Object notification) {
        subscribers.firePropertyChange(property, OLD_VALUE_DUMMY_MESSAGE, notification);
    }

    /**
     * Send a property change notification to subscribers.
     *
     * @param property     The message indicating the change.
     * @param oldNotification The old Object value of the thing that changed.
     * @param notification The new Object value of the thing that has changed.
     */
    public void notifySubscribers(String property, Object oldNotification, Object notification) {
        subscribers.firePropertyChange(property, oldNotification, notification);
    }

    /* ASSIGNMENT METHODS */

    /**
     * Create an assignment.
     *
     * @param assignmentTitle         The title of the assignment.
     * @param assignmentHeadings      The headings of the feedback document.
     * @param studentListFile     The student list file.
     * @param assignmentDirectory The directory location to save assignment related documents.
     * @return - The Assignment object that was created.
     */
    public Assignment createAssignment(
        String assignmentTitle,
        String assignmentHeadings,
        Path studentListFile,
        Path assignmentDirectory
    ) throws NotDirectoryException, IOException {
        // create assignment object
        Assignment assignment = new Assignment();
        assignment.setTitle(assignmentTitle);
        assignment.setAssignmentHeadings(assignmentHeadings);
        assignment.setStudentIds(studentListFile, assignmentDirectory);
        assignment.setDirectory(assignmentDirectory);

        // Create the assignment directory if it does not exist
        if (!Files.exists(assignmentDirectory)) {
            Files.createDirectories(assignmentDirectory);
        }

        // Cancel if it already exists and isn't a directory
        if (!Files.isDirectory(assignmentDirectory)) {
            throw new NotDirectoryException(assignmentDirectory.toString());
        }

        // Once an assignment is created, notify the observers
        notifySubscribers(ASSIGNMENT_MESSAGE, CREATED_MESSAGE);
        this.assignment = assignment;
        assignment.setModel(this);
        return this.assignment;
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
        this.assignment.setHeadingStyle(headingStyle);
        this.assignment.setUnderlineStyle(underlineStyle);
        this.assignment.setLineSpacing(lineSpacing);
        this.assignment.setLineMarker(lineMarker);
    }

    /**
     * Load an assignment from an FHT file.
     *
     * @param fhtFile Path to the assignment's FHT file.
     * @return The Assignment object for the assignment.
     */
    public Assignment loadAssignment(Path fhtFile) {
        this.assignment = Assignment.load(fhtFile);
        this.assignment.setModel(this);
        return this.assignment;
    }

    /**
     * Save the assignment to disk.
     *
     * It may be unnecessary to pass the assignment as a parameter, since it
     * should be equal to this.assignment, but this works for now. Importantly,
     * this allows us to handle any IOException using the model's notification
     * system.
     */
    public void saveAssignment(Assignment assignment) {
        try {
            assignment.save();
        } catch (IOException e) {
            notifySubscribers(ERROR_MESSAGE, "Error saving assignment: " + e.getMessage());
        }
    }

    /**
     * Get the line marker to use for denoting new lines.
     *
     * @return The line marker.
     */
    public String getLineMarker() {
        return assignment.getLineMarker();
    }

    /**
     * Get the heading style to use for headings when files are exported.
     *
     * @return The heading style.
     */
    public String getHeadingStyle() {
        return assignment.getHeadingStyle();
    }

    /**
     * Get the heading underline style to use for headings when files are exported.
     *
     * @return The heading underline style.
     */
    public String getUnderlineStyle() {
        return assignment.getUnderlineStyle();
    }

    /**
     * Get the number of line spaces to use between sections when files are exported.
     *
     * @return The number of line spaces.
     */
    public int getLineSpacing() {
        return assignment.getLineSpacing();
    }

    /* FEEDBACK DOCUMENT METHODS */

    /**
     * Get the last document ID that was edited.
     *
     * @return The last document's ID.
     */
    public StudentId getLastDocumentInView() {
        return this.lastStudentId;
    }

    /**
     * Get the document ID of the current document being edited.
     *
     * @return The current document's ID.
     */
    public StudentId getCurrentDocumentInView() {
        return this.currentStudentId;
    }

    /**
     * Update the model with the current ID of the document that is being edited.
     *
     * @param studentId The current document's ID.
     */
    public void setCurrentDocumentInView(StudentId studentId, boolean changeDoc) {
        this.lastStudentId = this.currentStudentId;
        this.currentStudentId = studentId;
        if (changeDoc) {
            notifySubscribers(DOC_VIEW_CHANGE_MESSAGE, studentId);
        }
    }

    /* HEADING MANAGEMENT METHODS */

    /**
     * Get the current feedback box heading being edited.
     *
     * @return The current heading being edited.
     */
    public String getCurrentHeadingBeingEdited() {
        return this.currentHeadingBeingEdited;
    }

    /**
     * Set the current feedback box heading being edited.
     *
     * @param currentHeadingBeingEdited The current heading being edited.
     */
    public void setCurrentHeadingBeingEdited(String currentHeadingBeingEdited) {
        this.previousHeadingBeingEdited = this.currentHeadingBeingEdited;
        this.currentHeadingBeingEdited = currentHeadingBeingEdited;
    }

    /**
     * Get the previous feedback box heading that was edited.
     *
     * @return The previous heading that was edited.
     */
    public String getPreviousHeadingBeingEdited() {
        return this.previousHeadingBeingEdited;
    }

    /** Change one of the headings to a new string. */
    public void editHeading(String previousHeading, String newHeading) throws IllegalArgumentException {
        this.assignment.editHeading(previousHeading, newHeading);
        notifySubscribers("editHeading", previousHeading, newHeading);
        saveAssignment(this.assignment);
    }

    /* USER EXPORTS AND OPERATIONS */

    /**
     * Export the feedback documents.
     *
     * @param assignment The assignment the feedback documents belong to.
     */
    public void exportFeedbackDocuments(Assignment assignment) {
        // Write out each feedback document as a text file
        try {
            Path outputDirectory = assignment.createFeedbackOutputDirectory();
            for (FeedbackDocument document : assignment.getFeedbackDocuments()) {
                document.export(outputDirectory);
            }
        } catch (IOException e) {
            notifySubscribers(ERROR_MESSAGE, "Error writing feedback documents: " + e.getMessage());
        }
    }

    /**
     * Export the grades of an assignment as a text file.
     *
     * @param assignment The assignment grades to export.
     */
    public void exportGrades(Assignment assignment) {
        // Write out the student ids and grades, one per line
        try {
            Path outputDirectory = assignment.createFeedbackOutputDirectory();
            Path gradesFile = outputDirectory.resolve(GRADES_FILENAME);
            try (BufferedWriter writer = Files.newBufferedWriter(gradesFile)) {
                for (FeedbackDocument document : assignment.getFeedbackDocuments()) {
                    writer.write(document.getStudentId() + "," + document.getGrade());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            notifySubscribers(ERROR_MESSAGE, "Error during grade export: " + e.getMessage());
        }
    }

    /**
     * Create an ordered list of grades.
     *
     * @param assignment The assignment the grades are for.
     * @return A list of grades.
     */
    public List<Integer> getGrades(Assignment assignment) {
        // Create a linked hashmap of grades and their counts
        Map<Double, Integer> gradeAndNumber = new LinkedHashMap<Double, Integer>();
        for (double i = 0.0; i <= 20.0; i += 0.5) {
            gradeAndNumber.put(i, 0);
        }

        // Count the number of students that got each grade
        assignment
            .getFeedbackDocuments()
            .forEach(feedbackDocument -> {
                // Round the grade to the nearest 0.5
                double grade = feedbackDocument.getGrade();
                grade = Math.round(grade * 2) / 2.0;
                int currentCount = gradeAndNumber.get(grade);
                gradeAndNumber.put(grade, currentCount + 1);
            });

        // Return order list of grades
        return new ArrayList<>(gradeAndNumber.values());
    }

    public void updateFeedback(StudentId studentId, Map<String, String> headingsAndData) {
        assignment.updateFeedback(studentId, headingsAndData);
    }

    /* PHRASE MANAGEMENT METHODS */

    /**
     * Insert a phrase into the current feedback box being edited.
     *
     * @param phrase The string representation of the phrase to be inserted.
     */
    public void insertPhraseIntoCurrentFeedbackBox(String phrase) {
        notifySubscribers(INSERT_PHRASE_MESSAGE, phrase);
    }

    /**
     * Add new phrase to the view.
     *
     * @param heading The heading this phrase is under, currently ignored.
     * @param phrase The phrase to add.
     */
    public void addNewPhraseToView(String heading, Phrase phrase) {
        notifySubscribers(NEW_PHRASE_MESSAGE, phrase);
    }

    /**
     * Add new custom phrase to the view.
     *
     * @param heading The heading this phrase is under, currently ignored.
     * @param phrase The phrase to add.
     */
    public void addNewCustomPhraseToView(String heading, String phrase) {
        notifySubscribers(NEW_CUSTOM_PHRASE_MESSAGE, phrase);
    }

    /**
     * Add new custom phrase.
     *
     * @param phrase The custom phrase to add.
     */
    public void addNewCustomPhrase(String heading, String phrase) {
        assignment.addCustomPhrase(heading, phrase);
    }

    /** Show all the custom phrases for this heading in the view. */
    public void showCustomPhrases(String heading) {
        assignment.getCustomPhrases(heading).forEach(phrase -> addNewCustomPhraseToView(heading, phrase));
    }

    /**
     * Update the counter on a phrase.
     *
     * @param heading The heading this phrase is under, currently ignored.
     * @param phrase The phrase to update.
     */
    public void updatePhraseCounterInView(String heading, Phrase phrase) {
        notifySubscribers(UPDATE_PHRASE_COUNTER_MESSAGE, phrase);
    }

    /**
     * Remove a phrase.
     *
     * @param heading The heading this phrase is under, currently ignored.
     * @param phrase The phrase to remove.
     */
    public void removePhraseFromView(String heading, Phrase phrase) {
        notifySubscribers(DELETE_PHRASE_MESSAGE, phrase);
    }

    /** Reset the custom phrases panel. */
    public void resetCustomPhrasesPanel() {
        resetPhrasesPanel(PhraseType.CUSTOM);
    }
    
    /**
     * Reset the phrases panel.
     */
    public void resetPhrasesPanel(PhraseType phraseType) {
        notifySubscribers(RESET_PHRASES_PANEL_MESSAGE, phraseType);
    }

    /** Reset the feedback boxes (after an attempted heading update). */
    public void resetFeedbackBoxes() {
        notifySubscribers(RESET_FEEDBACK_BOXES_MESSAGE, this.assignment.getHeadings());
    }

    /**
     * Get the list of phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @return A list of phrases for the given heading.
     */
    public List<Phrase> getCurrentPhraseSet(String heading) {
        return this.currentHeadingAndUsedPhrases.get(heading);
    }

    /**
     * Set the list of phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @param phrases A list of phrases for the given heading.
     */
    public void setCurrentHeadingPhraseSet(String heading, List<Phrase> phrases) {
        this.currentHeadingAndUsedPhrases.put(heading, phrases);
    }

    /**
     * Set the list of phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @param phrases A list of phrases for the given heading.
     */
    public void setPreviousHeadingPhraseSet(String heading, List<Phrase> phrases) {
        this.previousHeadingAndUsedPhrases.put(heading, phrases);
    }
}
