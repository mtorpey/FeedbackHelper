package view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class StatusBar extends JLabel {
    public static StatusBar create() {
        StatusBar statusBar = new StatusBar();
        statusBar.setText("hello");
        statusBar.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        return statusBar;
    }

    public void showMessage(String message) {
        setText(message);
    }
}
