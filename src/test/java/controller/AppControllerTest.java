package controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import configuration.UserPreferences;
import model.StudentId;

class AppControllerTest {

    // Test data
    final String TITLE = "CS2101-P2";
    final List<String> INITIAL_HEADINGS = List.of("Code", "Report quality", "Overall");
    final List<String> STUDENTS_STRINGS = List.of("090003554", "112110331", "250001331", "Janey", "mike_york12");
    final String HEADINGS_LIST = "Code\nReport quality\n\nOverall\n";
    final String STUDENT_LIST = "090003554,Janey, 250001331, 112110331, \nmike_york12";
    final String EXAMPLE_TEXT = """
        - Could do with improvement.
        - I like the bit about frogs.
        - Try to make your point more concisely.""";

    AppController controller;

    // Saving and restoring user prefs so tests don't mangle them
    static Path lastOpened;

    @BeforeAll
    static void saveUserPrefs() {
        lastOpened = UserPreferences.getLastOpenedAssignmentPath();
    }

    @AfterAll
    static void restoreUserPrefs() {
        UserPreferences.setLastOpenedAssignment(lastOpened);
    }

    @BeforeEach
    void setup() throws IOException {
        controller = new AppController();
    }

    @Test
    void noAssignment() {
        assertFalse(controller.hasAssignment());
    }

    @Nested
    class WithAssignmentCreated {

        // Test variables
        List<String> headings;
        List<StudentId> students;
        Path studentListFile;
        Path assignmentDirectory;

        MockListener assignmentListener;

        @BeforeEach
        void setup() throws IOException {
            // Create dependencies for assignment
            headings = new ArrayList<>(INITIAL_HEADINGS);
            studentListFile = Files.createTempFile("FeedbackHelperTest-", "-students.txt");
            Files.writeString(studentListFile, STUDENT_LIST);
            assignmentDirectory = Files.createTempDirectory("FeedbackHelperTest-assignment-");

            // Create the mock listener
            assignmentListener = new MockListener();

            // Create the assignment
            controller.createAssignment(TITLE, HEADINGS_LIST, studentListFile, assignmentDirectory, "", "=", 1, "•");

            // Register a listener for receiving changes
            controller.registerWithModel(assignmentListener); // triggers a save
        }

        @AfterEach
        void cleanup() throws IOException, InterruptedException {
            assignmentListener.joinAllThreads();
            Files.deleteIfExists(studentListFile);
            // Delete recursively
            Files.walkFileTree(
                assignmentDirectory,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                }
            );
        }

        @Test
        void hasAssignment() {
            assertTrue(controller.hasAssignment());
        }

        @Test
        void saveLocation() throws InterruptedException {
            controller.saveAssignment();
            assignmentListener.joinAllThreads();
            Path expected = assignmentDirectory.resolve(TITLE + ".fht");
            assertTrue(Files.isRegularFile(expected));
        }

        @Test
        void cannotCreateSecondAssignment() {
            assertThrows(RuntimeException.class, () ->
                controller.createAssignment(TITLE, HEADINGS_LIST, studentListFile, assignmentDirectory, "", "=", 1, "•")
            );
        }
    }
}
