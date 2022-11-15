package configuration;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public abstract class UserPreferences {

    public static final String THEME = "theme";
    
    private static Preferences prefs = Preferences.userRoot().node(UserPreferences.class.getClass().getCanonicalName());

    public static String getTheme() {
        return prefs.get(THEME, null);
    }

    public static void setTheme(String name) {
        prefs.put(THEME, name);
    }

    public static void addPreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.addPreferenceChangeListener(listener);
    }

}
