package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import controller.AppController;

/**
 * Document Viewer Class.
 */
public class DocumentViewer extends JFrame {

    // Instance variables
    protected AppController controller;
    protected JPanel documentPanel;
    protected JTextArea textArea;
    protected JLabel titleLabel;

    /**
     * Constructor.
     *
     * @param controller The controller.
     * @param title      The document title.
     */
    public DocumentViewer(AppController controller, String title) {
        this.controller = controller;

        // Setup components
        setupDocumentViewerScreen();
        setupTextArea();
        setupTitleLabel(title);
        setupDocumentPanel();

        // Set visibility
        this.setVisible(true);
    }

    /**
     * Setup the document viewer screen.
     */
    protected void setupDocumentViewerScreen() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(700, 900);
    }

    /**
     * Setup the document panel.
     */
    protected void setupDocumentPanel() {
        this.documentPanel = new JPanel(new BorderLayout());
        this.documentPanel.setBorder(BorderCreator.createAllSidesEmptyBorder(50));
        this.documentPanel.add(this.titleLabel, BorderLayout.PAGE_START);
        this.documentPanel.add(new JScrollPane(this.textArea), BorderLayout.CENTER);
        this.add(this.documentPanel);
    }

    /**
     * Setup the title label.
     *
     * @param title The title of the document.
     */
    protected void setupTitleLabel(String title) {
        this.titleLabel = new JLabel(title);
        this.titleLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 18));
        this.titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        this.titleLabel.setBorder(new EmptyBorder(0, 0, 20, 20));
    }

    /**
     * Setup the text area.
     */
    protected void setupTextArea() {
        this.textArea = new JTextArea();
        this.textArea.setBorder(BorderCreator.createAllSidesEmptyBorder(20));
        this.textArea.setEditable(false);
    }

    /**
     * Display the data in the text area.
     *
     * This doesn't format it particularly well, but it's good enough for a quick summary.
     */
    public void displayData(Map<String, List<String>> data, List<String> dataHeadings) {
        dataHeadings.forEach(heading -> {
            // Heading
            this.textArea.append(heading + "\n");
            this.textArea.append("\n");

            // Section data
            data
                .get(heading)
                .forEach(line -> {
                    this.textArea.append("- " + line);
                    this.textArea.append("\n");
                });

            // End section spacing
            this.textArea.append("\n");
            this.textArea.append("\n");
        });
    }
}
