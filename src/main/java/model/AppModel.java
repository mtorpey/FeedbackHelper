package model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import view.PhraseType;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * App Model Class.
 */
public class AppModel implements IAppModel {

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
    private final String NEW_LINKED_PHRASES_MESSAGE = "newLinkedPhrases";
    private final String UPDATE_PHRASE_COUNTER_MESSAGE = "updatePhraseCounter";
    private final String DELETE_PHRASE_MESSAGE = "deletePhrase";
    private final String RESET_PHRASES_PANEL_MESSAGE = "resetPhrasesPanel";

    // Instance variables
    private Assignment assignment;
    private String currentStudentId;
    private String lastStudentId;
    private String currentHeadingBeingEdited;
    private String previousHeadingBeingEdited;
    private Map<String, List<Phrase>> currentHeadingAndUsedPhrases;
    private Map<String, List<Phrase>> previousHeadingAndUsedPhrases;
    private PhraseType currentPhrasePanelInView;
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
    @Override
    public void subscribe(PropertyChangeListener listener) {
        subscribers.addPropertyChangeListener(listener);
    }

    /**
     * Send a property change notification to subscribers.
     *
     * @param property     The message indicating the change.
     * @param notification The new String value of the thing that has changed.
     */
    @Override
    public void notifySubscribers(String property, String notification) {
        subscribers.firePropertyChange(property, OLD_VALUE_DUMMY_MESSAGE, notification);
    }

    /**
     * Send a property change notification to subscribers.
     *
     * @param property     The message indicating the change.
     * @param notification The new Object value of the thing that has changed.
     */
    @Override
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
    @Override
    public void notifySubscribers(String property, Object oldNotification, Object notification) {
        subscribers.firePropertyChange(property, oldNotification, notification);
    }

    /* ASSIGNMENT METHODS */

    /**
     * Create an assignment.
     *
     * @param assignmentTitle         The title of the assignment.
     * @param assignmentHeadings      The headings of the feedback document.
     * @param studentManifestFile     The student list file.
     * @param assignmentDirectoryPath The directory location to save assignment related documents.
     * @return - The Assignment object that was created.
     */
    @Override
    public Assignment createAssignment(String assignmentTitle, String assignmentHeadings, File studentManifestFile, String assignmentDirectoryPath) {
        // Create assignment object
        Assignment assignment = new Assignment();
        assignment.setAssignmentTitle(assignmentTitle);
        assignment.setAssignmentHeadings(assignmentHeadings);
        assignment.setStudentIds(studentManifestFile, assignmentDirectoryPath);
        assignment.setAssignmentDirectoryPath(assignmentDirectoryPath);

        // Create the assignment directory if it does not exist
        File outputDirectory = new File(assignmentDirectoryPath);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        // Once an assignment is created, notify the observers
        notifySubscribers(ASSIGNMENT_MESSAGE, CREATED_MESSAGE);
        this.assignment = assignment;
        return this.assignment;
    }

