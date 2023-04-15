package view;

import controller.IAppController;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
    private static final String EDIT_SYMBOL = "✎";
    private static final String FINISH_SYMBOL = "✔";

    // Instance variables
    private final IAppController controller;
    private String heading;
    private JPanel headingPanel;
    private JTextArea headingField;
    private JButton headingButton;
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
        setupPanel();
        setupTextArea();

        // Layout components from top to bottom on this panel
        this.setLayout(new BorderLayout());

        // Add components to the panel
        this.add(this.headingPanel, BorderLayout.PAGE_START);
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
     * Set the heading string.
     */
    public void setHeading(String heading) {
        this.heading = heading;
    }

    /**
     * Setup the heading panel.
     */
    private void setupPanel() {
        this.headingPanel = new JPanel(new BorderLayout());
        this.headingPanel.setBorder(BorderCreator.createEmptyBorderBottomOnly(BorderCreator.PADDING_10_PIXELS));

        // Create components
        this.headingField = new JTextArea(this.heading, 1, this.heading.length());
        this.headingField.setEditable(false);
        this.headingButton = new JButton(EDIT_SYMBOL); 

        // Add to the panel
        this.headingPanel.add(this.headingField, BorderLayout.WEST);
        this.headingPanel.add(this.headingButton, BorderLayout.EAST);

        // Listen to renaming heading sections
        this.headingButton.addActionListener(a -> {
            if (headingButton.getText().equals(FINISH_SYMBOL)) {
                // Stop editing
                headingField.setEditable(false); 
                headingButton.setText(EDIT_SYMBOL); 

                // Update heading
                String currentHeading = getHeading();
                String newHeading = headingField.getText();

                if (!currentHeading.equals(newHeading)) {  
                    // Save new heading
                    controller.updateHeading(currentHeading, newHeading);
                    setHeading(newHeading);

                    // Add phrases for this heading
                    controller.updatePhrases(newHeading, previousBoxContents, currentBoxContents);
                    controller.managePhraseLinks(newHeading, previousBoxContents, currentBoxContents);   
                }

            } else { // Set editable
                headingField.setEditable(true); 
                headingButton.setText(FeedbackBox.FINISH_SYMBOL); 
            }
        });  
    }

    /**
     * Set up the text area
     */
    private void setupTextArea() {
        // Create the text area and set properties
        this.textArea = new JTextArea();
        this.textArea.setRows(10);
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

                // Set the caret colour (in some themes it might be hard to see)
                textArea.setCaretColor(textArea.getForeground());

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
