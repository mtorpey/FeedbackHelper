package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import model.StudentId;

/**
 * Editor Panel Class.
 */
public class EditorPanel extends VerticalScrollablePanel {

    // Instance variables
    private String titleText;
    private JLabel titleLabel;
    private JPanel feedbackBoxesPanel;
    private List<FeedbackBox> feedbackBoxes; // TODO: remove?
    private GradeBox gradeBox;
    private List<String> headings;
    private Map<String, FeedbackBox> headingAndFeedbackBox;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param titleText The title text for the editor panel.
     * @param headings The headings of the feedback boxes to create.
     * @param onSwitchSection Callback to be invoked when user selects a new section.
     * @param onUpdateText Callback to be invoked when user modifies text in a section.
     */
    public static EditorPanel create(
        String titleText,
        List<String> headings,
        String lineMarker,
        Consumer<String> onSwitchSection,
        BiConsumer<String, String> onEditHeading,
        BiConsumer<String, String> onUpdateText,
        Consumer<Double> onUpdateGrade
    ) {
        EditorPanel panel = new EditorPanel(titleText, headings);

        // Setup data structures
        panel.feedbackBoxes = new ArrayList<FeedbackBox>();
        panel.headingAndFeedbackBox = new HashMap<String, FeedbackBox>();

        // Layout components from top to bottom
        panel.setLayout(new BorderLayout());
        panel.setupTitle();
        panel.setupFeedbackBoxesPanel();
        panel.setupFeedbackBoxes(lineMarker, onSwitchSection, onEditHeading, onUpdateText);
        panel.setupGradeBox(onUpdateGrade);

        // Set visibility
        panel.setVisible(true);

        return panel;
    }

    private EditorPanel(String titleText, List<String> headings) {
        this.titleText = titleText;
        this.headings = headings;
    }

    /**
     * Setup the feedback boxes panel.
     */
    private void setupFeedbackBoxesPanel() {
        this.feedbackBoxesPanel = new JPanel();
        this.feedbackBoxesPanel.setLayout(new BoxLayout(this.feedbackBoxesPanel, BoxLayout.PAGE_AXIS));
    }

    /**
     * Setup the grade box.
     */
    private void setupGradeBox(Consumer<Double> onUpdateGrade) {
        this.gradeBox = GradeBox.create(onUpdateGrade);
        this.add(this.gradeBox, BorderLayout.PAGE_END);
    }

    /**
     * Set the title label.
     *
     * @param titleText The title label text.
     */
    private void setTitleLabel(String titleText) {
        this.titleLabel.setText(titleText);
    }

    /**
     * Setup the title.
     */
    private void setupTitle() {
        this.titleLabel = new JLabel(this.titleText);
        this.titleLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 20));
        this.titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        this.titleLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        this.add(this.titleLabel, BorderLayout.PAGE_START);
    }

    /**
     * Setup the feedback boxes.
     *
     */
    private void setupFeedbackBoxes(
        String lineMarker,
        Consumer<String> onSwitchSection,
        BiConsumer<String, String> onEditHeading,
        BiConsumer<String, String> onUpdateText
    ) {
        this.headings.forEach(heading -> {
            FeedbackBox feedbackBox = FeedbackBox.create(
                heading,
                lineMarker,
                onSwitchSection,
                onEditHeading,
                onUpdateText
            );
            this.feedbackBoxes.add(feedbackBox);
            this.headingAndFeedbackBox.put(heading, feedbackBox);
            this.feedbackBoxesPanel.add(feedbackBox);
        });

        this.feedbackBoxesPanel.setVisible(true);
        this.add(this.feedbackBoxesPanel, BorderLayout.CENTER);
    }

    /**
     * Reset the feedback boxes with a new set of headings.
     *
     * @param headings The headings of the feedback boxes to reset.
     */
    public void updateHeadings(List<String> newHeadings) {
        for (int position = 0; position < headings.size(); position++) {
            FeedbackBox feedbackBox = this.feedbackBoxes.get(position);
            String previousHeading = headings.get(position);

            // Change the heading
            String currentHeading = newHeadings.get(position);
            this.headingAndFeedbackBox.remove(previousHeading);
            this.headingAndFeedbackBox.put(currentHeading, feedbackBox);
            feedbackBox.setHeading(currentHeading);

            // Reconcile the heading interface
            JTextField headingField = feedbackBox.getHeadingField();
            headingField.setText(currentHeading);
        }
        this.headings = newHeadings;
    }

    /**
     * Register the feedback boxes with the popup editing menu.
     *
     * @param editingPopupMenu The editing menu to register with.
     */
    public void registerPopupMenu(EditingPopupMenu editingPopupMenu) {
        this.feedbackBoxes.forEach(feedbackBox -> feedbackBox.registerPopupMenu(editingPopupMenu));
    }

    /** Update this panel to display the given student ID. */
    public void setStudentId(StudentId studentId) {
        setTitleLabel("Document for: " + studentId);

        // Refresh the UI
        revalidate();
        repaint();
    }

    /**
     * Set the contents of a particular section
     *
     * @param heading The heading of the section to be set.
     * @param contents The text to be entered.
     */
    public void setSectionContents(String heading, String contents) {
        FeedbackBox box = headingAndFeedbackBox.get(heading);
        box.setContents("");
        box.setContents(contents);

        // Refresh the UI
        revalidate();
        repaint();
    }

    /** Update this panel to display the given grade. */
    public void setGrade(double grade) {
        gradeBox.setGrade(grade);

        // Refresh the UI
        revalidate();
        repaint();
    }

    /**
     * Insert a phrase into the feedback box.
     *
     * @param heading The heading of the feedback box to insert the phrase into.
     * @param phrase The text to insert.
     */
    public void insertPhraseIntoFeedbackBox(String heading, String phrase) {
        this.headingAndFeedbackBox.get(heading).insertPhrase(phrase);
    }

    /**
     * Save the data from each feedback box and return it as a map.
     *
     * @return A map of the headings and their data.
     */
    public Map<String, String> getSections() {
        Map<String, String> headingsAndData = new HashMap<String, String>();
        this.feedbackBoxes.forEach(feedbackBox -> {
            headingsAndData.put(feedbackBox.getHeading(), feedbackBox.getContents());
        });

        return headingsAndData;
    }

    /**
     * Get feedback boxes.
     *
     * @return The feedback boxes.
     */
    public List<FeedbackBox> getFeedbackBoxes() {
        return this.feedbackBoxes;
    }

    /**
     * Get the grade.
     *
     * @return The grade value.
     */
    public double getGrade() {
        return this.gradeBox.getGrade();
    }
}
