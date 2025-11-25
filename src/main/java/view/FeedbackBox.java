package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Feedback Box Class.
 */
public class FeedbackBox extends JPanel {

    // Class variable
    private static final int ENTER_KEY = 10;
    private static final String NEWLINE = "\n";
    private static final String EDIT_SYMBOL = "✎";
    private static final String FINISH_SYMBOL = "✔";

    // Swing instances
    private JPanel headingPanel;
    private JTextField headingField;
    private JButton headingButton;
    private JTextArea textArea;

    // Data instances
    private String heading;
    private String lineMarker;
    private Consumer<String> onSwitchSection;
    private BiConsumer<String, String> onEditHeading;
    private BiConsumer<String, String> onUpdateText;

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param heading The heading the feedback box is for.
     * @param lineMarker The bullet point string to appear at the beginning of a new line.
     * @param onSwitchSection Callback to be invoked when user selects a new section.
     * @param onEditHeading Callback to be invoked when user edits a heading.
     * @param onUpdateText Callback to be invoked when the text is updated by the user.
     */
    public static FeedbackBox create(
        String heading,
        String lineMarker,
        Consumer<String> onSwitchSection,
        BiConsumer<String, String> onEditHeading,
        BiConsumer<String, String> onUpdateText
    ) {
        FeedbackBox box = new FeedbackBox(heading, lineMarker, onSwitchSection, onEditHeading, onUpdateText);

        // Setup components
        box.setupPanel();
        box.setupTextArea();

        // Layout components from top to bottom on this panel
        box.setLayout(new BorderLayout());

        // Add components to the panel
        box.add(box.headingPanel, BorderLayout.PAGE_START);
        box.add(box.textArea, BorderLayout.CENTER);

        // Add some padding to the bottom on the panel and make it visible
        box.setBorder(BorderCreator.emptyBorderMedium());
        box.setVisible(true);

        return box;
    }

    private FeedbackBox(
        String heading,
        String lineMarker,
        Consumer<String> onSwitchSection,
        BiConsumer<String, String> onEditHeading,
        BiConsumer<String, String> onUpdateText
    ) {
        super();
        this.heading = heading;
        this.lineMarker = lineMarker;
        this.onSwitchSection = onSwitchSection;
        this.onEditHeading = onEditHeading;
        this.onUpdateText = onUpdateText;
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
        //this.headingPanel.setBorder(BorderCreator.emptyBorder5Pixels());

        // Create components
        this.headingField = new JTextField(this.heading);
        this.headingButton = new JButton(EDIT_SYMBOL);

        // Set heading font
        headingField.setFont(Configuration.getSubtitleFont());
        headingButton.setFont(Configuration.getSubtitleFont());

        // Add to the panel
        this.headingPanel.add(this.headingField, BorderLayout.WEST);
        this.headingPanel.add(this.headingButton, BorderLayout.EAST);

        // Create a function consumer
        Consumer<Boolean> edit = state -> {
            headingField.setEditable(state);
            headingField.setOpaque(state);
            headingField.setFocusable(state);
            headingField.repaint();

            // Set editable
            if (state) {
                headingButton.setText(FeedbackBox.FINISH_SYMBOL);
                headingField.setBorder(BorderCreator.textAreaBorder());
            } else {
                // Stop editing
                headingButton.setText(FeedbackBox.EDIT_SYMBOL);
                headingField.setBorder(BorderCreator.emptyBorderSmall());
            }

            headingPanel.revalidate();
        };

        // Listen to changed text field
        this.headingField.getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    //String currentHeading = headingField.getText();
                    //headingField.setColumns(currentHeading.length());
                    headingPanel.revalidate();
                }

                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }

                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
            }
        );

        // Listen to renaming heading sections
        this.headingButton.addActionListener(a -> {
            if (headingButton.getText().equals(FINISH_SYMBOL)) {
                edit.accept(false);

                // Update heading
                String currentHeading = getHeading();
                String newHeading = headingField.getText();

                if (!currentHeading.equals(newHeading)) {
                    // Change heading
                    setHeading(newHeading);
                    // Save new heading
                    onEditHeading.accept(currentHeading, newHeading);
                }
            } else {
                edit.accept(true);
            }
        });

        // Disable the editable header
        edit.accept(false);
    }

    /**
     * Get the heading field.
     */
    public JTextField getHeadingField() {
        return this.headingField;
    }

    /**
     * Set up the text area
     */
    private void setupTextArea() {
        // Create the text area and set properties
        this.textArea = new JTextArea();
        this.textArea.setRows(3);
        this.textArea.setEditable(false);
        this.textArea.setFocusable(false);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setMinimumSize(new Dimension(0, 0));
        this.textArea.setBorder(BorderCreator.textAreaBorder());

        // Listen for enter press
        this.textArea.addKeyListener(
            new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == ENTER_KEY) {
                        updateFeedback();
                        insertLineMarkerForNewLine();
                    }
                }
            }
        );

        // Listen for clicks on the text area
        this.textArea.addFocusListener(
            new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    // Callback that we switch sections
                    onSwitchSection.accept(heading);

                    // Set the caret colour (in some themes it might be hard to see)
                    textArea.setCaretColor(textArea.getForeground());

                    // Check if we need to insert a new line
                    String text = textArea.getText();
                    boolean atEnd = textArea.getCaretPosition() == text.length();
                    if (
                        text.isEmpty() || (!text.endsWith(lineMarker) && atEnd)
                    ) {
                        insertLineMarkerForNewLine();
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    trimText();                    
                    updateFeedback();
                }
            }
        );
    }

    public void enableTextArea() {
        System.out.println("Text area enabled");
        textArea.setEditable(true);
        textArea.setFocusable(true);
        repaint();
    }

    /** Send the current feedback to the model. */
    private void updateFeedback() {
        onUpdateText.accept(heading, textArea.getText());
    }

    /** Get the text from inside this box. */
    public String getContents() {
        return textArea.getText();
    }

    /**
     * Get the text area inside this component.
     *
     * Note: it would be better if we could conceal this internal object.
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    /**
     * Set the text area text.
     *
     * @param data The data to display in the text area.
     */
    public void setContents(String data) {
        textArea.setText(data);
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
     * Insert a phrase into the feedback box, at the end.
     *
     * @param phrase The phrase to insert.
     */
    public void insertPhrase(String phrase) {
        // Insert phrase
        String text = textArea.getText();
        if (!text.endsWith(lineMarker)) {
            if (!(text.isEmpty() || text.endsWith(NEWLINE))) {
                textArea.append(NEWLINE);
            }
            textArea.append(lineMarker);
        }
        textArea.append(phrase + NEWLINE);

        // Save new state
        updateFeedback();
    }

    /**
     * Insert a line marker at the beginning of a new line.
     */
    private void insertLineMarkerForNewLine() {
        int caretPos = this.textArea.getCaretPosition();
        String text = textArea.getText();
        if (!text.isEmpty() && !text.substring(0, caretPos).endsWith(NEWLINE)) {
            textArea.insert(NEWLINE, caretPos);
            caretPos += NEWLINE.length();
            textArea.setCaretPosition(caretPos);
        }
        textArea.insert(lineMarker, caretPos);
    }

    /** Trim the text in the box on leaving it. */
    public void trimText() {
        String text = textArea.getText();
        while (text.endsWith(lineMarker)) {
            text = text.substring(0, text.length() - lineMarker.length());
        }
        text = text.trim();
        textArea.setText(text);
    }
}
