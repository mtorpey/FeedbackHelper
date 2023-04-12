package view;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * About Dialog Class.
 */
public class AboutDialog extends JDialog {
    
    String info = "Feedback Helper was originally written by Bhuvan Bezawadag " +
    "as part of a CS5099 project at the University of St Andrews." +
    "Later contributions by Michael Young, Johannes Zelger, and Oluwanifemi Fadare. \n" +
    "The goal of the tool is to help markers create feedback documents more efficiently and " +
    "give them insight into the content of their feedback regarding phrases they use and " +
    "the sentiment behind them. ";
    
    /**
     * Constructor.
     *
     * @param parent the dialog parent
     */
    public AboutDialog(JFrame parent) {
        super(parent, "About", true);
        // set size, layout, and other properties of the dialog

        setLocationRelativeTo(null);

        // create a label to display the Java version
        JLabel javaVersionLabel = new JLabel("Java Version: " + System.getProperty("java.version"));
        add(javaVersionLabel);

        // add UI elements such as labels, buttons, and images
     }

}
