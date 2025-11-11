package model;

import static java.util.function.Predicate.not;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Assignment Class.
 */
public class Assignment implements Serializable {

    private static final long serialVersionUID = 1200109309800080100L;

    // Name of file used when exporting grades.
    private static final String GRADES_FILENAME = "grades.csv";

    // Instance variables (saved to disk)
    private String title;
    private List<String> headings;
    private SortedMap<StudentId, FeedbackDocument> feedbackDocuments;
    private Map<String, List<String>> customPhrases;
    private FeedbackStyle feedbackStyle;

    // Transient variables (not saved to disk)
    private transient Path directory;
    private transient Map<String, List<Phrase>> phraseCounts;
    private transient List<AssignmentListener> listeners;

    /**
     * Constructor.
     *
     * @param title The title of the assignment.
     * @param headings The headings of the feedback document, newline-separated.
     * @param studentListFile The student list file, which may be null.
     * @param directory The directory location to save assignment related documents.
     */
    public Assignment(String title, String headings, Path studentListFile, Path directory)
        throws NotDirectoryException, IOException {
        // Initialise data structures
        this.feedbackDocuments = new TreeMap<>();
        this.customPhrases = new HashMap<>();
        this.phraseCounts = new HashMap<>();
        this.listeners = new ArrayList<>();

        // Apply construction parameters
        this.title = title;
        setHeadings(headings);
        setStudentIds(studentListFile, directory);
        setDirectory(directory);

        // Create the assignment directory if it does not exist
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // Cancel if it already exists and isn't a directory
        if (!Files.isDirectory(directory)) {
            throw new NotDirectoryException(directory.toString());
        }
    }

