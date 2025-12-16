package uk.myoung.feedbackhelper.model;

import java.util.List;

/**
 * Read-only view of an Assignment object.
 *
 * This is used by objects in the view, which should only be able to query an
 * assignment and not mutate one.
 *
 * Descriptions of the methods can be found in the doc of the implementing class
 * Assignment.
 */
public interface AssignmentReadOnly {
    String getLineMarker();
    String getTitle();
    List<String> getHeadings();
    List<StudentId> getStudentIds();
    String getSectionContents(StudentId studentId, String heading);
    long getFeedbackLength(StudentId studentId);
    double getGrade(StudentId studentId);
    List<Double> getGradesList();
    List<Phrase> getCustomPhrases(String heading);
    List<Phrase> getPhrasesForHeading(String heading);
}
