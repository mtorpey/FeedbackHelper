package uk.myoung.feedbackhelper.controller;

import static org.junit.jupiter.api.Assertions.*;

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

import uk.myoung.feedbackhelper.infrastructure.UserPreferences;
import uk.myoung.feedbackhelper.model.AssignmentReadOnly;
import uk.myoung.feedbackhelper.model.Phrase;
import uk.myoung.feedbackhelper.model.StudentId;

class AppControllerTest {

    // Test data
    final String TITLE = "CS2101-P2";
    final List<String> INITIAL_HEADINGS = List.of("Code", "Report quality", "Overall");
    final List<String> STUDENTS_STRINGS = List.of("090003554", "112110331", "250001331", "Janey", "mike_york12");
    final String HEADINGS_LIST = "Code\nReport quality\n\nOverall\n";
    final String STUDENT_LIST = "090003554,Janey, 250001331, 112110331, \nmike_york12";
    final String EXAMPLE_TEXT = """
        • Could do with improvement.
        • I like the bit about frogs.
        • Try to make your point more concisely.""";

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
        Path expectedFht;
        Path expectedExportDirectory;

        MockListener assignmentListener;
        AssignmentReadOnly assignment;

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
            assignment = controller.registerWithModel(assignmentListener); // triggers a save

            // Where we expect files to appear
            expectedFht = assignmentDirectory.resolve(TITLE + ".fht");
            expectedExportDirectory = assignmentDirectory.resolve(TITLE + "-feedback");
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

        void exportAndCheck() {
            controller.exportFeedbackAndGrades();
            assertTrue(assignmentListener.hasEvent("exported", "to " + expectedExportDirectory));
            assertTrue(Files.isDirectory(expectedExportDirectory));
        }

        @Test
        void hasAssignment() {
            assertTrue(controller.hasAssignment());
        }

        @Test
        void saveLocation() throws InterruptedException {
            controller.saveAssignment();
            assignmentListener.joinAllThreads();
            
            assertTrue(Files.isRegularFile(expectedFht));
        }

        @Test
        void cannotCreateSecondAssignment() {
            assertThrows(RuntimeException.class, () ->
                controller.createAssignment(TITLE, HEADINGS_LIST, studentListFile, assignmentDirectory, "", "=", 1, "•")
            );
        }

        @Test
        void updateFeedbackSection() throws IOException {
            StudentId student = new StudentId(STUDENTS_STRINGS.get(2));
            controller.updateFeedbackSection(student, "Report quality", EXAMPLE_TEXT);
            assertTrue(assignmentListener.hasEvent("phraseAdded", "I like the bit about frogs. to Report quality"));

            exportAndCheck();

            // Check file
            Path feedbackFile = expectedExportDirectory.resolve(student.id() + ".txt");
            assertTrue(Files.isRegularFile(feedbackFile));
            assertTrue(Files.readString(feedbackFile).contains(EXAMPLE_TEXT));
        }

        @Test
        void updateGrade() throws IOException {
            StudentId student = new StudentId(STUDENTS_STRINGS.get(1));
            controller.updateGrade(student, 15.5);
            assertTrue(assignmentListener.hasEvent("gradeUpdate", student.id() + ",15.5"));

            exportAndCheck();

            // Check file
            Path gradesFile = expectedExportDirectory.resolve("grades.csv");
            assertTrue(Files.isRegularFile(gradesFile));
            assertTrue(Files.readString(gradesFile).contains(student.id() + ",15.5"));
        }

        @Test
        void addNewStudent() {
            String id = "i_am_late_1996";
            controller.addNewStudent(id);

            assertTrue(assignmentListener.hasEvent("newStudent", id));

            exportAndCheck();

            Path feedbackFile = expectedExportDirectory.resolve(id + ".txt");
            assertTrue(Files.isRegularFile(feedbackFile));
        }

        @Test
        void editHeading() throws IOException {
            String newHeading = "Boring writeup";
            controller.editHeading("Report quality", newHeading);

            assertTrue(assignmentListener.hasEvent("headingsUpdated", ".*" + newHeading + ".*"));

            exportAndCheck();

            Path feedbackFile = expectedExportDirectory.resolve(STUDENTS_STRINGS.get(3) + ".txt");
            assertTrue(Files.isRegularFile(feedbackFile));

            String formattedHeading = newHeading + "\n" + newHeading.replaceAll(".", "=");
            assertTrue(Files.readString(feedbackFile).contains(formattedHeading));
        }

        @Nested
        class WithCustomPhrasesAdded {
            static final String GOOD_PHRASE = "A good attempt meeting nearly all requirements successfully.";
            static final String REASONABLE_PHRASE = "A reasonable attempt addressing some of the requirements.";
            static final String EXCEPTIONAL_PHRASE = "Exceptional achievement.";
            static final String INVALID_PHRASE = " ";

