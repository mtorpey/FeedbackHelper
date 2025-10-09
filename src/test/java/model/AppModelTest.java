package model;

import junit.framework.TestCase;
import view.PhraseType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppModelTest extends TestCase {

    private AppModel model;
    private Assignment assignment;
    private List<Phrase> currentPhrases;
    private List<Phrase> previousPhrases;

    @Override
    protected void setUp() throws Exception {
        model = new AppModel();

        currentPhrases = new ArrayList<>();
        currentPhrases.add(new Phrase("current phrase 1"));
        currentPhrases.add(new Phrase("current phrase 2"));

        previousPhrases = new ArrayList<>();
        previousPhrases.add(new Phrase("previous phrase 1"));
        previousPhrases.add(new Phrase("previous phrase 2"));

        model.setCurrentHeadingBeingEdited("Heading 1");
        model.setCurrentHeadingPhraseSet("Heading 1", currentPhrases);
        model.setPreviousHeadingPhraseSet("Heading 2", previousPhrases);

        assignment = model.createAssignment("Test", "Heading 1 \n Heading 2 \n Heading 3", new File("Empty file"), "Example-Directory");
    }

    @Override
    protected void tearDown() throws Exception {
        File file = new File("Example-Directory");
        if (file.exists()) {
            file.delete();
        }
    }

    public void testSetCurrentPhrasePanelInView() {
        model.setCurrentPhrasePanelInView(PhraseType.FREQUENTLY_USED);
        model.setCurrentPhrasePanelInView(PhraseType.CUSTOM);
    }

    public void testCreateAssignment() {
        Assignment createdAssignment = model.createAssignment("Test-2", "1 \n 2\n 3\n", new File("Empty file 2"), "Test-Directory");
        assertEquals("Test-2", createdAssignment.getAssignmentTitle());
        assertEquals(3, createdAssignment.getAssignmentHeadings().size());
        assertEquals("Test-Directory", createdAssignment.getAssignmentDirectoryPath());

        // Remove file
        File file = new File("Test-Directory");
        if (file.exists()) {
            file.delete();
        }
    }

    public void testSetAndGetCurrentDocumentInView() {
        model.setCurrentDocumentInView("123", false);
        assertEquals("123", model.getCurrentDocumentInView());
    }

    public void testGetLastDocumentInView() {
        model.setCurrentDocumentInView("123", false);
        model.setCurrentDocumentInView("456", false);
        assertEquals("123", model.getLastDocumentInView());
    }

    public void testGetGrades() {
        List<FeedbackDocument> feedbackDocuments = new ArrayList<FeedbackDocument>();
        for (int i = 0; i <= 5; i++) {
            FeedbackDocument feedbackDocument = new FeedbackDocument(assignment, String.valueOf(i));
            feedbackDocument.setGrade(i * 4);
            feedbackDocuments.add(feedbackDocument);
        }
        assignment.setFeedbackDocuments(feedbackDocuments);

        List<Integer> grades = model.getGrades(assignment);

        // Expect 41 grades number of people who achieved the
        // grades from 0.0 to 20.0 in 0.5 increments
        assertEquals(41, grades.size());

        // Check the 6 grades set
        assertEquals(1, grades.get(0).intValue());   // 1 person got 0.0
        assertEquals(1, grades.get(8).intValue());   // 1 person got 4.0
        assertEquals(1, grades.get(16).intValue());  // 1 person got 8.0
        assertEquals(1, grades.get(24).intValue());  // 1 person got 12.0
        assertEquals(1, grades.get(32).intValue());  // 1 person got 16.0
        assertEquals(1, grades.get(40).intValue());  // 1 person got 20.0
    }

    public void testSetAndGetCurrentHeadingBeingEdited() {
        model.setCurrentHeadingBeingEdited("Heading 1");
        assertEquals("Heading 1", model.getCurrentHeadingBeingEdited());
    }

    public void testGetPreviousHeadingBeingEdited() {
        model.setCurrentHeadingBeingEdited("Heading 1");
        model.setCurrentHeadingBeingEdited("Heading 2");
        assertEquals("Heading 1", model.getPreviousHeadingBeingEdited());
    }

    public void testGetCurrentPhraseSet() {
        assertEquals(currentPhrases, model.getCurrentPhraseSet("Heading 1"));
    }

    public void testSetCurrentHeadingPhraseSet() {
        Phrase newPhrase = new Phrase("new phrase");
        currentPhrases.add(newPhrase);

        model.setCurrentHeadingPhraseSet("Heading 1", currentPhrases);

        List<Phrase> currentPhraseSet = model.getCurrentPhraseSet("Heading 1");

        assertEquals(3, currentPhraseSet.size());
        assertEquals(newPhrase, currentPhraseSet.get(2));
    }

    public void testSetAssignmentPreferences() {
        model.setAssignmentPreferences("##", "=", 3, "=>");
        assertEquals("## ", model.getHeadingStyle());
        assertEquals("=", model.getUnderlineStyle());
        assertEquals(3, model.getLineSpacing());
        assertEquals("=> ", model.getLineMarker());
    }

}
