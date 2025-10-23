package model;

import static java.util.function.Predicate.not;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Assignment Class.
 */
public class Assignment implements Serializable {

    private static final long serialVersionUID = 1200109309800080100L;

    // Heading styles map
    private static final List<String> headingStyles = Collections.unmodifiableList(
        new ArrayList<String>() {
            {
                add("#");
                add("##");
            }
        }
    );

    // Heading underline style map
    private final List<String> underlineStyles = Collections.unmodifiableList(
        new ArrayList<String>() {
            {
                add("-");
                add("=");
            }
        }
    );

    // Line spacing map
    private final List<Integer> lineSpacings = Collections.unmodifiableList(
        new ArrayList<Integer>() {
            {
                add(1);
                add(2);
                add(3);
            }
        }
    );

    // Line marker map
    private final List<String> lineMarkers = Collections.unmodifiableList(
        new ArrayList<String>() {
            {
                add("-");
                add("->");
                add("=>");
                add("*");
                add("+");
            }
        }
    );

    // Instance variables
    private String assignmentTitle;
    private List<String> assignmentHeadings;
    private Map<StudentId, FeedbackDocument> feedbackDocuments;
    private String headingStyle;
    private String underlineStyle;
    private int lineSpacing;
    private String lineMarker;
    private transient Path directory;

    /**
     * Constructor.
     */
    public Assignment() {
        this.assignmentHeadings = new ArrayList<String>();
        feedbackDocuments = new HashMap<StudentId, FeedbackDocument>();
    }

