package view;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

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
        installThirdPartyThemes();
        applyUserTheme();
        new HomeScreen(controller);
    }

    /**
     * Add a few extra look-and-feels for users to choose from.
     *
     * Currently this just adds four look-and-feels from the "flatlaf" library,
     * which we import for this purpose.  There will also be several
     * pre-installed look-and-feels, which will vary by OS and Java
     * distribution.
     */
    private static void installThirdPartyThemes() {
        FlatDarkLaf.installLafInfo();
        FlatLightLaf.installLafInfo();
        // Note: we could also install FlatDarculaLaf and FlatIntelliJLaf if we
        // wanted, but these are almost identical to the above two themes.
    }

    /** Apply whatever theme is set in the user preferences. */
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
