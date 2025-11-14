package view;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * About Dialog Class.
 */
public class AboutDialog extends JDialog {

    public static final String DEVELOPER_INFO =
        "Feedback Helper was developed by Bhuvan Bezawada as part of a CS5099 project at the University of St Andrews.";

    public static final String CONTRIBUTOR_INFO =
        "Later contributions by Michael Young, Johannes Zelger, and Oluwanifemi Fadare.";

    public static final String GOAL_INFO =
        "The goal of this tool is to help markers create feedback documents more efficiently.";

    public static final String ABOUT_INFO =
        "<html><br>" + DEVELOPER_INFO + "<br>" + GOAL_INFO + "<br><br>" + CONTRIBUTOR_INFO + "</html>";

    public static final String LINKS =
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
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 24, 16));

        JPanel panelWest = new JPanel(new FlowLayout());

        // Add information icon
        JLabel iconLabel = new JLabel(UIManager.getLookAndFeel().getDefaults().getIcon("OptionPane.informationIcon"));

        JPanel panelEast = new JPanel();
        panelEast.setLayout(new BoxLayout(panelEast, BoxLayout.Y_AXIS));
        // Create program header
        JLabel headerLabel = new JLabel("Feedback Helper");
        headerLabel.setFont(headerLabel.getFont().deriveFont(32.0f)); // Increase size
        // Create a label to display the Java version
        JLabel javaVersionLabel = new JLabel("Java Version: " + System.getProperty("java.version"));
        // Create labels to add information
        JLabel developersLabel = new JLabel(ABOUT_INFO);
        // Add hyperlink pane
        JPanel linksPane = new JPanel(new FlowLayout());
        JEditorPane linksEditorPane = new JEditorPane("text/html", LINKS);

        linksEditorPane.setEditable(false);
        linksEditorPane.setOpaque(false);

        // Hyperlink navigation
        linksEditorPane.addHyperlinkListener(
            new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent hle) {
                    // Open in browser on hyperlink activation
                    if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                        openLocation(hle.getURL().toString());
                    }
                }
            }
        );

        // Add panels
        dialogPanel.add(panelWest, BorderLayout.WEST);
        panelWest.add(iconLabel, BorderLayout.CENTER);
        dialogPanel.add(panelEast, BorderLayout.EAST);
        panelEast.add(headerLabel);
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
     * @param url of webpage
     */
    private static void openLocation(String url) {
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
            desktop.browse(new URI(url));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "Failed to launch the link, your computer is likely misconfigured.",
                "Cannot Launch Link",
                JOptionPane.WARNING_MESSAGE
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
