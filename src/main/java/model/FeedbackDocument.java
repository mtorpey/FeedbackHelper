package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Feedback Document Class.
 */
public class FeedbackDocument implements Serializable, Comparable<FeedbackDocument> {

    // Instance variables
    private Assignment assignment;
    private String studentId;
    private HashMap<String, String> headingAndData;
    private double grade;

    /**
     * Constructor.
     *
     * @param assignment The assignment the feedback document belongs to.
     * @param studentId  The student ID the feedback document is for.
     */
    public FeedbackDocument(Assignment assignment, String studentId) {
        this.assignment = assignment;
        this.studentId = studentId;
        this.headingAndData = new HashMap<String, String>();

        // Set the heading data to empty
        this.assignment.getAssignmentHeadings().forEach(heading -> {
            headingAndData.put(heading, "");
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
        this.headingAndData.put(heading, data);
    }

    /**
     * Get the data for a given heading.
     *
     * @param heading The heading to get the data for.
     * @return The data for the given heading.
     */
    public String getHeadingData(String heading) {
        return this.headingAndData.get(heading);
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
    public String getStudentId() {
        return studentId;
    }

    /**
     * Get a list of headings used in the feedback document.
     *
     * @return a list of headings used in the feedback document.
     */
    public List<String> getHeadings() {
        return assignment.getAssignmentHeadings();
    }

    /**
     * String representation of the FeedbackDocument Object.
     *
     * @return The string representation of the object.
     */
    @Override
    public String toString() {
        return "FeedbackDocument{" +
                "assignment=" + assignment.getDatabaseName() +
                ", studentId=" + studentId +
                '}';
    }

    /** Comparable by student id. */
    @Override
    public int compareTo(FeedbackDocument other) {
        return getStudentId().compareTo(other.getStudentId());
    }

}
