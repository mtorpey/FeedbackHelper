package controller;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import configuration.UserPreferences;
import database.GraphDatabaseManager;
import model.AppModel;
import model.Assignment;
import model.FeedbackDocument;
import model.Phrase;
import model.StudentId;
import model.Utilities;
import view.PhraseType;
import visualisation.Visualisations;

/**
 * App Controller Class.
 */
public class AppController {

    // Instance variables
    private final AppModel appModel;
    private final GraphDatabaseManager graphDatabase;

    /**
     * Constructor.
     *
     * @param appModel - The model to interact with.
     */
    public AppController(AppModel appModel) {
        this.appModel = appModel;
        this.graphDatabase = new GraphDatabaseManager();
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
     * Create an assignment in the model.
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
        assignment.saveAssignmentDetails();

        // Create the graph database
        graphDatabase.createGraphDatabase(assignment.getDirectory(), assignment.getFileSafeTitle());
        graphDatabase.setUpGraphDatabaseForAssignment(assignment.getHeadings());

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

        // Get the file at the path
        Path currentDirectory = fhtFile.getParent().toAbsolutePath();

        // The assignment may have moved since being saved, so its directory is not serialised
        assignment.setDirectory(currentDirectory);

        // Since this was successful, remember it as the default for next load
        UserPreferences.setLastOpenedAssignment(fhtFile);

        // Load the feedback documents into the assignment
        loadPhrasesFromFile(assignment);
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

    /**
     * Load the feedback documents for an assignment.
     *
     * @param assignment The assignment to load the feedback documents for.
     */
    private void loadPhrasesFromFile(Assignment assignment) {
        // Open the databases
        graphDatabase.openGraphDatabase(assignment.getDirectory(), assignment.getFileSafeTitle());

        // Get the phrases data for each heading from the graph database
        assignment
            .getHeadings()
            .forEach(heading -> {
                List<Phrase> phrasesForHeading = graphDatabase.getPhrasesForHeading(heading);
                appModel.setPreviousHeadingPhraseSet(heading, phrasesForHeading);
                appModel.setCurrentHeadingPhraseSet(heading, phrasesForHeading);
            });
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
        FeedbackDocument feedbackDocumentForStudent = assignment.getFeedbackDocumentForStudent(studentId);
        assignment
            .getHeadings()
            .forEach(heading -> {
                feedbackDocumentForStudent.setDataForHeading(heading, headingsAndData.get(heading));
            });
        feedbackDocumentForStudent.setGrade(grade);
        try {
            assignment.saveAssignmentDetails();
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
        FeedbackDocument feedbackDocumentForStudent = assignment.getFeedbackDocumentForStudent(studentId);

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
    public void checkHeading(String previousHeading, String newHeading) {
        newHeading = newHeading.replaceAll("\n", "").trim(); // Remove all new lines

        // Change to the new heading
        appModel.notifySubscribers("changeHeading", previousHeading, newHeading);
    }

    /**
     * Change the current feedback box heading.
     *
     * @param previousHeading           The current feedback box heading being edited.
     * @param currentHeading            The new feedback box heading
     */
    public void updateHeading(String previousHeading, String newHeading) {
        // Add new heading
        graphDatabase.addHeadingObject(newHeading);

        // Move phrases from old heading
        List<Phrase> currentPhrases = graphDatabase.getPhrasesForHeading(previousHeading);
        currentPhrases.forEach(phraseToAdd -> {
            graphDatabase.updatePhrase(newHeading, phraseToAdd);
        });

        // Remove the old heading
        graphDatabase.removeHeadingObject(previousHeading);
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

    /**
     * Get a summary of all the feedback documents.
     *
     * @param assignment The assignment to summarise.
     * @return A map of headings and the 3 most used phrases for those headings.
     */
    public Map<String, List<String>> getSummary(Assignment assignment) {
        Map<String, List<String>> summary = new HashMap<String, List<String>>();

        // Get the 3 most used phrases for a given heading
        assignment
            .getHeadings()
            .forEach(heading -> {
                summary.put(heading, new ArrayList<>());

                // Get the phrases
                List<Phrase> phrasesForHeading = graphDatabase.getPhrasesForHeading(heading);
                Collections.sort(phrasesForHeading);

                // Only store phrases if there are 3 or more
                if (phrasesForHeading.size() >= 3) {
                    List<String> phrases = new ArrayList<String>();
                    for (int i = 0; i < 3; i++) {
                        phrases.add(phrasesForHeading.get(i).getPhraseAsString());
                    }
                    summary.put(heading, phrases);
                }
            });

        return summary;
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
            currentPhraseSet.forEach(appModel::addNewPhraseToView);
        }
    }

    /**
     * Add a custom phrase from the GUI.
     *
     * @param phrase The string representation of the phrase to be added.
     */
    public void addNewCustomPhraseFromView(String phrase) {
        // Filter out empty lines
        if (!phrase.trim().isEmpty() && !phrase.trim().equals(getLineMarker())) {
            Phrase phrase1 = new Phrase(phrase);
            graphDatabase.addPhraseToCustomNode(phrase1);
            appModel.addNewCustomPhraseToView(phrase1);
        }
    }

    /**
     * Manage the links between phrases.
     *
     * @param heading             The current feedback box heading being edited.
     * @param previousBoxContents The last list of phrases for the feedback box.
     * @param currentBoxContents  The current list of phrases for the feedback box.
     */
    public void managePhraseLinks(String heading, List<String> previousBoxContents, List<String> currentBoxContents) {
        graphDatabase.managePhraseLinks(heading, previousBoxContents, currentBoxContents);
    }

    /**
     * Update the phrases stored in the graph database.
     *
     * @param heading             The current feedback box heading being edited.
     * @param previousBoxContents The last list of phrases for the feedback box.
     * @param currentBoxContents  The current list of phrases for the feedback box.
     */
    public void updatePhrases(String heading, List<String> previousBoxContents, List<String> currentBoxContents) {
        // Store previous phrase set
        List<Phrase> previousPhrasesForHeading = graphDatabase.getPhrasesForHeading(heading);
        appModel.setPreviousHeadingPhraseSet(heading, previousPhrasesForHeading);

        // Update the database with the new phrases
        graphDatabase.updatePhrasesForHeading(heading, previousBoxContents, currentBoxContents);
        List<Phrase> currentPhrasesForHeading = graphDatabase.getPhrasesForHeading(heading);
        appModel.setCurrentHeadingPhraseSet(heading, currentPhrasesForHeading);

        // Find what's changed and send those changes to GUI
        List<Phrase> removalsFromList = Utilities.getRemovalsFromList(
            previousPhrasesForHeading,
            currentPhrasesForHeading
        );
        List<Phrase> additionsToList = Utilities.getAdditionsToList(
            previousPhrasesForHeading,
            currentPhrasesForHeading
        );

        // Find what's stayed same and update the usage counts
        List<Phrase> stayedSameList = Utilities.getIntersection(previousPhrasesForHeading, currentPhrasesForHeading);

        // Perform updates
        removalsFromList.forEach(appModel::removePhraseFromView);
        additionsToList.forEach(appModel::addNewPhraseToView);
        stayedSameList.forEach(appModel::updatePhraseCounterInView); // takes some time

        // Update custom panel
        resetPhrasesPanel(PhraseType.CUSTOM);
        showCustomPhrases(); // takes a long time on startup for big sets
    }

    /**
     * Ask the model to reset the phrases panel.
     */
    public void resetPhrasesPanel(PhraseType phrasePanel) {
        appModel.resetPhrasesPanel(phrasePanel);
    }

    /**
     * Get the custom phrases and display them.
     */
    public void showCustomPhrases() {
        List<Phrase> customPhrases = graphDatabase.getCustomPhrases();
        customPhrases.forEach(appModel::addNewCustomPhraseToView);
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
