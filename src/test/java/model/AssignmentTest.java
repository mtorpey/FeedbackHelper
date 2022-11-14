package model;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class AssignmentTest extends TestCase {

    private Assignment assignment;

    public void setUp() throws Exception {
        assignment = new Assignment();
        assignment.setAssignmentTitle("Test");
        assignment.setAssignmentHeadings("Heading 1 \n Heading 2 \n Heading 3");
        assignment.setAssignmentDirectoryPath("Sample Directory Path");
        assignment.setHeadingStyle("#");
        assignment.setUnderlineStyle("-");
        assignment.setLineMarker("-");
        assignment.setLineSpacing(1);
    }

    public void testGetHeadingStyle() {
        assertEquals("# ", assignment.getHeadingStyle());
    }

    public void testSetHeadingStyle() {
        assignment.setHeadingStyle("##");
        assertEquals("## ", assignment.getHeadingStyle());
    }

    public void testSetInvalidHeadingStyle() {
        assignment.setHeadingStyle("-");
        assertEquals("", assignment.getHeadingStyle());
    }

    public void testGetUnderlineStyle() {
        assertEquals("-", assignment.getUnderlineStyle());
    }

    public void testSetUnderlineStyle() {
        assignment.setUnderlineStyle("=");
        assertEquals("=", assignment.getUnderlineStyle());
    }

    public void testSetInvalidUnderlineStyle() {
        assignment.setUnderlineStyle(".");
        assertEquals("", assignment.getUnderlineStyle());
    }

    public void testGetLineSpacing() {
        assertEquals(1, assignment.getLineSpacing());
    }

    public void testSetLineSpacing() {
        assignment.setLineSpacing(3);
        assertEquals(3, assignment.getLineSpacing());
    }

    public void testSetInvalidLineSpacing() {
        assignment.setLineSpacing(-1);
        assertEquals(1, assignment.getLineSpacing());
    }

    public void testGetLineMarker() {
        assertEquals("- ", assignment.getLineMarker());
    }

    public void testSetLineMarker() {
        assignment.setLineMarker("=>");
        assertEquals("=> ", assignment.getLineMarker());
    }

    public void testSetInvalidLineMarker() {
        assignment.setLineMarker("==>");
        assertEquals("- ", assignment.getLineMarker());
    }

    public void testGetAssignmentDirectoryPath() {
        assertEquals("Sample Directory Path", assignment.getAssignmentDirectoryPath());
    }

    public void testSetAssignmentDirectoryPath() {
        assignment.setAssignmentDirectoryPath("Testing");
        assertEquals("Testing", assignment.getAssignmentDirectoryPath());
    }

    public void testGetDatabaseCollectionName() {
        assertEquals("test-feedback-docs", assignment.getDatabaseCollectionName());
    }

    public void testSetAndGetFeedbackDocument() {
        FeedbackDocument feedbackDocument = new FeedbackDocument(assignment, "1");
        assignment.setFeedbackDocument("1", feedbackDocument);
        assertEquals(assignment.getFeedbackDocumentForStudent("1"), feedbackDocument);
    }

    public void testGetAssignmentTitle() {
        assertEquals("Test", assignment.getAssignmentTitle());
    }

    public void testSetAssignmentTitle() {
        assignment.setAssignmentTitle("New Title");
        assertEquals("New Title", assignment.getAssignmentTitle());
    }

    public void testGetAssignmentHeadings() {
        List<String> assignmentHeadings = assignment.getAssignmentHeadings();
        assertEquals(3, assignmentHeadings.size());
    }

    public void testSetAssignmentHeadings() {
        assignment.setAssignmentHeadings("1\n 2\n 3\n 4\n 5\n");
        List<String> assignmentHeadings = assignment.getAssignmentHeadings();
        assertEquals(5, assignmentHeadings.size());
    }

    public void testGetFeedbackDocuments() {
        assertEquals(0, assignment.getFeedbackDocuments().size());
    }

    public void testSetAndGetFeedbackDocuments() {
        List<FeedbackDocument> feedbackDocuments = new ArrayList<FeedbackDocument>();
        feedbackDocuments.add(new FeedbackDocument(assignment, "123"));
        feedbackDocuments.add(new FeedbackDocument(assignment, "456"));
        feedbackDocuments.add(new FeedbackDocument(assignment, "789"));
        assignment.setFeedbackDocuments(feedbackDocuments);
        assertEquals(3, assignment.getFeedbackDocuments().size());
    }

    public void testGetDatabaseName() {
        assertEquals("test", assignment.getDatabaseName());
    }

}
