package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.AppController;

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
    private AppController controller;
    private String heading;
    private String lineMarker;
    private Consumer<String> onSwitchSection;
    private BiConsumer<String, String> onUpdateText;

    /**
     * Constructor.
     *
     * @param heading The heading the feedback box is for.
     * @param onSwitchSection Callback to be invoked when user selects a new section
     * @param onUpdateText Callback to be invoked when the text is updated by the user
     */
    public FeedbackBox(
        AppController controller,
        String heading,
        String lineMarker,
        Consumer<String> onSwitchSection,
        BiConsumer<String, String> onUpdateText
    ) {
        // Store heading
        this.controller = controller;
        this.heading = heading;
        this.lineMarker = lineMarker;
        this.onSwitchSection = onSwitchSection;
        this.onUpdateText = onUpdateText;

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
        this.headingField = new JTextField(this.heading, this.heading.length());
        this.headingButton = new JButton(EDIT_SYMBOL);

        // Set heading font
        Font currentFont = getFont();
        this.headingField.setFont(new Font(currentFont.getFontName(), Font.BOLD, currentFont.getSize()));

        // Add to the panel
        this.headingPanel.add(this.headingField, BorderLayout.WEST);
        this.headingPanel.add(this.headingButton, BorderLayout.EAST);

        // Create a function consumer
        Consumer<Boolean> edit = state -> {
            headingField.setEditable(state);
            headingField.setOpaque(state);

            // Set editable
            if (state) {
                headingButton.setText(FeedbackBox.FINISH_SYMBOL);
                headingField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                headingField.setBackground(Color.WHITE);
            } else {
                // Stop editing
                headingButton.setText(FeedbackBox.EDIT_SYMBOL);
                //headingField.setBorder(BorderCreator.createEmptyBorderBottomOnly(BorderCreator.PADDING_10_PIXELS))
                headingField.setBorder(BorderFactory.createEmptyBorder());
                headingField.setBackground(new Color(0, 0, 0, 0));
            }

            headingPanel.revalidate();
        };

        // Listen to changed text field
        this.headingField.getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    String currentHeading = headingField.getText();
                    headingField.setColumns(currentHeading.length());
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
                    controller.editHeading(currentHeading, newHeading);
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
        this.textArea.setRows(10);
        this.textArea.setEditable(true);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setBorder(BorderCreator.createAllSidesEmptyBorder(BorderCreator.PADDING_10_PIXELS));

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
                    textArea.setCaretColor(textArea.getForeground()); // TODO: bad idea?

                    // Check if we need to insert a new line
                    if (textArea.getText().isEmpty()) {
                        insertLineMarkerForNewLine();
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    updateFeedback();
                }
            }
        );
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
     * Insert a phrase into the feedback box.
     *
     * @param phrase The phrase to insert.
     */
    public void insertPhrase(String phrase) {
        // Insert phrase
        int caretPos = this.textArea.getCaretPosition();
        this.textArea.insert(phrase + NEWLINE, caretPos);

        // Save new state
        updateFeedback();
        insertLineMarkerForNewLine();
    }

    /**
     * Insert a line marker at the beginning of a new line.
     */
    private void insertLineMarkerForNewLine() {
        int caretPos = this.textArea.getCaretPosition();
        this.textArea.insert(lineMarker, caretPos);
    }
}
