package configuration;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public abstract class UserPreferences {

    public static final String THEME = "theme";
    public static final String LAST_OPENED_ASSIGNMENT_PATH = "lastOpenedAssignmentPath";

    private static String NODE_NAME = "FeedbackHelper";
    private static Preferences prefs = Preferences.userRoot().node(NODE_NAME);

    public static String getTheme() {
        return prefs.get(THEME, null);
    }

    public static void setTheme(String name) {
        prefs.put(THEME, name);
    }

    public static String getLastOpenedAssignmentPath() {
        return prefs.get(LAST_OPENED_ASSIGNMENT_PATH, null);
    }

    public static void setLastOpenedAssignmentPath(String assignmentFilePath) {
        prefs.put(LAST_OPENED_ASSIGNMENT_PATH, assignmentFilePath);
    }

    public static void addPreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.addPreferenceChangeListener(listener);
    }
}
