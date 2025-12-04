package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeedbackDocumentTest {

    final List<String> INITIAL_HEADINGS = List.of("Code", "Report quality", "Overall");
    final StudentId STUDENT_ID = new StudentId("110004561");
    final StudentId STUDENT_ID2 = new StudentId("110004564");
    final FeedbackStyle STYLE = new FeedbackStyle("#", "", 2, "-");
    final String EXAMPLE_TEXT = """
        - Could do with improvement.
        - I like the bit about frogs.
        - Try to make your point more concisely.""";

    Path exportDirectory;
    List<String> headings;
    FeedbackDocument doc, doc2;

    @BeforeEach
    void setup() throws IOException {
        headings = new ArrayList<>(INITIAL_HEADINGS);
        exportDirectory = Files.createTempDirectory("FeedbackHelperTest");
        doc = new FeedbackDocument(STUDENT_ID, headings);
        doc2 = new FeedbackDocument(STUDENT_ID2, headings);
    }

    @Test
    void initialGrade() {
        assertEquals(0.0, doc.getGrade());
    }

    @Test
    void setGrade() {
        doc.setGrade(15.0);
        assertEquals(15.0, doc.getGrade());
    }

    @Test
    void writeToSection() {
        doc.setSectionContents("Report quality", EXAMPLE_TEXT);
        assertEquals(EXAMPLE_TEXT, doc.getSectionContents("Report quality"));
        assertEquals(EXAMPLE_TEXT.length(), doc.length());
    }

    @Test
    void emptySection() {
        assertEquals("", doc.getSectionContents("Report quality"));
    }

    @Test
    void writeBadSection() {
        assertThrows(IllegalArgumentException.class, () -> doc.setSectionContents("Not A Section", EXAMPLE_TEXT));
    }

    @Test
    void length() {
        doc.setSectionContents("Report quality", EXAMPLE_TEXT);
        doc.setSectionContents("Code", EXAMPLE_TEXT);
        assertEquals(EXAMPLE_TEXT.length() * 2, doc.length());
    }

    @Test
    void student() {
        assertEquals(STUDENT_ID, doc.getStudentId());
    }

    @Test
    void headingsExternalList() {
        assertSame(headings, doc.getHeadings());
    }

    @Test
    void editHeading() {
        String previous = "Overall";
        String next = "General comments";

        // Set some text
        doc.setSectionContents("Overall", EXAMPLE_TEXT);

        // Headings are update externally
        int headingPosition = headings.indexOf(previous);
        headings.set(headingPosition, next);

        // Edit the heading in here
        doc.editHeading("Overall", "General comments");

        // Text should match
        assertEquals(EXAMPLE_TEXT, doc.getSectionContents("General comments"));

        // Original heading is invalid
        assertThrows(IllegalArgumentException.class, () -> doc.getSectionContents("Overall"));
    }

    @Test
    void exportEmpty() throws IOException {
        doc.setSectionContents("Code", EXAMPLE_TEXT);
        doc.export(exportDirectory, STYLE);
        String expectedContents = """
                # Code

                - Could do with improvement.
                - I like the bit about frogs.
                - Try to make your point more concisely.


                # Report quality


                # Overall


                """;
        Path expectedFile = exportDirectory.resolve(STUDENT_ID + ".txt");
        String fileText = Files.readString(expectedFile);
        assertEquals(expectedContents, fileText);
    }

    @Test
    void compareLess() {
        assertTrue(doc.compareTo(doc2) < 0);
    }

    @Test
    void compareMore() {
        assertTrue(doc2.compareTo(doc) > 0);
    }

    @Test
    void compareEqual() {
        assertEquals(0, doc2.compareTo(doc2));
    }
}
