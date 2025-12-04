package controller;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.StudentId;

class FeedbackDocumentTest {

    final List<String> INITIAL_HEADINGS = List.of("Code", "Report quality", "Overall");
    final List<String> STUDENTS_STRINGS = List.of("090003554", "112110331", "250001331", "Janey", "mike_york12");
    final String EXAMPLE_TEXT = """
        - Could do with improvement.
        - I like the bit about frogs.
        - Try to make your point more concisely.""";

    List<String> headings;
    List<StudentId> students;
    Path assignmentDirectory;

    AppController controller;

    @BeforeEach
    void setup() throws IOException {
        headings = new ArrayList<>(INITIAL_HEADINGS);
        students = new ArrayList<>(STUDENTS_STRINGS.stream().map(StudentId::new).toList());
        assignmentDirectory = Files.createTempDirectory("FeedbackHelperTest");
        controller = new AppController();
    }

    @Test
    void noAssignment() {
        assertFalse(controller.hasAssignment());
    }
}
