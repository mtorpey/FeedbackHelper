package uk.myoung.feedbackhelper.view;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import uk.myoung.feedbackhelper.infrastructure.ApplicationMetadata;
import uk.myoung.feedbackhelper.view.style.Fonts;
import uk.myoung.feedbackhelper.view.style.LogoIcon;

/**
 * About Dialog Class.
 */
public class AboutDialog extends JDialog {

    private static final String DEVELOPER_INFO = "by Michael Young & Bhuvan Bezawada";

    private static final String GOAL_INFO =
        "The goal of this tool is to help markers create feedback documents more efficiently.";

    private static final String CONTRIBUTOR_INFO =
        "With contributions by Johannes Zelger, Oluwanifemi Fadare, and Yichen Cao.";

    private static final String ABOUT_INFO =
        "<html><br>" + DEVELOPER_INFO + "<br>" + GOAL_INFO + "<br><br>" + CONTRIBUTOR_INFO + "</html>";

    private static final String LINKS =
        "<html>Check out the <a href='https://github.com/mtorpey/FeedbackHelper'>Github repository</a> " +
        "or view the <a href='https://github.com/mtorpey/FeedbackHelper/issues'>issue tracker</a>.</html>";

    /**
     * Create and return a new object of this class, including setup.
     *
     * @param parent the dialog parent
     */
    public static AboutDialog create(JFrame parent) {
        AboutDialog dialog = new AboutDialog(parent);

        // Create the dialog components
        JPanel dialogPanel = new JPanel(new BorderLayout());

        JPanel panelWest = new JPanel(new FlowLayout());

        // Add information icon
        JLabel iconLabel = LogoIcon.createLabel();

        JPanel panelEast = new JPanel();
        panelEast.setLayout(new BoxLayout(panelEast, BoxLayout.Y_AXIS));
        // Create program header
        JLabel headerLabel = new JLabel("Feedback Helper");
        headerLabel.setFont(Fonts.getTitleFont());
        // Create labels for versions
        JLabel feedbackHelperVersionLabel = new JLabel("Version " + ApplicationMetadata.getVersion());
        feedbackHelperVersionLabel.setFont(Fonts.getSubtitleFont());
        JLabel javaVersionLabel = new JLabel("Java version " + System.getProperty("java.version"));
        // Create labels to add information
        JLabel developersLabel = new JLabel(ABOUT_INFO);
        // Add hyperlink pane
        JPanel linksPane = new JPanel(new FlowLayout());
        JEditorPane linksEditorPane = new JEditorPane("text/html", LINKS);

        linksEditorPane.setEditable(false);
        linksEditorPane.setFocusable(false);
        linksEditorPane.setOpaque(false);

        // Hyperlink navigation
        linksEditorPane.addHyperlinkListener(
            new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent hle) {
                    // Open in browser on hyperlink activation
                    if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                        try {
                            openLocation(hle.getURL().toURI());
                        } catch (URISyntaxException e) {
                            // Unlikely
                            e.printStackTrace();
                        }
                    }
                }
            }
        );

        // Add panels
        dialogPanel.add(panelWest, BorderLayout.WEST);
        panelWest.add(iconLabel, BorderLayout.CENTER);
        dialogPanel.add(panelEast, BorderLayout.EAST);
        panelEast.add(headerLabel);
        panelEast.add(feedbackHelperVersionLabel);
        panelEast.add(javaVersionLabel);
        panelEast.add(developersLabel);
        // Add pane for links
        panelEast.add(linksPane);
        linksPane.add(linksEditorPane);

        dialog.add(dialogPanel, BorderLayout.CENTER);

        dialog.setResizable(false);
        dialog.pack(); // Adjust dialog size to fit components
        dialog.setLocationRelativeTo(parent); // Center the dialog onscreen

        dialog.setVisible(true);

        return dialog;
    }

    private AboutDialog(JFrame parent) {
        super(parent, "About Feedback Helper", true);
    }

    /**
     * Open a given url
     *
     * @param location of webpage
     */
    private static void openLocation(URI location) {
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(
                null,
                "Java is not able to launch links on your computer.",
                "Cannot Launch Link",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Desktop desktop = Desktop.getDesktop();

        try {
            desktop.browse(location);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "Failed to launch the link.",
                "Cannot Launch Link",
                JOptionPane.WARNING_MESSAGE
            );
            e.printStackTrace();
        }
    }
}
