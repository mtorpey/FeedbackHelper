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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Assignment Class.
 */
public class Assignment implements Serializable {

    private static final long serialVersionUID = 1200109309800080100L;

    // Instance variables (saved to disk)
    private String title;
    private List<String> headings;
    private Map<StudentId, FeedbackDocument> feedbackDocuments;
    private Map<String, List<String>> customPhrases;
    private String headingStyle;
    private String underlineStyle;
    private int lineSpacing;
    private String lineMarker;

    // Transient variables (not saved to disk)
    private transient Path directory;
    private transient Map<String, List<Phrase>> phraseCounts;
    private transient AppModel model;

    /**
     * Constructor.
     */
    public Assignment() {
        System.out.println("Making assignment");
        this.headings = new ArrayList<>();
        feedbackDocuments = new HashMap<>();
        customPhrases = new HashMap<>();
        phraseCounts = new HashMap<>();
    }

    public void setModel(AppModel model) {
        this.model = model;
    }

    /**
     * Load an assignment from an FHT file.
     *
     * @param fhtFile The location of the FHT file.
     * @return The Assignment object stored in the file.
     */
    public static Assignment load(Path fhtFile) {
        // Deserialise from file
        Assignment loadedAssignment = null;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(fhtFile))) {
            loadedAssignment = (Assignment) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // TODO: handle errors better
            e.printStackTrace();
        }

        // Set transient fields
        loadedAssignment.directory = fhtFile.getParent().toAbsolutePath();
        loadedAssignment.computePhraseCounts();

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
        // Check if the heading style is allowed, if not use a default
        if (headingStyle.length() > 0) {
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
        // Check if the underline style is allowed, if not use a default
        if (underlineStyle.length() <= 1) {
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
        if (lineSpacing >= 0) {
            this.lineSpacing = lineSpacing;
        } else {
            this.lineSpacing = 0;
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
        if (lineMarker.length() > 0) {
            this.lineMarker = lineMarker.trim() + " ";
        } else {
            this.lineMarker = "- ";
        }
    }

    /** Get an absolute path to the assignment directory. */
    public Path getDirectory() {
        return directory.toAbsolutePath();
    }

    /** Set the assignment directory, where files will be saved and exported. */
    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    /**
     * Store the feedback document for a given student ID.
     *
     * @param studentId        The student ID the feedback document is for.
     * @param feedbackDocument The feedback document for the student.
     */
    public void setFeedbackDocument(StudentId studentId, FeedbackDocument feedbackDocument) {
        // TODO: remove
        this.feedbackDocuments.put(studentId, feedbackDocument);
    }

    /**
     * Get the assignment title.
     */
    public String getTitle() {
        // TODO: remove
        return this.title;
    }

    /**
     * Set the assignment title.
     */
    public void setTitle(String assignmentTitle) {
        this.title = assignmentTitle;
    }

    /**
     * Get a list of the headings to be used in the feedback documents.
     *
     * @return A list of the headings to be used in the feedback documents.
     */
    public List<String> getHeadings() {
        // TODO: remove/privatise
        return this.headings;
    }

    /**
     * Set a list of the headings to be used in the feedback documents.
     *
     * @param assignmentHeadings A string containing a list of the headings to
     * be used in the feedback documents, separated by newline characters
     */
    public void setAssignmentHeadings(String assignmentHeadings) {
        this.headings = Arrays.stream(assignmentHeadings.split("\n"))
            .map(String::trim)
            .filter(not(String::isEmpty))
            .collect(Collectors.toList());
        computePhraseCounts();
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
            System.out.println("Searching for submissions in '" + assignmentDirectory + "'...");
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
     * Get a list of the feedback documents, sorted by student ID.
     *
     * @return A list of the feedback documents.
     */
    public List<FeedbackDocument> getFeedbackDocuments() {
        // TODO: privatise
        return this.feedbackDocuments.values()
            .stream()
            .sorted() // FeedbackDocument implements compareTo using studentId
            .collect(Collectors.toList());
    }

    /**
     * Save assignment details into an FHT file.
     */
    public void save() throws IOException {
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
        // TODO: privatise
        Path outputDirectory = getDirectory().resolve(getFileSafeTitle() + "-feedback");
        if (!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
        }
        return outputDirectory;
    }

    /**
     * Get the name of this assignment, normalised for use in filenames.
     */
    private String getFileSafeTitle() {
        String rejected = "[^-a-zA-Z_0-9!#$%&\\+=\\^\\{\\}~]+";
        return title.trim().replaceAll(rejected, "-");
    }

    /**
     * Get a feedback document for a given student ID.
     *
     * @param studentId The student ID to get the feedback document for.
     * @return The feedback document for the given student ID.
     */
    public FeedbackDocument getFeedbackDocument(StudentId studentId) {
        // TODO: remove/privatise
        return feedbackDocuments.get(studentId);
    }

    /**
     * Update the model with the feedback in the given sections for a particular student.
     *
     * @param headingsAndData The feedback sections to be updated, which might not be all of them.
     */
    public void updateFeedback(StudentId studentId, Map<String, String> headingsAndData) {
        FeedbackDocument document = getFeedbackDocument(studentId);
        for (String heading : headingsAndData.keySet()) {
            // Set the new text for this section
            String oldContents = document.getSectionContents(heading);
            String newContents = headingsAndData.get(heading);
            document.setDataForHeading(heading, newContents);

            // Handle phrase counts
            updatePhrasesForHeading(heading, oldContents, newContents);
        }
        
        // Update custom panel
        model.resetCustomPhrasesPanel();
    }

    /** Update the grade in the model for the given student. */
    public void updateGrade(StudentId studentId, double grade) {
        getFeedbackDocument(studentId).setGrade(grade);
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
    }

    /*
     * PHRASE HANDLING
     */
    private void updatePhrasesForHeading(String heading, String oldContents, String newContents) {
        List<String> oldPhrases = splitIntoPhrases(oldContents);
        List<String> newPhrases = splitIntoPhrases(newContents);

        // Handle phrases that were deleted
        List<String> removals = Utilities.getRemovalsFromList(oldPhrases, newPhrases);  // TODO: make Sets?
        for (Phrase phrase : getPhrasesForHeading(heading)) {
            if (removals.contains(phrase.getPhraseAsString())) {
                phrase.decrementUsageCount();
                model.updatePhraseCounterInView(heading, phrase);
            }
        }
        removeZeroUsePhrases(heading);

        // Handle existing phrases that were added
        List<String> additions = Utilities.getAdditionsToList(oldPhrases, newPhrases);
        for (Phrase phrase : getPhrasesForHeading(heading)) {
            int pos = additions.indexOf(phrase.getPhraseAsString());
            if (pos != -1) {
                phrase.incrementUsageCount();
                model.updatePhraseCounterInView(heading, phrase);
                additions.remove(pos);
            }
        }

        // Add any phrases that haven't been used before
        additions.stream().forEach(phrase -> addPhrase(heading, phrase));
    }

    private void addPhrase(String heading, String phrase) {
        Phrase newPhrase = new Phrase(phrase);
        phraseCounts.get(heading).add(newPhrase);
        model.addNewPhraseToView(heading, newPhrase);
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
        model.removePhraseFromView(heading, phrase);
    }

    private List<String> splitIntoPhrases(String contents) {
        return Arrays.stream(contents.split("\n"))
            .map(String::trim)
            .filter(line -> line.startsWith(lineMarker))
            .map(line -> line.replaceFirst(lineMarker, ""))
            .collect(Collectors.toList());
    }

    public void addCustomPhrase(String heading, String phrase) {
        customPhrases.get(heading).add(phrase);
        model.addNewCustomPhraseToView(heading, phrase);
    }

    public List<String> getCustomPhrases(String heading) {
        return customPhrases.get(heading);
    }

    private List<Phrase> getPhrasesForHeading(String heading) {
        System.out.println(this);
        System.out.println(this.headings);
        System.out.println(phraseCounts);
        return phraseCounts.get(heading);
    }

    private void computePhraseCounts() {
        System.out.println("Computing phrase counts, with headings " + getHeadings());
        phraseCounts = new HashMap<>();
        getHeadings().forEach(
            heading ->
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
    }
}
