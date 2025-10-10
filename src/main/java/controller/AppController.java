package controller;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import database.DocumentDatabaseManager;
import database.GraphDatabaseManager;
import database.IDocumentDatabase;
import database.IGraphDatabase;
import model.Assignment;
import model.FeedbackDocument;
import model.IAppModel;
import model.LinkedPhrases;
import model.Phrase;
import model.Utilities;
import view.PhraseType;
import visualisation.Visualisations;

/**
 * App Controller Class.
 */
public class AppController implements IAppController {

    // Instance variables
    private final IAppModel appModel;
    private final IDocumentDatabase documentDatabase;
    private final IGraphDatabase graphDatabase;

    /**
     * Constructor.
     *
     * @param appModel - The model to interact with.
     */
    public AppController(IAppModel appModel) {
        this.appModel = appModel;
        this.documentDatabase = new DocumentDatabaseManager();
        this.graphDatabase = new GraphDatabaseManager();
    }

    /**
     * Register as a subscriber of the model.
     *
     * @param propertyChangeListener The listener object to register with the model.
     */
    @Override
    public void registerWithModel(PropertyChangeListener propertyChangeListener) {
        appModel.subscribe(propertyChangeListener);
    }

    /* ASSIGNMENT METHODS */

    /**
     * Create the feedback files for the assignment in the document database.
     *
     * @param assignment              The assignment.
     */
    @Override
    public void createFeedbackDocuments(Assignment assignment) {
        documentDatabase.createFeedbackDocuments(assignment);
    }

    /**
     * Create an assignment in the model.
     *
     * @param assignmentTitle         The title of the assignment.
     * @param headings                The headings of the feedback document.
     * @param studentManifestFile     The student list file.
     * @param assignmentDirectoryPath The directory location to save assignment related documents.
     * @return - The Assignment object that was created.
     */
    @Override
    public Assignment createAssignment(
        String assignmentTitle,
        String headings,
        File studentManifestFile,
        String assignmentDirectoryPath
    ) {
        // Create assignment in the model
        Assignment assignment = appModel.createAssignment(
            assignmentTitle,
            headings,
            studentManifestFile,
            assignmentDirectoryPath
        );
        assignment.saveAssignmentDetails(assignmentTitle.toLowerCase().replace(" ", "-").replace(".db", ""));

        // Create the assignment database
        documentDatabase.createDocumentDatabase(assignment.getFullyQualifiedDatabaseName());

        // Create the feedback files for the assignment in the document database
        documentDatabase.createFeedbackDocuments(assignment);

        // Create the graph database
        graphDatabase.createGraphDatabase(assignment.getFullyQualifiedDatabaseName());
        graphDatabase.setUpGraphDatabaseForAssignment(assignment.getAssignmentHeadings());

        return assignment;
    }

