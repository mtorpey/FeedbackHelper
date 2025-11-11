package model;

import junit.framework.TestCase;

public class FeedbackDocumentTest extends TestCase {

    private Assignment assignment;
    private FeedbackDocument feedbackDocument;

    public void setUp() {
        assignment = new Assignment();
        assignment.setTitle("Test");
        assignment.setHeadings("Heading 1 \n Heading 2 \n Heading 3 \n");
        feedbackDocument = new FeedbackDocument(assignment, "1");
        feedbackDocument.setGrade(20);
        feedbackDocument.setSectionContents("Heading 1", "Data-1");
        feedbackDocument.setSectionContents("Heading 2", "Data-2");
        feedbackDocument.setSectionContents("Heading 3", "Data-3");
    }

    public void testGetGrade() {
        assertEquals(20.0, feedbackDocument.getGrade());
    }

    public void testSetGrade() {
        feedbackDocument.setGrade(19.5);
        assertEquals(19.5, feedbackDocument.getGrade());
    }

    public void testGetHeadingData() {
        assertEquals("Data-1", feedbackDocument.getSectionContents("Heading 1"));
        assertEquals("Data-2", feedbackDocument.getSectionContents("Heading 2"));
        assertEquals("Data-3", feedbackDocument.getSectionContents("Heading 3"));
    }

    public void testGetAssignment() {
        assertEquals(assignment, feedbackDocument.getAssignment());
    }

    public void testGetStudentId() {
        assertEquals("1", feedbackDocument.getStudentId());
    }

    public void testGetHeadings() {
        assertEquals(3, feedbackDocument.getHeadings().size());
    }

    public void testToString() {
        assertEquals("FeedbackDocument{assignment=test, studentId=1}", feedbackDocument.toString());
    }
}
