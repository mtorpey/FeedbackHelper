package uk.myoung.feedbackhelper.view.feedbackscreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import uk.myoung.feedbackhelper.model.StudentId;

/**
 * Preview Panel Class.
 */
public class StudentList extends JList<String> {

    private DefaultListModel<String> listModel;

    private List<StudentId> students;
    private List<Double> grades;
    private List<Long> charCounts;
    private List<Boolean> locked;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param onSelectStudent Callback for when a student is selected.
     */
    public static StudentList create(Consumer<StudentId> onSelectStudent) {
        var studentList = new StudentList();

        // Set up the list model
        studentList.listModel = new DefaultListModel<String>();
        studentList.setModel(studentList.listModel);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set up data structures for the information that might be displayed
        studentList.students = new ArrayList<>();
        studentList.grades = new ArrayList<>();
        studentList.charCounts = new ArrayList<>();
        studentList.locked = new ArrayList<>();

        // Handle list selection using the callback
        studentList.addListSelectionListener(e -> {
            onSelectStudent.accept(studentList.students.get(studentList.getSelectedIndex()));
        });

        // Display!
        studentList.setVisible(true);

        return studentList;
    }

    /** Add the given student to the list. */
    public void addStudent(StudentId studentId, double grade, long charCount, boolean isLocked) {
        // Find insertion point
        int pos = -Collections.binarySearch(students, studentId) - 1;

        // Insert into all three lists
        students.add(pos, studentId);
        grades.add(pos, grade);
        charCounts.add(pos, charCount);
        locked.add(pos, isLocked);

        // Add the item to the visible list
        listModel.add(pos, entryString(pos));
    }

    private String entryString(int pos) {
        StringBuilder out = new StringBuilder();
        
        // Marked as done
        if (locked.get(pos)) {
            out.append("✓ ");
        }
        
        // Student ID
        out.append(students.get(pos).toString());

        // Grade
        double grade = grades.get(pos);
        if (grade > 0.0) {
            out.append(" – " + grade);
        }

        // Character count
        long count = charCounts.get(pos);
        if (count > 0) {
            out.append(" – " + count + " chars");
        }
        return out.toString();
    }

    /** Selects the given student in the list, without triggering any events. */
    public void selectStudent(StudentId studentId) {
        setSelectedIndex(Collections.binarySearch(students, studentId));
    }

    /**
     * Update a student's grade in the display.
     *
     * @param heading The heading of the preview box to update.
     * @param grade The new grade
     */
    public void updateGrade(StudentId studentId, double grade) {
        int pos = Collections.binarySearch(students, studentId);
        grades.set(pos, grade);
        listModel.set(pos, entryString(pos));
    }

    /**
     * Update a student's character count in the display.
     *
     * @param heading The heading of the preview box to update.
     * @param charCount The new character count.
     */
    public void updateLength(StudentId studentId, long charCount) {
        int pos = Collections.binarySearch(students, studentId);
        charCounts.set(pos, charCount);
        listModel.set(pos, entryString(pos));
    }

    /**
     * Update whether a student is "marked as done" in the display.
     *
     * @param heading The heading of the preview box to update.
     * @param locked Whether this submission is now locked.
     */
    public void updateLocked(StudentId studentId, boolean locked) {
        int pos = Collections.binarySearch(students, studentId);
        this.locked.set(pos, locked);
        listModel.set(pos, entryString(pos));
    }
}
