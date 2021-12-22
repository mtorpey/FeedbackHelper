package view;

import controller.IAppController;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Feedback Box Class.
 */
public class FeedbackBox extends JPanel {

    // Class variable
    private static final int ENTER_KEY = 10;
    private static final String NEWLINE = "\n";

    // Instance variables
    private final IAppController controller;
    private String heading;
    private JLabel headingLabel;
    private JTextArea textArea;
    private List<String> currentBoxContents;
    private List<String> previousBoxContents;

    /**
     * Constructor.
     *
     * @param controller The controller.
     * @param heading    The heading the feedback box is for.
     */
    public FeedbackBox(IAppController controller, String heading) {
        // Store heading
        this.heading = heading;
        this.controller = controller;

        // Create lists to store old and new box contents
        this.currentBoxContents = new ArrayList<String>();
        this.previousBoxContents = new ArrayList<String>();

        // Setup components
        setupLabel();
        setupTextArea();

        // Layout components from top to bottom on this panel
        this.setLayout(new BorderLayout());

        // Add components to the panel
        this.add(this.headingLabel, BorderLayout.PAGE_START);
        this.add(this.textArea, BorderLayout.CENTER);

        // Add some padding to the bottom on the panel and make it visible
        this.setBorder(BorderCreator.createEmptyBorderLeavingTop(BorderCreator.PADDING_20_PIXELS));
        this.setVisible(true);
    }

    /**
     * Get the heading string.
     *
     * @return The heading string.
     */
    public String getHeading() {
        return this.heading;
    }

    /**
     * Setup the heading label.
     */
    private void setupLabel() {
        this.headingLabel = new JLabel(this.heading, SwingConstants.LEFT);
        this.headingLabel.setBorder(BorderCreator.createEmptyBorderBottomOnly(BorderCreator.PADDING_10_PIXELS));
    }

    /**
     * Set up the text area
     */
    private void setupTextArea() {
        // Create the text area and set properties
        this.textArea = new JTextArea();
        this.textArea.setRows(10);
        this.textArea.setBackground(Configuration.COLOR_FEEDBACK_BOX_BACKGROUND);
        this.textArea.setEditable(true);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setBorder(BorderCreator.createAllSidesEmptyBorder(BorderCreator.PADDING_10_PIXELS));

        // Listen for enter press
        this.textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == ENTER_KEY) {
                    captureState();
                    controller.updatePhrases(heading, previousBoxContents, currentBoxContents);
                    controller.managePhraseLinks(heading, previousBoxContents, currentBoxContents);
                    controller.saveFeedbackDocument(controller.getCurrentDocumentInView());
                    insertLineMarkerForNewLine();
                }
            }
        });

        // Listen for clicks on the text area
        this.textArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // Notify model what is in focus
                controller.updateCurrentHeadingBeingEdited(heading);

                // If heading being edited has changed, show all the phrases for that heading
                if (controller.headingChanged()) {
                    controller.resetPhrasesPanel(PhraseType.FREQUENTLY_USED);
                    controller.showPhrasesForHeading(heading);
                }

                // Check if we need to insert a new line
                if (textArea.getText().isEmpty()) {
                    insertLineMarkerForNewLine();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                controller.saveFeedbackDocument(controller.getCurrentDocumentInView());
            }
        });
    }

    /**
     * Get the text area.
     *
     * @return The text area.
     */
    public JTextArea getTextArea() {
        return this.textArea;
    }

    /**
     * Capture the state of the feedback box and update the old and new box content lists.
     */
    private void captureState() {
        // Clear previous contents and store most recent contents
        this.previousBoxContents = new ArrayList<>(this.currentBoxContents);

        // Store the realtime contents (and remove line marker for storage)
        this.currentBoxContents = Arrays.asList(this.textArea.getText().split(NEWLINE));
        this.currentBoxContents = this.currentBoxContents.stream()
                .map(String::trim)
                .filter(line -> line.startsWith(this.controller.getLineMarker()))
                .map(line -> line.replace(this.controller.getLineMarker(), ""))
                .collect(Collectors.toList());
    }

    /**
     * Set the text area text.
     *
     * @param data The data to display in the text area.
     */
    public void setTextAreaText(String data) {
        // Store the realtime contents (and remove line marker for storage)
        this.currentBoxContents = Arrays.asList(data.split(NEWLINE));
        this.currentBoxContents = this.currentBoxContents.stream()
                .map(String::trim)
                .filter(line -> line.startsWith(this.controller.getLineMarker()))
                .map(line -> line.replace(this.controller.getLineMarker(), ""))
                .collect(Collectors.toList());
        this.textArea.setText(data);
    }

    /**
     * Register the feedback box with the popup editing menu.
     *
     * @param editingPopupMenu The editing menu to register with.
     */
    public void registerPopupMenu(EditingPopupMenu editingPopupMenu) {
        editingPopupMenu.registerFeedbackBox(this);
    }

    /**
     * Insert a phrase into the feedback box.
     *
     * @param phrase The phrase to insert.
     */
    public void insertPhrase(String phrase) {
        // Insert phrase
        int caretPos = this.textArea.getCaretPosition();
        this.textArea.insert(phrase + NEWLINE, caretPos);

        // Save new state
        captureState();
        this.controller.updatePhrases(this.heading, this.previousBoxContents, this.currentBoxContents);
        this.controller.managePhraseLinks(this.heading, this.previousBoxContents, this.currentBoxContents);
        insertLineMarkerForNewLine();
    }

    /**
     * Insert a line marker at the beginning of a new line.
     */
    private void insertLineMarkerForNewLine() {
        int caretPos = this.textArea.getCaretPosition();
        this.textArea.insert(this.controller.getLineMarker(), caretPos);
    }

}
