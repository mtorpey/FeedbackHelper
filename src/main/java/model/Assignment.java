package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Assignment Class.
 */
public class Assignment implements Serializable {

    private static final long serialVersionUID = 1200109309800080100L;

    // Heading styles map
    private static final List<String> headingStyles = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("#");
                add("##");
            }});

    // Heading underline style map
    private final List<String> underlineStyles = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("-");
                add("=");
            }});

    // Line spacing map
    private final List<Integer> lineSpacings = Collections.unmodifiableList(
            new ArrayList<Integer>() {{
                add(1);
                add(2);
                add(3);
            }});

    // Line marker map
    private final List<String> lineMarkers = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("-");
                add("->");
                add("=>");
                add("*");
                add("+");
            }});

    // Instance variables
    private String assignmentTitle;
    private List<String> assignmentHeadings;
    private List<String> studentIds;
    private List<FeedbackDocument> feedbackDocuments;
    private Map<String, FeedbackDocument> studentIdAndFeedbackDocumentMap;
    private String headingStyle;
    private String underlineStyle;
    private int lineSpacing;
    private String lineMarker;
    private String databaseName;
    private String databaseCollectionName;
    private String assignmentDirectoryPath;

    /**
     * Constructor.
     */
    public Assignment() {
        this.studentIds = new ArrayList<String>();
        this.assignmentHeadings = new ArrayList<String>();
        studentIdAndFeedbackDocumentMap = new HashMap<String, FeedbackDocument>();
    }

    /**
     * Load an assignment from an FHT file.
     *
     * @param filePath The location of the FHT file.
     * @return The Assignment object stored in the file.
     */
    public static Assignment loadAssignment(String filePath) {
        Assignment loadedAssignment = null;
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            loadedAssignment = (Assignment) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return loadedAssignment;
    }

    /**
     * Get the heading style to use for headings when files are exported.
     *
     * @return The heading style.
     */
    public String getHeadingStyle() {
        return headingStyle;
    }

    /**
     * Set the heading style to use for headings when files are exported.
     *
     * @param headingStyle The heading style.
     */
    public void setHeadingStyle(String headingStyle) {
        // Check if the heading style is allowed, if its not use a default
        if (headingStyles.contains(headingStyle)) {
            this.headingStyle = headingStyle.trim() + " ";
        } else {
            this.headingStyle = "";
        }
    }

    /**
     * Get the heading underline style to use for headings when files are exported.
     *
     * @return The heading underline style.
     */
    public String getUnderlineStyle() {
        return underlineStyle;
    }

    /**
     * Set the heading underline style to use for headings when files are exported.
     *
     * @param underlineStyle The heading underline style.
     */
    public void setUnderlineStyle(String underlineStyle) {
        // Check if the underline style is allowed, if its not use a default
        if (underlineStyles.contains(underlineStyle)) {
            this.underlineStyle = underlineStyle;
        } else {
            this.underlineStyle = "";
        }
    }

    /**
     * Get the number of line spaces to use between sections when files are exported.
     *
     * @return The number of line spaces.
     */
    public int getLineSpacing() {
        return lineSpacing;
    }

    /**
     * Set the number of line spaces to use between sections when files are exported.
     *
     * @param lineSpacing The number of line spaces.
     */
    public void setLineSpacing(int lineSpacing) {
        // Check if the line spacing style is allowed, if its not use a default
        if (lineSpacings.contains(lineSpacing)) {
            this.lineSpacing = lineSpacing;
        } else {
            this.lineSpacing = 1;
        }
    }

    /**
     * Get the line marker to use for denoting new lines.
     *
     * @return The line marker.
     */
    public String getLineMarker() {
        return lineMarker;
    }

    /**
     * Set the line marker to use for denoting new lines.
     *
     * @param lineMarker The line marker.
     */
    public void setLineMarker(String lineMarker) {
        if (lineMarkers.contains(lineMarker)) {
            this.lineMarker = lineMarker.trim() + " ";
        } else {
            this.lineMarker = "- ";
        }
    }

    /**
     * Get the assignment directory path.
     *
     * Note: this is saved when we serialize, but is not actually used
     * meaningfully when we load an assignment from disk.  We reset it in
     * AppController.loadAssignment if the location has actually moved on disk.
     *
     * @return The assignment directory path.
     */
    public String getAssignmentDirectoryPath() {
        return assignmentDirectoryPath;
    }

    /**
     * Set the assignment directory path.
     *
     * @param assignmentDirectoryPath The assignment directory path.
     */
    public void setAssignmentDirectoryPath(String assignmentDirectoryPath) {
        this.assignmentDirectoryPath = assignmentDirectoryPath;
    }

    /**
     * Get the database collection name used for the assignment's documents.
     *
     * @return The database collection name.
     */
    public String getDatabaseCollectionName() {
        return this.databaseCollectionName;
    }

    /**
     * Store the feedback document for a given student ID.
     *
     * @param studentId        The student ID the feedback document is for.
     * @param feedbackDocument The feedback document for the student.
     */
    public void setFeedbackDocument(String studentId, FeedbackDocument feedbackDocument) {
        this.studentIdAndFeedbackDocumentMap.put(studentId, feedbackDocument);
    }

    /**
     * Get the assignment title.
     *
     * @return The assignment title.
     */
    public String getAssignmentTitle() {
        return this.assignmentTitle;
    }

    /**
     * Set the assignment title and use it to generate the database name and database collection name.
     *
     * @param assignmentTitle The assignment title.
     */
    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
        this.databaseName = assignmentTitle.replace(" ", "-").toLowerCase();  // no extension
        this.databaseCollectionName = assignmentTitle.replace(" ", "-").toLowerCase() + "-feedback-docs";
    }

    /**
     * Get a list of the headings to be used in the feedback documents.
     *
     * @return A list of the headings to be used in the feedback documents.
     */
    public List<String> getAssignmentHeadings() {
        return this.assignmentHeadings;
    }

    /**
     * Set a list of the headings to be used in the feedback documents.
     *
     * @param assignmentHeadings A string containing a list of the headings to be used in the feedback documents, separated by a new line.
     */
    public void setAssignmentHeadings(String assignmentHeadings) {
        this.assignmentHeadings = new ArrayList<String>(Arrays.asList(assignmentHeadings.split("\n")));
        // Remove empty lines
        this.assignmentHeadings = this.assignmentHeadings.stream().filter(heading -> !heading.trim().isEmpty()).collect(Collectors.toList());
    }

    /**
     * Set a list of the student ids.
     *
     * @param studentManifestFile The student list file to read from.
     * @param assignmentDirectoryPath The assignment directory.
     */
    public void setStudentIds(File studentManifestFile, String assignmentDirectoryPath) {
        //if manifest is undefined try to guess from directories in the assignment dir
        if (studentManifestFile == null) {
            for (File f : new File(assignmentDirectoryPath).listFiles()) {
                //skip files, only look at dirs
                if (!f.isDirectory()) continue;
                try {
                    Integer studentId = Integer.valueOf(f.getName());
                    FeedbackDocument feedbackDocument = new FeedbackDocument(this, studentId.toString());
                    this.studentIdAndFeedbackDocumentMap.put(studentId.toString(), feedbackDocument);
                } catch(NumberFormatException exception) {
                }
            }
        } else {
            // Read the file and store the student id and feedback document in a map
            try (BufferedReader reader = new BufferedReader(new FileReader(studentManifestFile))) {
                while (reader.ready()) {
                    String studentId = reader.readLine().trim();
                    if (!studentId.isEmpty()) {
                        FeedbackDocument feedbackDocument = new FeedbackDocument(this, studentId);
                        this.studentIdAndFeedbackDocumentMap.put(studentId, feedbackDocument);
                    }
                }
            } catch (IOException e) {
                System.err.println("Something went wrong when loading the file: " + studentManifestFile);
            }
        }
    }

    /**
     * Get a list of the feedback documents.
     *
     * @return A list of the feedback documents.
     */
    public List<FeedbackDocument> getFeedbackDocuments() {
        return new ArrayList<FeedbackDocument>(this.studentIdAndFeedbackDocumentMap.values());
    }

    /**
     * Set the feedback documents.
     *
     * @param feedbackDocuments The list of feedback documents to set in the student id feedback document map.
     */
    public void setFeedbackDocuments(List<FeedbackDocument> feedbackDocuments) {
        feedbackDocuments.forEach(feedbackDocument -> {
            this.studentIdAndFeedbackDocumentMap.put(feedbackDocument.getStudentId(), feedbackDocument);
        });
    }

    /**
     * Save assignment details into an FHT file.
     *
     * @param fileName The file name to save the assignment to.
     */
    public void saveAssignmentDetails(String fileName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(assignmentDirectoryPath + File.separator + fileName + ".fht");
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the database name.
     *
     * @return The database name.
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /** Get the database name with its full directory path before it. */
    public String getFullyQualifiedDatabaseName() {
        return this.getAssignmentDirectoryPath() + File.separator + this.getDatabaseName();
    }

    /**
     * Get a feedback document for a given student ID.
     *
     * @param studentId The student ID to get the feedback document for.
     * @return The feedback document for the given student ID.
     */
    public FeedbackDocument getFeedbackDocumentForStudent(String studentId) {
        return studentIdAndFeedbackDocumentMap.get(studentId);
    }

}