    /**
     * Create an assignment from a configuration file.
     *
     * @param configFilePath The location of the configuration file.
     * @return The newly created Assignment object.
     */
    @Override
    public Assignment createAssignmentFromConfig(String configFilePath) {
        // Create the assignment and save it to an FHT file
        Assignment assignment = appModel.createAssignmentFromConfig(configFilePath);
        assignment.saveAssignmentDetails(
            assignment.getAssignmentTitle().toLowerCase().replace(" ", "-").replace(".db", "")
        );

        // Create the assignment database
        documentDatabase.createDocumentDatabase(assignment.getFullyQualifiedDatabaseName());

        // Create the feedback files for the assignment in the database
        documentDatabase.createFeedbackDocuments(assignment);

        // Setup the graph database
        graphDatabase.createGraphDatabase(assignment.getFullyQualifiedDatabaseName());
        graphDatabase.setUpGraphDatabaseForAssignment(assignment.getAssignmentHeadings());

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
    @Override
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
     * @param assignmentFilePath The location of the assignment FHT file.
     * @return The Assignment object for the assignment.
     */
    @Override
    public Assignment loadAssignment(String assignmentFilePath) {
        // Load the assignment
        Assignment assignment = appModel.loadAssignment(assignmentFilePath);

        // Get the file at the path
        File file = new File(assignmentFilePath);
        String currentDirectory = file.getParent();

        // Check if the assignment working directory matches the current directory
        if (!assignment.getAssignmentDirectoryPath().equals(currentDirectory)) {
            System.out.println("Changing directory to " + currentDirectory);
            // Change working directory to current directory
            assignment.setAssignmentDirectoryPath(currentDirectory);
        }

        // Load the feedback documents into the assignment
        loadFeedbackDocuments(assignment);
        return assignment;
    }

    /**
     * Save an assignment.
     *
     * @param assignment The assignment to save.
     * @param fileName   The file name to save the assignment FHT as.
     */
    @Override
    public void saveAssignment(Assignment assignment, String fileName) {
        assignment.saveAssignmentDetails(fileName);
    }

    /**
     * Get the line marker to use for denoting new lines.
     *
     * @return The line marker.
     */
    @Override
    public String getLineMarker() {
        return appModel.getLineMarker();
    }

    /**
     * Get the heading style to use for headings when files are exported.
     *
     * @return The heading style.
     */
    @Override
    public String getHeadingStyle() {
        return appModel.getHeadingStyle();
    }

    /**
     * Get the heading underline style to use for headings when files are exported.
     *
     * @return The heading underline style.
     */
    @Override
    public String getUnderlineStyle() {
        return appModel.getUnderlineStyle();
    }

    /**
     * Get the number of line spaces to use between sections when files are exported.
     *
     * @return The number of line spaces.
     */
    @Override
    public int getLineSpacing() {
        return appModel.getLineSpacing();
    }

    /* FEEDBACK DOCUMENT METHODS */

    /**
     * Load the feedback documents for an assignment.
     *
     * @param assignment The assignment to load the feedback documents for.
     */
    private void loadFeedbackDocuments(Assignment assignment) {
        // Open the databases
        System.out.println("Loading files " + assignment.getFullyQualifiedDatabaseName() + ".*");
        documentDatabase.openDocumentDatabase(assignment.getFullyQualifiedDatabaseName());
        graphDatabase.openGraphDatabase(assignment.getFullyQualifiedDatabaseName());

        // Get the feedback documents from the document database
        List<FeedbackDocument> feedbackDocuments = documentDatabase.loadFeedbackDocumentsForAssignment(assignment);
        assignment.setFeedbackDocuments(feedbackDocuments);

        // Get the phrases data for each heading from the graph database
        assignment
            .getAssignmentHeadings()
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
    @Override
    public void saveFeedbackDocument(String studentId) {
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
    @Override
    public void saveFeedbackDocument(
        Assignment assignment,
        String studentId,
        Map<String, String> headingsAndData,
        double grade
    ) {
        documentDatabase.saveFeedbackDocument(assignment, studentId, headingsAndData, grade);
        FeedbackDocument feedbackDocumentForStudent = assignment.getFeedbackDocumentForStudent(studentId);
        assignment
            .getAssignmentHeadings()
            .forEach(heading -> {
                feedbackDocumentForStudent.setDataForHeading(heading, headingsAndData.get(heading));
            });
        feedbackDocumentForStudent.setGrade(grade);
    }

    /**
     * Get the last document ID that was edited.
     *
     * @return The last document's ID.
     */
    @Override
    public String getLastDocumentInView() {
        return appModel.getLastDocumentInView();
    }

    /**
     * Get the document ID of the current document being edited.
     *
     * @return The current document's ID.
     */
    @Override
    public String getCurrentDocumentInView() {
        return appModel.getCurrentDocumentInView();
    }

    /**
     * Update the model with the current ID of the document that is being edited.
     *
     * @param studentId The current document's ID.
     */
    @Override
    public void setCurrentDocumentInView(String studentId) {
        appModel.setCurrentDocumentInView(studentId, false);
    }

    /**
     * Process a request to display a new document to edit.
     *
     * @param assignment The assignment the document belongs to.
     * @param studentId  The ID of the document to display.
     */
    @Override
    public void displayNewDocument(Assignment assignment, String studentId) {
        // Get the latest data for the requested document
        documentDatabase.updateFeedbackDocument(assignment, studentId);
        appModel.setCurrentDocumentInView(studentId, true);
    }

    /**
     * Get the first line of a feedback document.
     *
     * @param assignment The assignment the document belongs to.
     * @param studentId  The student id of the document.
     * @return The first line of the document if it exists or a default message.
     */
    @Override
    public String getFirstLineFromDocument(Assignment assignment, String studentId) {
        // Set the default text
        String returnString = "<no preview available>";

        // Get the student's feedback document
        FeedbackDocument feedbackDocumentForStudent = assignment.getFeedbackDocumentForStudent(studentId);

        // Find the first line of the document
        for (String heading : assignment.getAssignmentHeadings()) {
            if (!feedbackDocumentForStudent.getHeadingData(heading).isEmpty()) {
                List<String> dataAsList = Arrays.stream(feedbackDocumentForStudent.getHeadingData(heading).split("\n"))
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
    @Override
    public void updateCurrentHeadingBeingEdited(String currentHeadingBeingEdited) {
        appModel.setCurrentHeadingBeingEdited(currentHeadingBeingEdited);
    }

    /**
     * Get the current feedback box heading being edited.
     *
     * @return The current heading being edited.
     */
    @Override
    public String getCurrentHeadingBeingEdited() {
        return appModel.getCurrentHeadingBeingEdited();
    }

    /**
     * Determine whether the user has navigated to a new feedback box.
     *
     * @return True if a new heading is being edited, false otherwise.
     */
    @Override
    public boolean headingChanged() {
        return !appModel.getCurrentHeadingBeingEdited().equals(appModel.getPreviousHeadingBeingEdited());
    }

    /**
     * Change the current feedback box heading.
     *
     * @param previousHeading           The current feedback box heading being edited.
     * @param currentHeading            The new feedback box heading
     */
    @Override
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
    @Override
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
    @Override
    public void exportFeedbackDocuments(Assignment assignment) {
        // Make sure feedback documents are updated
        documentDatabase.loadFeedbackDocumentsForAssignment(assignment);
        // Export them
        appModel.exportFeedbackDocuments(assignment);
    }

    /**
     * Export the grade document.
     *
     * @param assignment The assignment the grade document belongs to.
     */
    @Override
    public void exportGrades(Assignment assignment) {
        // Make sure feedback documents are updated
        documentDatabase.loadFeedbackDocumentsForAssignment(assignment);
        // Export them
        appModel.exportGrades(assignment);
    }

    /**
     * Create a bar chart visualisation of the grades.
     *
     * @param assignment The assignment grades to visualise.
     */
    @Override
    public void visualiseGrades(Assignment assignment) {
        // Make sure feedback documents are updated
        documentDatabase.loadFeedbackDocumentsForAssignment(assignment);

        // Extract the grades and visualise them
        List<Integer> grades = appModel.getGrades(assignment);
        Visualisations.createBarChart(grades);
    }

    /**
     * Get a summary of all the feedback documents.
     *
     * @param assignment The assignment to summarise.
     * @return A map of headings and the 3 most used phrases for those headings.
     */
    @Override
    public Map<String, List<String>> getSummary(Assignment assignment) {
        Map<String, List<String>> summary = new HashMap<String, List<String>>();

        // Get the 3 most used phrases for a given heading
        assignment
            .getAssignmentHeadings()
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
    @Override
    public void insertPhraseIntoCurrentFeedbackBox(String phrase) {
        appModel.insertPhraseIntoCurrentFeedbackBox(phrase);
    }

    /**
     * Show all the frequently used phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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

        // Update insights panel
        //resetPhrasesPanel(PhraseType.INSIGHTS);
        //showInsights();
        // This gets very expensive on big data sets, so we don't do it here.
        // Now we only do it when switching to the Insights window.
    }

    /**
     * Ask the model to reset the phrases panel.
     */
    @Override
    public void resetPhrasesPanel(PhraseType phrasePanel) {
        appModel.resetPhrasesPanel(phrasePanel);
    }

    /**
     * Get the custom phrases and display them.
     */
    @Override
    public void showCustomPhrases() {
        List<Phrase> customPhrases = graphDatabase.getCustomPhrases();
        customPhrases.forEach(appModel::addNewCustomPhraseToView);
    }

    /**
     * Get the custom phrases and display them.
     */
    @Override
    public void showInsights() {
        List<LinkedPhrases> linkedPhrases = graphDatabase.getLinkedPhrases(getCurrentHeadingBeingEdited());
        linkedPhrases.forEach(appModel::addNewLinkedPhrasesToView);
    }

    /**
     * Set the phrase panel the user is currently viewing.
     *
     * @param currentPhrasePanelInView The phrase panel type.
     */
    @Override
    public void setCurrentPhrasePanelInView(PhraseType currentPhrasePanelInView) {
        appModel.setCurrentPhrasePanelInView(currentPhrasePanelInView);
    }

    /**
     * Show the user an error message.
     *
     * @param errorMessage The error message to show.
     */
    @Override
    public void error(String errorMessage) {
        appModel.notifySubscribers("error", errorMessage);
    }
}
