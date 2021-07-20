package view;

import controller.AppController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeedbackBox extends JPanel {

    public String getHeading() {
        return heading;
    }

    private String heading;
    private JLabel headingLabel;
    private JTextArea textPane;
    private EditingPopupMenu editingPopupMenu;
    private AppController controller;

    private List<String> currentBoxContents;
    private List<String> previousBoxContents;

    public FeedbackBox(AppController controller, String heading) {
        // Store heading
        this.heading = heading;
        this.controller = controller;

        // Setup components
        setupLabel();
        setupTextArea();

        // Layout components from top to bottom on this panel
        this.setLayout(new BorderLayout());

        // Add components to the panel
        this.add(headingLabel, BorderLayout.PAGE_START);
        this.add(textPane, BorderLayout.CENTER);

        // Listen for clicks on the text area
        this.textPane.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
//                textPane.setBorder(selectedBorder);
                //controller.displayNewDocument(controller.getCurrentDocInView());
                controller.updateCurrentHeadingBeingEdited(heading);

                if (textPane.getText().isEmpty()) {
                    insertHypenForNewLine();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                controller.saveFeedbackDocument(controller.getCurrentDocInView());
//                textPane.setBorder(unselectedBorder);
            }
        });

        // Add some padding to the bottom on the panel and make it visible
        this.setBorder(new EmptyBorder(0, 20, 20, 20));
        this.setVisible(true);
    }

    private void setupLabel() {
        this.headingLabel = new JLabel(heading, SwingConstants.LEFT);
        this.headingLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
    }

    private void setupTextArea() {
        this.textPane = new JTextArea();
        this.textPane.setRows(10);
        this.textPane.setBackground(Color.WHITE);
        this.textPane.setEditable(true);
        this.textPane.setLineWrap(true);
        this.textPane.setWrapStyleWord(true);
        this.textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == 10){
                    // Clear previous contents and store most recent contents
                    previousBoxContents = new ArrayList<>();
                    previousBoxContents.addAll(currentBoxContents);

                    // Store the realtime contents
                    currentBoxContents = Arrays.asList(textPane.getText().split("\n"));
                    System.out.println("Completed lines : " + currentBoxContents.size());
                    //controller.updatePhrases(heading, previousBoxContents, currentBoxContents);
                    insertHypenForNewLine();
                }
            }
        });
    }

    public JTextArea getTextPane() {
        return this.textPane;
    }

    public void setTextPaneText(String data) {
        this.textPane.setText(data);
    }

    public void registerPopupMenu(EditingPopupMenu editingPopupMenu) {
        editingPopupMenu.registerFeedbackBox(this);
    }

    public void insertPhrase(String phrase) {
        int caretPos = this.textPane.getCaretPosition();
        textPane.insert(phrase, caretPos);
    }

    private void insertHypenForNewLine(){
        int caretPos = textPane.getCaretPosition();
        textPane.insert("- ", caretPos); // When a new line is taken add a " - "
    }
}
