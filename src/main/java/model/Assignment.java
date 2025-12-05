package model;

import static java.util.function.Predicate.not;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
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

    /**
     * Verification for deserialisation, which should change in releases with breaking changes.
     *
     * Last set v5.0
     */
    private static final long serialVersionUID = 5215430117083566496L;

    // Names for files
    private static final String GRADES_FILENAME = "grades.csv";
    private static final String BACKUP_DIRECTORY = "backups";

    // How long to wait between backups, in milliseconds
    private static final long BACKUP_INTERVAL = 15 * 60 * 1000;

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
    private transient long lastBackupTime;

    /**
     * Constructor.
     *
     * @param title The title of the assignment.
     * @param headings The headings of the feedback document, newline-separated.
     * @param studentListFile The student list file, which may be null.
     * @param directory The directory location to save assignment related documents.
     * @param feedbackStyle The style used for feedback documents.
     */
    public Assignment(String title, String headings, Path studentListFile, Path directory, FeedbackStyle feedbackStyle)
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
        this.feedbackStyle = feedbackStyle;

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
        assignment.listeners = new ArrayList<>();
        assignment.lastBackupTime = System.currentTimeMillis();
        assignment.computePhraseCounts();

        System.out.println("Loaded assignment from " + fhtFile);
        return assignment;
    }

    /** Add the listener to this assignment, so it will be notified of changes. */
    public void addListener(AssignmentListener listener) {
        listeners.add(listener);
    }

    /** Set the assignment directory, where files will be saved and exported. */
    private void setDirectory(Path directory) {
        this.directory = directory;
    }

    /**
     * Set a list of the headings to be used in the feedback documents.
     *
     * Only to be called on construction of the assignment.
     *
     * @param assignmentHeadings A string containing a list of the headings to
     * be used in the feedback documents, separated by newline characters
     */
    private void setHeadings(String assignmentHeadings) {
        this.headings = Arrays.stream(assignmentHeadings.split("\n"))
            .map(String::trim)
            .filter(not(String::isEmpty))
            .collect(Collectors.toList());
        headings.forEach(heading -> customPhrases.put(heading, new ArrayList<>()));
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

        // Create and install documents
        for (StudentId studentId : studentIds) {
            feedbackDocuments.put(studentId, new FeedbackDocument(studentId, headings));
        }
    }

    /** Get the list of students from a file or by guessing from the directory contents. */
    public static List<StudentId> findStudentIds(Path studentListFile, Path assignmentDirectory) {
        List<StudentId> studentIds;
        try {
            // Read from the student list file
            studentIds = findStudentIdsFromFile(studentListFile);
        } catch (IOException | NullPointerException e) {
            try {
                // Try searching the assignment directory instead
                studentIds = findStudentIdsFromDirectory(assignmentDirectory);
            } catch (NullPointerException e2) {
                // No students
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
            scanner.useDelimiter(StudentId.DELIMITER);
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
     *
     * @return The thread that is doing the saving, which will run in the background.
     */
    public void save() {
        // Write to a byte array (synchronous) then copy that to a file (asynchronous)
        try (
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
        ) {
            // Write to byte array
            synchronized (this) {
                objectOutputStream.writeObject(this);
                objectOutputStream.flush();
            }
            byte[] bytes = byteStream.toByteArray();

            // Save to disk in another thread
            Thread saveThread = new Thread(() -> {
                try {
                    Path tempFile = Files.createTempFile(null, null);
                    Files.write(tempFile, bytes);
                    String fileName = getFileSafeTitle() + ".fht";
                    Path fhtFile = directory.resolve(fileName);
                    moveFile(tempFile, fhtFile);
                    reportInfo("Saved to " + fhtFile);
                    if (isBackupDue()) {
                        makeBackup(bytes);
                    }
                } catch (IOException e) {
                    reportError("Error saving assignment", e);
                }
            });
            saveThread.start();

            // Wait for the thread to finish, or hand responsibility to a listener
            if (listeners.isEmpty()) {
                saveThread.join();
            } else {
                notifyListeners(l -> l.handleSaveThread(saveThread));
            }
        } catch (IOException | InterruptedException e) {
            reportError("Error saving assignment", e);
        }
    }

    /** Do the copying, which should be synchronized to just one thread. */
    private synchronized void moveFile(Path source, Path target) throws IOException {
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Indicates whether it's been long enough that we should back up the file.
     *
     * If the answer is true, then this method also resets the timer, so when it
     * returns true we should definitely back up.
     */
    private synchronized boolean isBackupDue() {
        long now = System.currentTimeMillis();
        if (now - lastBackupTime > BACKUP_INTERVAL) {
            lastBackupTime = now;
            return true;
        }
        return false;
    }

    private synchronized void makeBackup(byte[] bytes) throws IOException {
        // Choose a name based on the current time
        LocalDateTime now = LocalDateTime.now();
        String fileName = getFileSafeTitle() + "-backup-" + now.toString() + ".fht";

        // Get the backup directory
        Path backupDirectory = directory.resolve(BACKUP_DIRECTORY);
        if (!Files.isDirectory(backupDirectory)) {
            Files.createDirectories(backupDirectory);
        }

        // Create the backup file
        Path backupFile = backupDirectory.resolve(fileName);
        Files.write(backupFile, bytes);
        reportInfo("Assignment backed up to " + backupFile);
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
            Path outputDirectory = createFeedbackOutputDirectory();
            reportInfo("Exported feedback and grades to " + outputDirectory);
            notifyListeners(l -> l.handleExported(outputDirectory));
        } catch (IOException e) {
            reportError("Error exporting feedback and grades.", e);
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
        notifyListeners(l -> l.handleGradeUpdate(studentId, grade));
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

        // Get the phrases up to date
        computePhraseCounts();

        // Broadcast the change
        notifyListeners(l -> l.handleHeadingsUpdated(headings));

        // Save to disk for good measure
        save();
    }

    //
    // PHRASE HANDLING
    //
    private void updatePhrasesForHeading(String heading, String oldContents, String newContents) {
        Set<String> oldPhrases = splitIntoPhrases(oldContents);
        Set<String> newPhrases = splitIntoPhrases(newContents);
        List<Phrase> phrasesForHeading = phraseCounts.get(heading);

        // Handle phrases that were deleted
        Set<String> removals = Utilities.getRemovals(oldPhrases, newPhrases);
        for (Phrase phrase : phrasesForHeading) {
            if (removals.contains(phrase.getPhraseAsString())) {
                phrase.decrementUsageCount();
                notifyListeners(l -> l.handlePhraseCounterUpdated(heading, phrase));
            }
        }
        removeZeroUsePhrases(heading);

        // Handle existing phrases that were added
        Set<String> additions = Utilities.getAdditions(oldPhrases, newPhrases);
        for (Phrase phrase : phrasesForHeading) {
            String text = phrase.getPhraseAsString();
            if (additions.contains(text)) {
                phrase.incrementUsageCount();
                notifyListeners(l -> l.handlePhraseCounterUpdated(heading, phrase));
                additions.remove(text);
            }
        }

        // Add any phrases that haven't been used before
        additions.forEach(phrase -> addPhrase(heading, phrase));

        // Re-sort, since some counts may have changed
        phrasesForHeading.sort(null);
    }

    private void addPhrase(String heading, String phrase) {
        Phrase newPhrase = new Phrase(phrase);
        phraseCounts.get(heading).add(newPhrase);
        notifyListeners(l -> l.handlePhraseAdded(heading, newPhrase));
    }

    private void removeZeroUsePhrases(String heading) {
        List<Phrase> filtered = phraseCounts
            .get(heading)
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

    private Set<String> splitIntoPhrases(String contents) {
        String lineMarker = feedbackStyle.lineMarker();
        return Arrays.stream(contents.split("\n"))
            .map(String::trim)
            .filter(line -> line.startsWith(lineMarker))
            .map(line -> line.substring(lineMarker.length()))
            .collect(Collectors.toSet());
    }

    /** Add a custom phrase for the given heading. */
    public void addCustomPhrase(String heading, String text) {
        customPhrases.get(heading).add(text);
        notifyListeners(l -> l.handleCustomPhraseAdded(heading, getPhraseCount(heading, text)));
    }

    public void deleteCustomPhrase(String heading, String phrase) {
        customPhrases.get(heading).remove(phrase);
        notifyListeners(l -> l.handleCustomPhraseDeleted(heading, phrase));
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
                    .flatMap(Set::stream)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    .map(e -> new Phrase(e.getKey(), e.getValue()))
                    .sorted()
                    .collect(Collectors.toCollection(LinkedList::new))
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
    // PUBLIC GETTERS
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

    /** Get the total number of characters used in feedback for this student. */
    public long getFeedbackLength(StudentId studentId) {
        return feedbackDocuments.get(studentId).length();
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

    /** Get all used phrases for a heading, with their usage counts, in order. */
    public List<Phrase> getPhrasesForHeading(String heading) {
        return phraseCounts.get(heading);
    }
}