            @BeforeEach
            void setup() {
                controller.addCustomPhrase("Overall", REASONABLE_PHRASE);
                controller.addCustomPhrase("Overall", "A competent attempt addressing most requirements.");
                controller.addCustomPhrase("Overall", INVALID_PHRASE); // not inserted
                controller.addCustomPhrase("Overall", GOOD_PHRASE);
                controller.addCustomPhrase("Overall", "An excellent attempt with no significant defects.");
                controller.addCustomPhrase("Overall", EXCEPTIONAL_PHRASE);
            }

            @Test
            void hasCustomPhrase() {
                assertTrue(assignmentListener.hasEvent("customPhraseAdded", GOOD_PHRASE + " to Overall"));
            }

            @Test
            void noInvalidPhrase() {
                List<Phrase> phrases = assignment.getCustomPhrases("Overall");
                assertNotEquals(INVALID_PHRASE, phrases.get(2).getPhraseAsString());
                assertEquals(5, phrases.size());
            }

            @Test
            void correctPlace() {
                List<Phrase> phrases = assignment.getCustomPhrases("Overall");
                assertEquals(GOOD_PHRASE, phrases.get(2).getPhraseAsString());
            }

            @Test
            void deleteCustomPhrase() {
                controller.deleteCustomPhrase("Overall", GOOD_PHRASE);

                List<Phrase> phrases = assignment.getCustomPhrases("Overall");
                assertNotEquals(GOOD_PHRASE, phrases.get(2).getPhraseAsString());
            }

            @Test
            void moveCustomPhraseUpOne() {
                controller.reorderCustomPhrase("Overall", GOOD_PHRASE, -1);

                List<Phrase> phrases = assignment.getCustomPhrases("Overall");
                assertNotEquals(GOOD_PHRASE, phrases.get(2).getPhraseAsString());
                assertEquals(GOOD_PHRASE, phrases.get(1).getPhraseAsString());
            }

            @Test
            void moveCustomPhraseDownOne() {
                controller.reorderCustomPhrase("Overall", GOOD_PHRASE, +1);

                List<Phrase> phrases = assignment.getCustomPhrases("Overall");
                assertNotEquals(GOOD_PHRASE, phrases.get(2).getPhraseAsString());
                assertEquals(GOOD_PHRASE, phrases.get(3).getPhraseAsString());
            }

            @Test
            void cannotMoveAbove0() {
                controller.reorderCustomPhrase("Overall", REASONABLE_PHRASE, -1);

                List<Phrase> phrases = assignment.getCustomPhrases("Overall");
                assertEquals(REASONABLE_PHRASE, phrases.get(0).getPhraseAsString());
            }

            @Test
            void cannotMoveBelowBottom() {
                controller.reorderCustomPhrase("Overall", EXCEPTIONAL_PHRASE, +1);

                List<Phrase> phrases = assignment.getCustomPhrases("Overall");
                assertEquals(EXCEPTIONAL_PHRASE, phrases.get(4).getPhraseAsString());
                assertEquals(5, phrases.size());
            }

        }

        @Nested
        class LoadedNewAssignment {
            AppController loader;
            MockListener loadedAssignmentListener;
            AssignmentReadOnly loadedAssignment;
            StudentId student;
            
            @BeforeEach
            void modifySaveAndLoad() throws IOException, ClassNotFoundException, ClassCastException, InterruptedException {
                // Modify and save
                student = new StudentId(STUDENTS_STRINGS.get(2));
                controller.updateFeedbackSection(student, "Report quality", EXAMPLE_TEXT);
                controller.updateGrade(student, 15.5);
                controller.addCustomPhrase("Code", "Avoid magic variables.");
                assignmentListener.joinAllThreads();
                controller.saveAssignment();
                assignmentListener.joinAllThreads();

                // Load into new controller
                loader = new AppController();
                loader.loadAssignment(expectedFht);

                // Register
                loadedAssignmentListener = new MockListener();
                loadedAssignment = loader.registerWithModel(loadedAssignmentListener);
            }

            @AfterEach
            void cleanup() throws InterruptedException {
                loadedAssignmentListener.joinAllThreads();
            }

            @Test
            void hasAssignment() {
                assertTrue(loader.hasAssignment());
            }

            @Test
            void hasCustomPhrase() {
                List<Phrase> phrases = loadedAssignment.getCustomPhrases("Code");
                assertEquals(1, phrases.size());
                assertEquals(
                    "Avoid magic variables.",
                    phrases.get(0).getPhraseAsString()
                );
            }

            @Test
            void gradeSet() {
                assertEquals(15.5, loadedAssignment.getGrade(student));
            }

            @Test
            void feedbackSet() {
                String section = loadedAssignment.getSectionContents(student, "Report quality");
                assertEquals(EXAMPLE_TEXT, section);
            }

            
        }
    }
}
