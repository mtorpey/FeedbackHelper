package model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feedback Document Class.
 */
public class FeedbackDocument implements Serializable, Comparable<FeedbackDocument> {

    // Instance variables
    private StudentId studentId;
    private double grade;

    // headings should be the keys of sectionContents.
    // This way is easier than using a SequencedMap.
    private List<String> headings;
    private Map<String, String> sectionContents;

    /**
     * Constructor.
     *
     * @param assignment The assignment the feedback document belongs to.
     * @param studentId  The student ID the feedback document is for.
     */
    public FeedbackDocument(StudentId studentId, List<String> headings) {
        this.studentId = studentId;
        this.headings = headings;

        // Setup the sections
        this.sectionContents = new HashMap<String, String>();
        headings.forEach(heading -> sectionContents.put(heading, ""));

        this.grade = 0.0;
    }

    /**
     * Get the grade.
     */
    public double getGrade() {
        return this.grade;
    }

    /**
     * Set the grade.
     */
    public void setGrade(double grade) {
        this.grade = grade;
    }

    /**
     * Set the data for a given heading.
     *
     * @param heading The heading to set the data for.
     * @param contents The data associated with the heading.
     */
    public void setSectionContents(String heading, String contents) {
        this.sectionContents.put(heading, contents);
    }

    /**
     * Get the data for a given heading.
     *
     * @param heading The heading to get the data for.
     */
    public String getSectionContents(String heading) {
        return this.sectionContents.get(heading);
    }

    /**
     * Get the student ID of the feedback document.
     *
     * @return The student ID of the feedback document
     */
    public StudentId getStudentId() {
        return studentId;
    }

    /**
     * Get a list of headings used in the feedback document.
     *
     * @return a list of headings used in the feedback document.
     */
    public List<String> getHeadings() {
        return headings;
    }

    /**
     * Changes one of the headings.
     *
     * This doesn't do any checks, which should be performed in Assignment::editHeading.
     */
    public void editHeading(String previousHeading, String newHeading) {
        // Note: headings should be a reference to an external list, so should be up to date already
        sectionContents.put(newHeading, sectionContents.get(previousHeading));
        sectionContents.remove(previousHeading);
    }

    /** Export feedback to a text document in the given directory. */
    public void export(Path directory, FeedbackStyle style) throws IOException {
        Path outFile = directory.resolve(getStudentId() + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            for (String heading : getHeadings()) {
                // Heading
                String fullHeading = style.headingStyle() + heading;
                writer.write(fullHeading);
                writer.newLine();

                // Underline heading (may be blank)
                String underlineStyle = style.underlineStyle();
                writer.write(underlineStyle.repeat(fullHeading.length()));

                // Data
                writer.newLine();
                String contents = getSectionContents(heading);
                String[] lines = contents.split("\n");
                String lineMarker = style.lineMarker();
                for (String line : lines) {
                    if (!line.trim().equals(lineMarker)) {
                        writer.write(line);
                        writer.newLine();
                    }
                }

                // End section spacing
                for (int i = 0; i < style.lineSpacing(); i++) {
                    writer.newLine();
                }
            }
        }
    }

    /** Comparable by student id. */
    @Override
    public int compareTo(FeedbackDocument other) {
        return getStudentId().compareTo(other.getStudentId());
    }
}
