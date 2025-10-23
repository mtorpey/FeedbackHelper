package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.AppController;
import model.FeedbackDocument;

/**
 * Editor Panel Class.
 */
public class EditorPanel extends JPanel {

    // Instance variables
    private final AppController controller;
    private String titleText;
    private JLabel titleLabel;
    private JPanel feedbackBoxesPanel;
    private List<FeedbackBox> feedbackBoxes;
    private GradeBox gradeBox;
    private List<String> headings;
    private Map<String, FeedbackBox> headingAndFeedbackBox;

    /**
     * Constructor.
     *
     * @param controller The controller.
     * @param titleText  The title text for the editor panel.
     * @param headings   The headings of the feedback boxes to create.
     */
    public EditorPanel(AppController controller, String titleText, List<String> headings) {
        // Set data variables
        this.titleText = titleText;
        this.controller = controller;
        this.feedbackBoxes = new ArrayList<FeedbackBox>();
        this.headings = headings;
        this.headingAndFeedbackBox = new HashMap<String, FeedbackBox>();

        // Layout components from top to bottom
        this.setLayout(new BorderLayout());
        this.setupTitle();
        this.setupFeedbackBoxesPanel();
        this.setupFeedbackBoxes();
        this.setupGradeBox();

        // Set visibility
        this.setVisible(true);
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
    private void setupGradeBox() {
        this.gradeBox = new GradeBox(this.controller);
        this.add(this.gradeBox, BorderLayout.PAGE_END);
    }

    /**
     * Set the title label.
     *
     * @param titleText The title label text.
     */
    public void setTitleLabel(String titleText) {
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
    private void setupFeedbackBoxes() {
        this.headings.forEach(heading -> {
            FeedbackBox feedbackBox = new FeedbackBox(this.controller, heading);
            this.feedbackBoxes.add(feedbackBox);
            this.headingAndFeedbackBox.put(heading, feedbackBox);
            this.feedbackBoxesPanel.add(feedbackBox);
        });

        this.feedbackBoxesPanel.setVisible(true);
        this.add(this.feedbackBoxesPanel, BorderLayout.CENTER);
    }

    /**
     * Reset the feedback boxes.
     *
     * @param headings   The headings of the feedback boxes to reset.
     */
    public void resetFeedbackBoxes(List<String> newHeadings) {
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

    /**
     * Set the data from a feedback document.
     *
     * @param feedbackDocument The feedback document to set the data from.
     */
    public void setData(FeedbackDocument feedbackDocument) {
        // Fill up the feedback boxes with the data
        setTitleLabel("Document for: " + feedbackDocument.getStudentId());

        // Set the data for each feedback box
        this.feedbackBoxes.forEach(feedbackBox -> {
            feedbackBox.setTextAreaText("");
            feedbackBox.setTextAreaText(feedbackDocument.getSectionContents(feedbackBox.getHeading()));
        });

        // Set the grade box
        this.gradeBox.setStudentId(feedbackDocument.getStudentId());
        this.gradeBox.setGrade(feedbackDocument.getGrade());

        // Refresh the UI
        revalidate();
        repaint();
    }

    /**
     * Insert a phrase into the feedback box.
     *
     * @param phrase             The phrase to insert.
     * @param feedbackBoxHeading The feedback box to insert the phrase into.
     */
    public void insertPhraseIntoFeedbackBox(String phrase, String feedbackBoxHeading) {
        this.headingAndFeedbackBox.get(feedbackBoxHeading).insertPhrase(phrase);
    }

    /**
     * Save the data from each feedback box and return it as a map.
     *
     * @return A map of the headings and their data.
     */
    public Map<String, String> saveDataAsMap() {
        Map<String, String> headingsAndData = new HashMap<String, String>();
        this.feedbackBoxes.forEach(feedbackBox -> {
            headingsAndData.put(feedbackBox.getHeading(), feedbackBox.getTextArea().getText());
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
