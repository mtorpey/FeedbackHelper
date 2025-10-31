package model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * Feedback Document Class.
 */
public class FeedbackDocument implements Serializable, Comparable<FeedbackDocument> {

    // Instance variables
    private Assignment assignment;
    private StudentId studentId;
    private HashMap<String, String> sectionContents;
    private double grade;

    /**
     * Constructor.
     *
     * @param assignment The assignment the feedback document belongs to.
     * @param studentId  The student ID the feedback document is for.
     */
    public FeedbackDocument(Assignment assignment, StudentId studentId) {
        this.assignment = assignment;
        this.studentId = studentId;
        this.sectionContents = new HashMap<String, String>();

        // Set the heading data to empty
        this.assignment.getHeadings().forEach(heading -> {
            sectionContents.put(heading, "");
        });

        this.grade = 0.0;
    }

    /**
     * Get the grade.
     *
     * @return The grade.
     */
    public double getGrade() {
        return this.grade;
    }

    /**
     * Set the grade.
     *
     * @param grade The grade to set.
     */
    public void setGrade(double grade) {
        this.grade = grade;
    }

    /**
     * Set the data for a given heading.
     *
     * @param heading The heading to set the data for.
     * @param data    The data associated with the heading.
     */
    public void setDataForHeading(String heading, String data) {
        this.sectionContents.put(heading, data);
    }

    /**
     * Get the data for a given heading.
     *
     * @param heading The heading to get the data for.
     * @return The data for the given heading.
     */
    public String getSectionContents(String heading) {
        return this.sectionContents.get(heading);
    }

    /**
     * Get the assignment the feedback document belongs to.
     *
     * @return The Assignment object the feedback document belongs to.
     */
    public Assignment getAssignment() {
        return assignment;
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
        return assignment.getHeadings();
    }

    /**
     * Changes one of the headings.
     *
     * This doesn't do any checks, which should be performed in Assignment::editHeading.
     */
    public void editHeading(String previousHeading, String newHeading) {
        sectionContents.put(newHeading, sectionContents.get(previousHeading));
        sectionContents.remove(previousHeading);
    }

    /** Export feedback to a text document in the given directory. */
    public void export(Path directory) throws IOException {
        Path outFile = directory.resolve(getStudentId() + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            for (String heading : getHeadings()) {
                // Heading
                String fullHeading = assignment.getHeadingStyle() + heading;
                writer.write(fullHeading);
                writer.newLine();

                // Underline heading (may be blank)
                String underlineStyle = assignment.getUnderlineStyle();
                writer.write(underlineStyle.repeat(fullHeading.length()));

                // Data
                writer.newLine();
                String contents = getSectionContents(heading);
                String[] lines = contents.split("\n");
                String lineMarker = assignment.getLineMarker();
                for (String line : lines) {
                    if (!line.trim().equals(lineMarker)) {
                        writer.write(line);
                        writer.newLine();
                    }
                }

                // End section spacing
                for (int i = 0; i < assignment.getLineSpacing(); i++) {
                    writer.newLine();
                }
            }
        }
    }

    /**
     * Simple string indication of the FeedbackDocument.
     */
    @Override
    public String toString() {
        return "FeedbackDocument{" + studentId + '}';
    }

    /** Comparable by student id. */
    @Override
    public int compareTo(FeedbackDocument other) {
        return getStudentId().compareTo(other.getStudentId());
    }
}