    /**
     * Load an assignment from an FHT file.
     *
     * @param fhtFile The location of the FHT file.
     * @return The Assignment object stored in the file.
     */
    public static Assignment loadAssignment(Path fhtFile) {
        Assignment loadedAssignment = null;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(fhtFile))) {
            loadedAssignment = (Assignment) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Loaded assignment from " + fhtFile);
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
     */
    public String getLineMarker() {
        return lineMarker;
    }

    /**
     * Set the line marker to use for denoting new lines.
     */
    public void setLineMarker(String lineMarker) {
        if (lineMarkers.contains(lineMarker)) {
            this.lineMarker = lineMarker.trim() + " ";
        } else {
            this.lineMarker = "- ";
        }
    }

    /**
     * Get an absolute path to the assignment directory.
     *
     * Note: this is transient (not saved when we serialize) and should always
     * be set in AppController.loadAssignment immediately after loading from
     * disk. This allows for the assignment to be moved on disk while the
     * program is closed.
     */
    public Path getDirectory() {
        return directory.toAbsolutePath();
    }

    /**
     * Set the assignment directory path.
     */
    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    /**
     * Get the database collection name used for the assignment's documents.
     */
    public String getDatabaseCollectionName() {
        return getFileSafeTitle() + "-feedback-docs";
    }

    /**
     * Store the feedback document for a given student ID.
     *
     * @param studentId        The student ID the feedback document is for.
     * @param feedbackDocument The feedback document for the student.
     */
    public void setFeedbackDocument(StudentId studentId, FeedbackDocument feedbackDocument) {
        this.feedbackDocuments.put(studentId, feedbackDocument);
    }

    /**
     * Get the assignment title.
     */
    public String getAssignmentTitle() {
        return this.assignmentTitle;
    }

    /**
     * Set the assignment title.
     */
    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    /**
     * Get a list of the headings to be used in the feedback documents.
     *
     * @return A list of the headings to be used in the feedback documents.
     */
    public List<String> getHeadings() {
        return this.assignmentHeadings;
    }

    /**
     * Set a list of the headings to be used in the feedback documents.
     *
     * @param assignmentHeadings A string containing a list of the headings to
     * be used in the feedback documents, separated by newline characters
     */
    public void setAssignmentHeadings(String assignmentHeadings) {
        this.assignmentHeadings = Arrays.stream(assignmentHeadings.split("\n"))
            .map(String::trim)
            .filter(not(String::isEmpty))
            .collect(Collectors.toList());
    }

    /**
     * Set a list of the student ids.
     *
     * @param studentListFile The student list file to read from.
     * @param assignmentDirectory The assignment directory.
     */
    public void setStudentIds(Path studentListFile, Path assignmentDirectory) {
        // Get the ids
        List<StudentId> studentIds = findStudentIds(studentListFile, assignmentDirectory);
        System.out.println("Using student ids " + studentIds);

        // Create and install documents
        for (StudentId studentId : studentIds) {
            feedbackDocuments.put(studentId, new FeedbackDocument(this, studentId));
        }
    }

    /** Get the list of students from a file or by guessing from the directory contents. */
    public static List<StudentId> findStudentIds(Path studentListFile, Path assignmentDirectory) {
        List<StudentId> studentIds;
        try {
            System.out.println("Looking for student ids in file '" + studentListFile + "'...");
            studentIds = findStudentIdsFromFile(studentListFile);
        } catch (IOException | NullPointerException e) {
            System.out.println("Failed to read file");
            System.out.println("Searching for submissions in " + assignmentDirectory + " ...");
            try {
                studentIds = findStudentIdsFromDirectory(assignmentDirectory);
            } catch (NullPointerException e2) {
                System.out.println("Unable to find any submissions");
                studentIds = new ArrayList<>();
            }
        }
        return studentIds;
    }

    /**
     * Get a list of student IDs from the given file.
     *
     * @throws IOException if something goes wrong while reading
     * @throws NullPointerException if the argument is null
     */
    private static List<StudentId> findStudentIdsFromFile(Path file) throws IOException, NullPointerException {
        if (!Files.isRegularFile(file)) {
            throw new FileNotFoundException("not a regular file");
        }
        List<StudentId> studentIds = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter("\\s|,|;");
            while (scanner.hasNext()) {
                System.out.println("Next");
                try {
                    StudentId studentId = new StudentId(scanner.next());
                    studentIds.add(studentId);
                } catch (IllegalArgumentException e) {
                    // not a valid id, so skip
                    continue;
                }
                System.out.println(studentIds.size() + " students");
            }
        }
        return studentIds;
    }

    /**
     * Search the given directory for files/directories that look like student IDs.
     *
     * @param directory The directory to search
     */
    private static List<StudentId> findStudentIdsFromDirectory(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.matches(StudentId.ST_ANDREWS_PATTERN + "(\\..*)?"))
                .map(name -> new StudentId(name.split("\\.")[0]))
                .collect(Collectors.toList());
        } catch (IOException e) {
            // problem searching directory, so none found
            return List.of();
        }
    }

    /**
     * Get a list of the feedback documents.
     *
     * @return A list of the feedback documents.
     */
    public List<FeedbackDocument> getFeedbackDocuments() {
        return this.feedbackDocuments.values()
            .stream()
            .sorted() // FeedbackDocument implements compareTo using studentId
            .collect(Collectors.toList());
    }

    /**
     * Set the feedback documents.
     *
     * @param feedbackDocuments The list of feedback documents to set in the student id feedback document map.
     */
    public void addFeedbackDocuments(List<FeedbackDocument> feedbackDocuments) {
        for (FeedbackDocument document : feedbackDocuments) {
            this.feedbackDocuments.put(document.getStudentId(), document);
        }
    }

    /**
     * Save assignment details into an FHT file.
     */
    public void saveAssignmentDetails() throws IOException {
        String fileName = getFileSafeTitle() + ".fht";
        Path fhtFile = directory.resolve(fileName);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(fhtFile))) {
            objectOutputStream.writeObject(this);
            System.out.println("Saved to " + fhtFile);
        }
    }

    /**
     * Create a directory where exported feedback should be placed, and return its path.
     */
    public Path createFeedbackOutputDirectory() throws IOException {
        Path outputDirectory = getDirectory().resolve(getFileSafeTitle() + "-feedback");
        if (!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
        }
        return outputDirectory;
    }

    /**
     * Get the name of this assignment, normalised for use in filenames.
     */
    public String getFileSafeTitle() {
        String rejected = "[^-a-zA-Z_0-9!#$%&\\+=\\^\\{\\}~]+";
        return assignmentTitle.trim().replaceAll(rejected, "-");
    }

    /**
     * Get a feedback document for a given student ID.
     *
     * @param studentId The student ID to get the feedback document for.
     * @return The feedback document for the given student ID.
     */
    public FeedbackDocument getFeedbackDocumentForStudent(StudentId studentId) {
        return feedbackDocuments.get(studentId);
    }
}
