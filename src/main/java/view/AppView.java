package view;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.nio.file.Path;

import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
     * Create and return a new object of this class, including setup.
     *
     * @param controller The controller for the program.
     */
    public static AppView create(AppController controller) {
        AppView view = new AppView(controller);

        installThirdPartyThemes();
        applyUserTheme();
        applyFontScaling(UserPreferences.getScale());
        addMacKeyBindings();

        return view;
    }

    private AppView(AppController controller) {
        this.controller = controller;
    }

    /**
     * Start the app at the homescreen, without a file already provided.
     */
    public void start() {
        HomeScreen.create(controller);
    }

    /** Start the app with a pre-specified file to open, skipping the home screen. */
    public void startWithFile(Path fhtFile) {
        // Try to load the assignment
        try {
            controller.loadAssignment(fhtFile);
            FeedbackScreen.create(controller);
        } catch (Exception exception) {
            JFrame parent = new JFrame();
            LogoIcon.applyIcon(parent);
            JOptionPane.showMessageDialog(
                parent,
                exception.toString(),
                "Problem loading assignment",
                JOptionPane.ERROR_MESSAGE
            );
            exception.printStackTrace();
            parent.dispose();
        }
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
        UIManager.getDefaults()
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
    private static void addMacKeyBindings() {
        // From https://stackoverflow.com/questions/7252749
        InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
    }
}
