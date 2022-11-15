package view;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import configuration.UserPreferences;
import controller.IAppController;

/**
 * App View Class.
 */
public class AppView implements IAppView {

    // Scrollar speeds
    public static final int SCROLL_SPEED = 15;

    // Instance variable
    private final IAppController controller;

    /**
     * Constructor.
     *
     * @param controller The controller.
     */
    public AppView(IAppController controller) {
        this.controller = controller;
    }

    /**
     * Start the app.
     */
    @Override
    public void start() {
        applyUserTheme();
        new HomeScreen(controller);
    }

    public static void applyUserTheme() {
        String theme = UserPreferences.getTheme();
        if (theme != null) {
            try {
                UIManager.setLookAndFeel(theme);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }
    }

}