    /**
     * Create an assignment from a configuration file.
     *
     * @param configFilePath The location of the configuration file.
     * @return The newly created Assignment object.
     */
    @Override
    public Assignment createAssignmentFromConfig(String configFilePath) {
        try {
            // Load the assignment file as a JSON object
            JSONObject configDoc = (JSONObject) new JSONParser().parse(new FileReader(configFilePath));

            // Extract configuration options
            String title = (String) configDoc.get("title");
            String assignmentLocation = (String) configDoc.get("assignment_location");
            String lineMarker = (String) configDoc.get("line_marker");
            JSONArray headings = (JSONArray) configDoc.get("headings");

            // Build a string of headings
            StringBuilder headingsString = new StringBuilder();
            headings.forEach(heading -> {
                headingsString.append(heading);
                headingsString.append("\n");
            });

            // Extract styling options
            Map headingStyle = ((Map) configDoc.get("heading_style"));
            String headingMarker = (String) headingStyle.get("heading_marker");
            String headingUnderlineStyle = (String) headingStyle.get("heading_underline_style");
            long numLinesAfterSectionEnds = (long) headingStyle.get("num_lines_after_section_ends");

            String studentManifestFileLocation = (String) configDoc.get("student_manifest_location");
            File studentManifestFile = new File(studentManifestFileLocation);

            // Create the assignment and set style preferences
            Assignment assignment = createAssignment(title, headingsString.toString(), studentManifestFile, assignmentLocation);
            setAssignmentPreferences(headingMarker, headingUnderlineStyle, (int) numLinesAfterSectionEnds, lineMarker);
            this.assignment = assignment;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

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
    @Override
    public void setAssignmentPreferences(String headingStyle, String underlineStyle, int lineSpacing, String lineMarker) {
        this.assignment.setHeadingStyle(headingStyle);
        this.assignment.setUnderlineStyle(underlineStyle);
        this.assignment.setLineSpacing(lineSpacing);
        this.assignment.setLineMarker(lineMarker);
    }

    /**
     * Load an assignment from an FHT file.
     *
     * @param assignmentFilePath The location of the assignment FHT file.
     * @return The Assignment object for the assignment.
     */
    @Override
    public Assignment loadAssignment(String assignmentFilePath) {
        this.assignment = Assignment.loadAssignment(assignmentFilePath);
        return this.assignment;
    }

    /**
     * Get the line marker to use for denoting new lines.
     *
     * @return The line marker.
     */
    @Override
    public String getLineMarker() {
        return assignment.getLineMarker();
    }

    /**
     * Get the heading style to use for headings when files are exported.
     *
     * @return The heading style.
     */
    @Override
    public String getHeadingStyle() {
        return assignment.getHeadingStyle();
    }

    /**
     * Get the heading underline style to use for headings when files are exported.
     *
     * @return The heading underline style.
     */
    @Override
    public String getUnderlineStyle() {
        return assignment.getUnderlineStyle();
    }

    /**
     * Get the number of line spaces to use between sections when files are exported.
     *
     * @return The number of line spaces.
     */
    @Override
    public int getLineSpacing() {
        return assignment.getLineSpacing();
    }


    /* FEEDBACK DOCUMENT METHODS */

    /**
     * Get the last document ID that was edited.
     *
     * @return The last document's ID.
     */
    @Override
    public String getLastDocumentInView() {
        return this.lastStudentId;
    }

    /**
     * Get the document ID of the current document being edited.
     *
     * @return The current document's ID.
     */
    @Override
    public String getCurrentDocumentInView() {
        return this.currentStudentId;
    }

    /**
     * Update the model with the current ID of the document that is being edited.
     *
     * @param studentId The current document's ID.
     */
    @Override
    public void setCurrentDocumentInView(String studentId, boolean changeDoc) {
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
    @Override
    public String getCurrentHeadingBeingEdited() {
        return this.currentHeadingBeingEdited;
    }

    /**
     * Set the current feedback box heading being edited.
     *
     * @param currentHeadingBeingEdited The current heading being edited.
     */
    @Override
    public void setCurrentHeadingBeingEdited(String currentHeadingBeingEdited) {
        this.previousHeadingBeingEdited = this.currentHeadingBeingEdited;
        this.currentHeadingBeingEdited = currentHeadingBeingEdited;
    }

    /**
     * Get the previous feedback box heading that was edited.
     *
     * @return The previous heading that was edited.
     */
    @Override
    public String getPreviousHeadingBeingEdited() {
        return this.previousHeadingBeingEdited;
    }


    /* USER EXPORTS AND OPERATIONS */

    /**
     * Export the feedback documents.
     *
     * @param assignment The assignment the feedback documents belong to.
     */
    @Override
    public void exportFeedbackDocuments(Assignment assignment) {
        // Create the output directory if it does not exist
        File outputDirectory = new File(assignment.getAssignmentDirectoryPath() + File.separator + assignment.getAssignmentTitle().trim().replace(" ", "-"));
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        // Write out each feedback document as a text file
        assignment.getFeedbackDocuments().forEach(feedbackDocument -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputDirectory + File.separator + feedbackDocument.getStudentId() + ".txt"))) {
                for (String heading : assignment.getAssignmentHeadings()) {
                    // Heading
                    writer.write(assignment.getHeadingStyle() + heading);
                    writer.newLine();

                    // Underline heading if required
                    String underlineStyle = assignment.getUnderlineStyle();
                    if (!underlineStyle.isEmpty()) {
                        for (int i = 0; i < assignment.getHeadingStyle().length() + heading.length(); i++) {
                            writer.write(underlineStyle);
                        }
                    }

                    // Data
                    writer.newLine();
                    String headingData = feedbackDocument.getHeadingData(heading);
                    List<String> dataAsList = Arrays.stream(headingData.split("\n")).collect(Collectors.toList());
                    for (String line : dataAsList) {
                        if (!line.trim().equals(getLineMarker().trim())) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }

                    // End section spacing
                    for (int i = 0; i < assignment.getLineSpacing(); i++) {
                        writer.newLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                notifySubscribers(ERROR_MESSAGE, "Something went wrong during grade export!");
            }
        });
    }

    /**
     * Export the grades of an assignment as a text file.
     *
     * @param assignment The assignment grades to export.
     */
    @Override
    public void exportGrades(Assignment assignment) {
        // Create the output directory if it does not exist
        File outputDirectory = new File(assignment.getAssignmentDirectoryPath() + File.separator + assignment.getAssignmentTitle().trim().replace(" ", "-"));
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        // Write out the student ids and grades, one per line
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputDirectory + File.separator + GRADES_FILENAME))) {
            for (FeedbackDocument feedbackDocument : assignment.getFeedbackDocuments()) {
                writer.write(feedbackDocument.getStudentId() + "," + feedbackDocument.getGrade());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            notifySubscribers(ERROR_MESSAGE, "Something went wrong during grade export!");
        }
    }

    /**
     * Create an ordered list of grades.
     *
     * @param assignment The assignment the grades are for.
     * @return A list of grades.
     */
    @Override
    public List<Integer> getGrades(Assignment assignment) {
        // Create a linked hashmap of grades and their counts
        Map<Double, Integer> gradeAndNumber = new LinkedHashMap<Double, Integer>();
        for (double i = 0.0; i <= 20.0; i += 0.5) {
            gradeAndNumber.put(i, 0);
        }

        // Count the number of students that got each grade
        assignment.getFeedbackDocuments().forEach(feedbackDocument -> {
            // Round the grade to the nearest 0.5
            // Rounding code adapted from: https://stackoverflow.com/questions/23449662/java-round-to-nearest-5
            double grade = feedbackDocument.getGrade();
            grade = Math.round(grade * 2) / 2.0;
            int currentCount = gradeAndNumber.get(grade);
            gradeAndNumber.put(grade, currentCount + 1);
        });

        // Return order list of grades
        return new ArrayList<>(gradeAndNumber.values());
    }


    /* PHRASE MANAGEMENT METHODS */

    /**
     * Insert a phrase into the current feedback box being edited.
     *
     * @param phrase The string representation of the phrase to be inserted.
     */
    @Override
    public void insertPhraseIntoCurrentFeedbackBox(String phrase) {
        notifySubscribers(INSERT_PHRASE_MESSAGE, phrase);
    }

    /**
     * Add new phrase.
     *
     * @param phrase The phrase to add.
     */
    @Override
    public void addNewPhraseToView(Phrase phrase) {
        notifySubscribers(NEW_PHRASE_MESSAGE, phrase);
    }

    /**
     * Add new custom phrase.
     *
     * @param phrase The custom phrase to add.
     */
    @Override
    public void addNewCustomPhraseToView(Phrase phrase) {
        notifySubscribers(NEW_CUSTOM_PHRASE_MESSAGE, phrase);
    }

    /**
     * Add new linked phrase.
     *
     * @param linkedPhrases The linked phrases to add.
     */
    @Override
    public void addNewLinkedPhrasesToView(LinkedPhrases linkedPhrases) {
        notifySubscribers(NEW_LINKED_PHRASES_MESSAGE, linkedPhrases);
    }

    /**
     * Update the counter on a phrase.
     *
     * @param phrase The phrase to update.
     */
    @Override
    public void updatePhraseCounterInView(Phrase phrase) {
        notifySubscribers(UPDATE_PHRASE_COUNTER_MESSAGE, phrase);
    }

    /**
     * Remove a phrase.
     *
     * @param phrase The phrase to remove.
     */
    @Override
    public void removePhraseFromView(Phrase phrase) {
        notifySubscribers(DELETE_PHRASE_MESSAGE, phrase);
    }

    /**
     * Reset the phrases panel.
     */
    @Override
    public void resetPhrasesPanel(PhraseType phraseType) {
        notifySubscribers(RESET_PHRASES_PANEL_MESSAGE, phraseType);
    }

    /**
     * Get the list of phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @return A list of phrases for the given heading.
     */
    @Override
    public List<Phrase> getCurrentPhraseSet(String heading) {
        return this.currentHeadingAndUsedPhrases.get(heading);
    }

    /**
     * Set the list of phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @param phrases A list of phrases for the given heading.
     */
    @Override
    public void setCurrentHeadingPhraseSet(String heading, List<Phrase> phrases) {
        this.currentHeadingAndUsedPhrases.put(heading, phrases);
    }

    /**
     * Set the list of phrases for a given heading.
     *
     * @param heading The heading the phrases are for.
     * @param phrases A list of phrases for the given heading.
     */
    @Override
    public void setPreviousHeadingPhraseSet(String heading, List<Phrase> phrases) {
        this.previousHeadingAndUsedPhrases.put(heading, phrases);
    }

    /**
     * Set the phrase panel the user is currently viewing.
     *
     * @param currentPhrasePanelInView The phrase panel type.
     */
    @Override
    public void setCurrentPhrasePanelInView(PhraseType currentPhrasePanelInView) {
        this.currentPhrasePanelInView = currentPhrasePanelInView;
        notifySubscribers(PHRASE_PANEL_CHANGE_MESSAGE, currentPhrasePanelInView);
    }

}
















