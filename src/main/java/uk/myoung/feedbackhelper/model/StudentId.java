package uk.myoung.feedbackhelper.model;

import java.io.Serializable;

/** Class for a student ID, which ensures validity and provides some checking functionality. */
public record StudentId(String id) implements Comparable<StudentId>, Serializable {
    /**
     * Regex pattern for valid IDs.
     *
     * An ID must be at least 1 character, and consist of only alphanumeric
     * characters and a few other allowed symbols.
     */
    public static final String ALLOWED_CHARACTERS = "-a-zA-Z_0-9!#$%&\\*\\+/=\\?\\^\\{\\}~";
    public static final String ALLOWED_PATTERN = "[" + ALLOWED_CHARACTERS + "]+";
    public static final String DELIMITER = "[^" + ALLOWED_CHARACTERS + "]+";

    /**
     * Regex pattern for St Andrews matric numbers (9 digits).
     */
    public static final String ST_ANDREWS_PATTERN = "\\d{9}";

    /** Constructor, which checks for validity of ID. */
    public StudentId {
        if (!id.matches(ALLOWED_PATTERN)) {
            throw new IllegalArgumentException("Illegal student id '" + id + "'");
        }
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(StudentId other) {
        return id.compareTo(other.id);
    }
}