    /**
     * Load an assignment from an FHT file.
     *
     * @param fhtFile The location of the FHT file.
     * @return The Assignment object stored in the file.
     */
    public static Assignment load(Path fhtFile) throws IOException, ClassNotFoundException, ClassCastException {
        // Deserialise from file
        Assignment assignment;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(fhtFile))) {
            assignment = (Assignment) objectInputStream.readObject();
        }

        // Set transient fields
        assignment.directory = fhtFile.getParent().toAbsolutePath();
        assignment.computePhraseCounts();
        assignment.listeners = new ArrayList<>();

        System.out.println("Loaded assignment from " + fhtFile);
        return assignment;
    }

    /** Add the listener to this assignment, so it will be notified of changes. */
    public void addListener(AssignmentListener listener) {
        listeners.add(listener);
    }

    /**
     * Set the style preferences for the assignment.
     *
     * @param headingStyle   The heading style
     * @param underlineStyle The heading underline style
     * @param lineSpacing    The line spacing after each section
     * @param lineMarker     The line marker for each new line
     */

    public void setFeedbackStyle(String headingStyle, String underlineStyle, int lineSpacing, String lineMarker) {
        this.feedbackStyle = new FeedbackStyle(headingStyle, underlineStyle, lineSpacing, lineMarker);
    }

    /** Set the assignment directory, where files will be saved and exported. */
    private void setDirectory(Path directory) {
        this.directory = directory;
    }

    /**
     * Set a list of the headings to be used in the feedback documents.
     *
     * @param assignmentHeadings A string containing a list of the headings to
     * be used in the feedback documents, separated by newline characters
     */
    private void setHeadings(String assignmentHeadings) {
        this.headings = Arrays.stream(assignmentHeadings.split("\n"))
            .map(String::trim)
            .filter(not(String::isEmpty))
            .collect(Collectors.toList());
        computePhraseCounts();
    }

    /**
     * Set a list of the student ids and create their feedback documents.
     *
     * @param studentListFile The student list file to read from.
     * @param assignmentDirectory The assignment directory.
     */
    private void setStudentIds(Path studentListFile, Path assignmentDirectory) {
        // Get the ids
        List<StudentId> studentIds = findStudentIds(studentListFile, assignmentDirectory);
        System.out.println("Using student ids " + studentIds);

        // Create and install documents
        for (StudentId studentId : studentIds) {
            feedbackDocuments.put(studentId, new FeedbackDocument(studentId, headings));
        }
    }

    /** Get the list of students from a file or by guessing from the directory contents. */
    public static List<StudentId> findStudentIds(Path studentListFile, Path assignmentDirectory) {
        List<StudentId> studentIds;
        try {
            System.out.print("Searching for student ids in file '" + studentListFile + "'...");
            studentIds = findStudentIdsFromFile(studentListFile);
        } catch (IOException | NullPointerException e) {
            System.out.println(" failed to read file.");
            System.out.print("Searching for submissions in '" + assignmentDirectory + "'...");
            try {
                studentIds = findStudentIdsFromDirectory(assignmentDirectory);
            } catch (NullPointerException e2) {
                studentIds = new ArrayList<>();
            }
        }
        System.out.println(" found " + studentIds.size() + " students.");
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
                try {
                    StudentId studentId = new StudentId(scanner.next());
                    studentIds.add(studentId);
                } catch (IllegalArgumentException e) {
                    // not a valid id, so skip
                    continue;
                }
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
     * Get a list of the feedback documents, sorted by student ID.
     */
    private Collection<FeedbackDocument> getFeedbackDocuments() {
        return this.feedbackDocuments.values();
    }

    /**
     * Save assignment details into an FHT file.
     */
    public void save() {
        String fileName = getFileSafeTitle() + ".fht";
        Path fhtFile = directory.resolve(fileName);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(fhtFile))) {
            objectOutputStream.writeObject(this);
            reportInfo("Saved to " + fhtFile + ".");
        } catch (IOException e) {
            reportError("Error saving assignment", e);
        }
    }

    /**
     * Export the feedback and grades.
     *
     * This writes each feedback document out as a text file in the assignment
     * directory, along with a single CSV file with all student IDs and grades.
     */
    public void export() {
        try {
            exportFeedback();
            exportGrades();
            reportInfo("Exported feedback and grades to " + createFeedbackOutputDirectory() + ".");
        } catch (IOException e) {
            reportError("Error exporting feedback and grades", e);
        }
    }

    private void exportFeedback() throws IOException {
        Path outputDirectory = createFeedbackOutputDirectory();
        for (FeedbackDocument document : getFeedbackDocuments()) {
            document.export(outputDirectory, feedbackStyle);
        }
    }

    private void exportGrades() throws IOException {
        Path outputDirectory = createFeedbackOutputDirectory();
        Path gradesFile = outputDirectory.resolve(GRADES_FILENAME);
        try (BufferedWriter writer = Files.newBufferedWriter(gradesFile)) {
            for (FeedbackDocument document : getFeedbackDocuments()) {
                writer.write(document.getStudentId() + "," + document.getGrade());
                writer.newLine();
            }
        }
    }

    /** Create a directory where exported feedback should be placed, and return its path. */
    private Path createFeedbackOutputDirectory() throws IOException {
        Path outputDirectory = directory.resolve(getFileSafeTitle() + "-feedback");
        if (!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
        }
        return outputDirectory;
    }

    /** Get the name of this assignment, normalised for use in filenames. */
    private String getFileSafeTitle() {
        String rejected = "[^-a-zA-Z_0-9!#$%&\\+=\\^\\{\\}~]+";
        return title.trim().replaceAll(rejected, "-");
    }

    /**
     * Update the model with the feedback in the given sections for a particular student.
     *
     * @param headingsAndData The feedback sections to be updated, which might not be all of them.
     */
    public void updateFeedback(StudentId studentId, Map<String, String> headingsAndData) {
        FeedbackDocument document = feedbackDocuments.get(studentId);
        for (String heading : headingsAndData.keySet()) {
            // Set the new text for this section
            String oldContents = document.getSectionContents(heading);
            String newContents = headingsAndData.get(heading);
            document.setSectionContents(heading, newContents);

            // Handle phrase counts
            updatePhrasesForHeading(heading, oldContents, newContents);
        }
        // Note: we don't automatically save here
    }

    /** Update the grade in the model for the given student. */
    public void updateGrade(StudentId studentId, double grade) {
        feedbackDocuments.get(studentId).setGrade(grade);
        // Note: we don't automatically save here
    }

    public void addStudent(StudentId studentId) {
        // Check for existing students with this ID.
        if (feedbackDocuments.containsKey(studentId)) {
            reportError("Student '" + studentId + "' already exists.", new IllegalArgumentException());
        } else {
            // Create and install document
            feedbackDocuments.put(studentId, new FeedbackDocument(studentId, headings));
            save();
            notifyListeners(l -> l.handleNewStudent(studentId));
            reportInfo("New student '" + studentId + "' added.");
        }
    }

    /**
     * Change the name of a section, i.e. a heading.
     *
     * @param previousHeading A heading that currently exists in the assignment
     * @param newHeading The new name for that heading
     */
    public void editHeading(String previousHeading, String newHeading) throws IllegalArgumentException {
        // Check the heading is not blank
        if (newHeading.isBlank()) {
            throw new IllegalArgumentException("New heading cannot be blank.");
        }

        // Check that the new heading is not the same as any old ones
        for (String heading : headings) {
            if (heading.equals(newHeading)) {
                throw new IllegalArgumentException("The heading " + heading + " already exists.");
            }
        }

        // Modify the appropriate heading
        int headingPosition = headings.indexOf(previousHeading);
        headings.set(headingPosition, newHeading);

        // Update the keys in the feedback documents
        getFeedbackDocuments().forEach(doc -> doc.editHeading(previousHeading, newHeading));

        // Update the keys in the custom phrases
        customPhrases.put(newHeading, customPhrases.get(previousHeading));
        customPhrases.remove(previousHeading);

        // Broadcast the change
        notifyListeners(l -> l.handleHeadingsUpdated(headings));

        // Save to disk for good measure
        save();
    }

    //
    // PHRASE HANDLING
    //
    private void updatePhrasesForHeading(String heading, String oldContents, String newContents) {
        List<String> oldPhrases = splitIntoPhrases(oldContents);
        List<String> newPhrases = splitIntoPhrases(newContents);

        // Handle phrases that were deleted
        List<String> removals = Utilities.getRemovalsFromList(oldPhrases, newPhrases); // TODO: make Sets?
        for (Phrase phrase : getPhrasesForHeading(heading)) {
            if (removals.contains(phrase.getPhraseAsString())) {
                phrase.decrementUsageCount();
                notifyListeners(l -> l.handlePhraseCounterUpdated(heading, phrase));
            }
        }
        removeZeroUsePhrases(heading);

        // Handle existing phrases that were added
        List<String> additions = Utilities.getAdditionsToList(oldPhrases, newPhrases);
        for (Phrase phrase : getPhrasesForHeading(heading)) {
            int pos = additions.indexOf(phrase.getPhraseAsString());
            if (pos != -1) {
                phrase.incrementUsageCount();
                notifyListeners(l -> l.handlePhraseCounterUpdated(heading, phrase));
                additions.remove(pos);
            }
        }

        // Add any phrases that haven't been used before
        additions.stream().forEach(phrase -> addPhrase(heading, phrase));
    }

    private void addPhrase(String heading, String phrase) {
        Phrase newPhrase = new Phrase(phrase);
        phraseCounts.get(heading).add(newPhrase);
        notifyListeners(l -> l.handlePhraseAdded(heading, newPhrase));
    }

    private void removeZeroUsePhrases(String heading) {
        List<Phrase> filtered = getPhrasesForHeading(heading)
            .stream()
            .filter(Phrase::isUnused)
            .collect(Collectors.toList());
        for (Phrase phrase : filtered) {
            removePhrase(heading, phrase);
        }
    }

    private void removePhrase(String heading, Phrase phrase) {
        phraseCounts.get(heading).remove(phrase);
        notifyListeners(l -> l.handlePhraseDeleted(heading, phrase));
    }

    private List<String> splitIntoPhrases(String contents) {
        String lineMarker = feedbackStyle.lineMarker();
        return Arrays.stream(contents.split("\n"))
            .map(String::trim)
            .filter(line -> line.startsWith(lineMarker))
            .map(line -> line.replaceFirst(lineMarker, ""))
            .collect(Collectors.toList());
    }

    /** Add a custom phrase for the given heading. */
    public void addCustomPhrase(String heading, String text) {
        customPhrases.get(heading).add(text);
        notifyListeners(l -> l.handleCustomPhraseAdded(heading, getPhraseCount(heading, text)));
    }

    /** Get a Phrase object representing uses of the given text phrase in the given heading. */
    private Phrase getPhraseCount(String heading, String text) {
        return phraseCounts
            .get(heading)
            .stream()
            .filter(phrase -> phrase.getPhraseAsString().equals(text))
            .findFirst()
            .orElse(new Phrase(text, 0L));
    }

    private void computePhraseCounts() {
        phraseCounts = new HashMap<>();
        headings.forEach(heading ->
            phraseCounts.put(
                heading,
                getFeedbackDocuments()
                    .stream()
                    .map(doc -> doc.getSectionContents(heading))
                    .map(this::splitIntoPhrases)
                    .flatMap(List::stream)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    .map(e -> new Phrase(e.getKey(), e.getValue()))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList())
            )
        );
        reportInfo("Computed phrase counts.");
    }

    //
    // LISTENER HANDLING
    // since some of these might be called from a different thread
    //
    private void reportInfo(String message) {
        notifyListeners(l -> l.handleInfo(message));
        System.out.println(message);
    }

    private void reportError(String description, Exception exception) {
        notifyListeners(l -> l.handleError(description, exception));
        System.err.println(description);
        exception.printStackTrace();
    }

    private void notifyListeners(Consumer<AssignmentListener> run) {
        listeners.forEach(run);
    }

    //
    // PUBLIG GETTERS
    //
    // These should be all the view needs for querying the assignment
    //

    /**
     * Get the string that appears at the start of a phrase in feedback.
     *
     * The view's behaviour depends on this, so we expose it even though we
     * don't do so with the other members of FeedbackStyle.
     */
    public String getLineMarker() {
        return feedbackStyle.lineMarker();
    }

    /** Get the assignment title. */
    public String getTitle() {
        return title;
    }

    /** Get an unmodifiable list of the current headings used in this assignment. */
    public List<String> getHeadings() {
        return List.copyOf(headings);
    }

    /** Get a sorted, unmodifiable list of the students in this assignment. */
    public List<StudentId> getStudentIds() {
        return List.copyOf(feedbackDocuments.keySet());
    }

    public String getSectionContents(StudentId studentId, String heading) {
        return feedbackDocuments.get(studentId).getSectionContents(heading);
    }

    public double getGrade(StudentId studentId) {
        return feedbackDocuments.get(studentId).getGrade();
    }

    /** Get an anonymised list of grades. */
    public List<Double> getGradesList() {
        return getFeedbackDocuments().stream().map(FeedbackDocument::getGrade).collect(Collectors.toList());
    }

    /** Get all the custom phrases for a heading, with their usage counts. */
    public List<Phrase> getCustomPhrases(String heading) {
        // Note: doing this lookup every time could be a bit expensive
        return customPhrases
            .get(heading)
            .stream()
            .map(text -> getPhraseCount(heading, text))
            .toList();
    }

    /** Get all used phrases for a heading, with their usage counts. */
    public List<Phrase> getPhrasesForHeading(String heading) {
        return phraseCounts.get(heading);
    }
}
