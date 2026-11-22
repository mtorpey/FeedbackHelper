package uk.myoung.feedbackhelper.infrastructure;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public abstract class UserPreferences {

    public static final String THEME = "theme";
    public static final String SCALE = "scale";
    public static final String LAST_OPENED_ASSIGNMENT_PATH = "lastOpenedAssignmentPath";
    public static final String SPELL_CHECK_ENABLED = "spellCheckEnabled";
    public static final String CUSTOM_DICTIONARY_WORDS = "customDictionaryWords";

    private static String NODE_NAME = "FeedbackHelper-5";
    private static Preferences prefs = Preferences.userRoot().node(NODE_NAME);

    private static final String THEME_DEFAULT = "com.formdev.flatlaf.FlatLightLaf";

    public static String getTheme() {
        return prefs.get(THEME, THEME_DEFAULT);
    }

    public static void setTheme(String name) {
        prefs.put(THEME, name);
    }

    public static boolean isDarkThemeSelected() {
        return getTheme().equals("com.formdev.flatlaf.FlatDarkLaf");
    }

    public static Path getLastOpenedAssignmentPath() {
        String pathStr = prefs.get(LAST_OPENED_ASSIGNMENT_PATH, null);
        return pathStr == null ? null : Path.of(pathStr);
    }

    public static void setLastOpenedAssignment(Path assignmentFile) {
        if (assignmentFile != null) {
            prefs.put(LAST_OPENED_ASSIGNMENT_PATH, assignmentFile.toAbsolutePath().toString());
        }
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

    public static boolean isSpellCheckEnabled() {
        return prefs.getBoolean(SPELL_CHECK_ENABLED, true);
    }

    public static void setSpellCheckEnabled(boolean enabled) {
        prefs.putBoolean(SPELL_CHECK_ENABLED, enabled);
    }

    public static Set<String> getCustomDictionaryWords() {
        String raw = prefs.get(CUSTOM_DICTIONARY_WORDS, "");
        if (raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split("\n"))
            .map(String::trim)
            .filter(w -> !w.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
    }

    public static void setCustomDictionaryWords(Set<String> words) {
        prefs.put(CUSTOM_DICTIONARY_WORDS, String.join("\n", words));
    }
}
