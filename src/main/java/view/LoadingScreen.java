package view;

import javax.swing.ImageIcon;
import javax.swing.JWindow;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;

/**
 * Loading Screens Class.
 *
 * Used for creating splash screens, to cover periods when the program is
 * loading. Static methods are provided which create an appropriate instance,
 * and then delete it after a period of time; hence there are no public
 * constructors.
 */
public class LoadingScreen extends JWindow {

    // Image file paths - both images are custom made, so no licence is needed
    private static final String SPLASH_SCREEN = "/splashscreen.png";
    private static final String LOADING_SCREEN = "/loadingscreen.png";

    /**
     * Show a splash screen for 10 seconds.
     */
    public static void showSplashScreen() {
        showScreen(SPLASH_SCREEN, 10000);
    }

    /**
     * Show a loading screen for 3 seconds.
     */
    public static void showLoadingScreen() {
        showScreen(LOADING_SCREEN, 3000);
    }

    /**
     * Show a given image on a window in the middle of the screen.
     * Code adapted from:
     * https://www.tutorialspoint.com/how-can-we-implement-a-splash-screen-using-jwindow-in-java
     */
    private static void showScreen(String imagePath, int time) {
        LoadingScreen loadingScreen = new LoadingScreen(imagePath);
        try {
            Thread.sleep(time);
            loadingScreen.dispose();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Image to be displayed in this window. */
    private ImageIcon imageIcon;

    /**
     * Constructor which starts the loading screen displaying the given image.
     *
     * Note that this object does not delete itself, and must be deleted later
     * e.g. by calling its dispose() method.
     *
     * @param imagePath Path to the image file to be displayed.
     */
    private LoadingScreen(String imagePath) {
        // Create the loading window
        imageIcon = new ImageIcon(SetupOptionsScreen.class.getResource(imagePath));
        this.setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());

        // Centre the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - imageIcon.getIconWidth()) / 2;
        int y = (screenSize.height - imageIcon.getIconHeight()) / 2;
        this.setLocation(x, y);
        this.setVisible(true);

        // Draw the loading screen graphics
        this.paint(this.getGraphics());
    }

    /**
     * Draw the image every time this screen is repainted.
     *
     * @param g Should be equal to this.getGraphics().
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(imageIcon.getImage(), 0, 0, this);
    }

}
