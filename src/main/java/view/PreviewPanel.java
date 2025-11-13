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
    private List<Long> wordCounts;

    /**
     * Constructor.
     *
     * @param onSelectStudent Callback for when a student is selected.
     */
    public PreviewPanel(Consumer<StudentId> onSelectStudent) {
        // Do JList stuff
        super();
        // Set up the list model
        listModel = new DefaultListModel<String>();
        setModel(listModel);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set up data structures for the information that might be displayed
        students = new ArrayList<>();
        grades = new ArrayList<>();
        wordCounts = new ArrayList<>();

        // Handle list selection using the callback
        addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                onSelectStudent.accept(students.get(getSelectedIndex()));
            }
        });

        // Display!
        this.setVisible(true);
    }

    /** Add the given student to the list. */
    public void addStudent(StudentId studentId, double grade, long wordCount) {
        // Find insertion point
        int pos = -Collections.binarySearch(students, studentId) - 1;
        System.out.println("pos is " + pos + ", there are " + students.size() + " students.");

        // Insert into all three lists
        students.add(pos, studentId);
        grades.add(pos, grade);
        wordCounts.add(pos, wordCount);

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

        // Word count
        long wordCount = wordCounts.get(pos);
        if (wordCount > 0) {
            out.append(" – " + wordCounts.get(pos) + " words");
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
     * Update a student's word count in the display.
     *
     * @param heading The heading of the preview box to update.
     * @param wordCount The new word count.
     */
    public void updateWordCount(StudentId studentId, long wordCount) {
        int pos = Collections.binarySearch(students, studentId);
        wordCounts.set(pos, wordCount);
        listModel.set(pos, entryString(pos));
    }
}
