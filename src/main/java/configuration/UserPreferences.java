package configuration;

import java.nio.file.Path;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public abstract class UserPreferences {

    public static final String THEME = "theme";
    public static final String SCALE = "scale";
    public static final String LAST_OPENED_ASSIGNMENT_PATH = "lastOpenedAssignmentPath";

    private static String NODE_NAME = "FeedbackHelper";
    private static Preferences prefs = Preferences.userRoot().node(NODE_NAME);

    public static String getTheme() {
        return prefs.get(THEME, null);
    }

    public static void setTheme(String name) {
        prefs.put(THEME, name);
    }

    public static Path getLastOpenedAssignmentPath() {
        String pathStr = prefs.get(LAST_OPENED_ASSIGNMENT_PATH, null);
        return pathStr == null ? null : Path.of(pathStr);
    }

    public static void setLastOpenedAssignment(Path assignmentFile) {
        prefs.put(LAST_OPENED_ASSIGNMENT_PATH, assignmentFile.toAbsolutePath().toString());
    }

    public static void addPreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.addPreferenceChangeListener(listener);
    }

    public static float getScale() {
        String s = prefs.get(SCALE, null);
        if (s == null) {
            return 1f;
        } else {
            return Float.parseFloat(s);
        }
    }

    public static void setScale(float value) {
        prefs.put(SCALE, Float.toString(value));
    }
}
