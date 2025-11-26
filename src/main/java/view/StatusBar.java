package view;

import javax.swing.JLabel;

public class StatusBar extends JLabel {

    public static StatusBar create() {
        StatusBar statusBar = new StatusBar();
        statusBar.setText(" ");
        statusBar.setBorder(BorderCreator.statusBarBorder());
        return statusBar;
    }

    public void showMessage(String message) {
        setText(message);
        repaint();
    }
}
