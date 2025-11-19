package view;

import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultEditorKit;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import configuration.UserPreferences;
import controller.AppController;

/**
 * Class for doing some basic setup of the view, and holding some global variables.
 */
public class AppView {

    // Scrollar speeds
    public static final int SCROLL_SPEED = 20;

    // Instance variable
    private final AppController controller;

    /**
     * Constructor.
     *
     * @param controller The controller.
     */
    public AppView(AppController controller) {
        this.controller = controller;
    }

    /**
     * Start the app.
     */
    public void start() {
        installThirdPartyThemes();
        applyUserTheme();
        applyFontScaling(UserPreferences.getScale());
        addMacKeyBindings();
        HomeScreen.create(controller);
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
            } catch (
                ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException e
            ) {
                e.printStackTrace();
            }
        }
    }

    /** Apply font scaling to all fonts in the UI. */
    private static void applyFontScaling(float scale) {
        UIManager
            .getDefaults()
            .keySet()
            .stream()
            .map(Object::toString)
            .filter(s -> s.endsWith(".font"))
            .forEach(key -> {
                Font font = UIManager.getFont(key);
                UIManager.put(key, font.deriveFont(font.getSize() * scale));
            });
    }

    /** Attempt to support command key cut/copy/paste on a Mac. */
    private void addMacKeyBindings() {
        // From https://stackoverflow.com/questions/7252749
        InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
    }
}
