package uk.myoung.feedbackhelper.model;

import java.util.List;

/**
 * Read-only view of the spell checker model for use by views.
 */
public interface SpellCheckReadOnly {
    record Misspelling(int start, int end, String word) {}

    List<Misspelling> findMisspellings(String text);

    List<String> suggest(String word, int maxSuggestions);

    boolean isEnabled();

    void addListener(Runnable listener);

    void removeListener(Runnable listener);
}
