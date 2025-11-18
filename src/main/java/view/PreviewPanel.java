package view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import model.StudentId;

/**
 * Preview Panel Class.
 */
public class PreviewPanel extends JList<String> {

    private DefaultListModel<String> listModel;

    private List<StudentId> students;
    private List<Double> grades;
    private List<Long> charCounts;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param onSelectStudent Callback for when a student is selected.
     */
    public static PreviewPanel create(Consumer<StudentId> onSelectStudent) {
        var panel = new PreviewPanel();

        // Set up the list model
        panel.listModel = new DefaultListModel<String>();
        panel.setModel(panel.listModel);
        panel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set up data structures for the information that might be displayed
        panel.students = new ArrayList<>();
        panel.grades = new ArrayList<>();
        panel.charCounts = new ArrayList<>();

        // Handle list selection using the callback
        panel.addListSelectionListener(e -> {
            onSelectStudent.accept(panel.students.get(panel.getSelectedIndex()));
        });

        // Display!
        panel.setVisible(true);

        return panel;
    }

    /** Add the given student to the list. */
    public void addStudent(StudentId studentId, double grade, long charCount) {
        // Find insertion point
        int pos = -Collections.binarySearch(students, studentId) - 1;
        System.out.println("pos is " + pos + ", there are " + students.size() + " students.");

        // Insert into all three lists
        students.add(pos, studentId);
        grades.add(pos, grade);
        charCounts.add(pos, charCount);

        // Add the item to the visible list
        listModel.add(pos, entryString(pos));
    }

    private String entryString(int pos) {
        // Student ID
        StringBuilder out = new StringBuilder(students.get(pos).toString());

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
}
