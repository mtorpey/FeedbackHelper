package uk.myoung.feedbackhelper.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Spell checker model backed by an embedded English dictionary.
 */
public class SpellChecker implements SpellCheckReadOnly {

    private static final String DICTIONARY_RESOURCE = "/dictionary/words.txt.gz";
    private static final int MAX_EDIT_DISTANCE = 2;
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\d_]+(?:['-][\\p{L}\\d_]+)*");

    private final Set<String> dictionary;
    private final Set<String> customWords = new HashSet<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private boolean enabled = true;

    public static SpellChecker createDefault() {
        return new SpellChecker(loadDefaultDictionary());
    }

    public SpellChecker(Set<String> dictionary) {
        Set<String> normalized = new HashSet<>();
        for (String word : dictionary) {
            if (word != null && !word.isBlank()) {
                normalized.add(normalize(word));
            }
        }
        this.dictionary = Set.copyOf(normalized);
    }

    private static Set<String> loadDefaultDictionary() {
        InputStream stream = SpellChecker.class.getResourceAsStream(DICTIONARY_RESOURCE);
        if (stream == null) {
            System.err.println("Spell check dictionary not found: " + DICTIONARY_RESOURCE);
            return Set.of();
        }
        try (
            InputStream in = stream;
            GZIPInputStream gzip = new GZIPInputStream(in);
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8))
        ) {
            Set<String> words = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    words.add(line.trim());
                }
            }
            return Set.copyOf(words);
        } catch (IOException e) {
            System.err.println("Failed to load spell check dictionary: " + e.getMessage());
            return Set.of();
        }
    }

    public boolean isCorrect(String word) {
        if (word == null || dictionary.isEmpty()) {
            return true;
        }
        String trimmed = word.trim();
        if (trimmed.length() <= 1 || shouldIgnoreToken(trimmed)) {
            return true;
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (isKnownWord(lower)) {
            return true;
        }
        if (lower.endsWith("'s") && isKnownWord(lower.substring(0, lower.length() - 2))) {
            return true;
        }
        if (lower.endsWith("'") && isKnownWord(lower.substring(0, lower.length() - 1))) {
            return true;
        }
        if (lower.contains("-")) {
            for (String part : lower.split("-")) {
                if (!isCorrect(part)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<Misspelling> findMisspellings(String text) {
        if (!enabled || dictionary.isEmpty() || text == null || text.isEmpty()) {
            return List.of();
        }
        List<Misspelling> misspellings = new ArrayList<>();
        Matcher matcher = WORD_PATTERN.matcher(text);
        while (matcher.find()) {
            String token = matcher.group();
            if (!isCorrect(token)) {
                misspellings.add(new Misspelling(matcher.start(), matcher.end(), token));
            }
        }
        return misspellings;
    }

    @Override
    public List<String> suggest(String word, int maxSuggestions) {
        if (word == null || word.isBlank() || dictionary.isEmpty() || maxSuggestions <= 0) {
            return List.of();
        }
        String target = normalize(word);
        boolean capitalize = Character.isUpperCase(word.trim().charAt(0));

        List<String> distance1 = new ArrayList<>();
        List<String> distance2 = new ArrayList<>();

        collectSuggestions(target, capitalize, customWords, distance1, distance2);
        collectSuggestions(target, capitalize, dictionary, distance1, distance2);

        List<String> combined = new ArrayList<>(distance1);
        combined.addAll(distance2);
        return combined.size() > maxSuggestions ? combined.subList(0, maxSuggestions) : combined;
    }

    public void addCustomWord(String word) {
        if (word == null || word.isBlank()) {
            return;
        }
        if (customWords.add(normalize(word))) {
            notifyListeners();
        }
    }

    public Set<String> getCustomWords() {
        return Set.copyOf(customWords);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            notifyListeners();
        }
    }

    @Override
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }

    private boolean isKnownWord(String lower) {
        return dictionary.contains(lower) || customWords.contains(lower);
    }

    private static String normalize(String word) {
        return word.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Skip tokens that are likely code identifiers, student IDs, numbers, or acronyms
     * (anything with digits, underscores, camelCase/PascalCase transitions, or all-uppercase).
     */
    private static boolean shouldIgnoreToken(String token) {
        boolean allUpper = true;
        boolean seenLower = false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (Character.isDigit(c) || c == '_') {
                return true;
            }
            if (Character.isLowerCase(c)) {
                allUpper = false;
                seenLower = true;
            } else if (Character.isUpperCase(c) && seenLower) {
                return true;
            }
        }
        return allUpper;
    }

    private static void collectSuggestions(
        String target,
        boolean capitalize,
        Set<String> source,
        List<String> distance1,
        List<String> distance2
    ) {
        for (String candidate : source) {
            if (Math.abs(candidate.length() - target.length()) > MAX_EDIT_DISTANCE) {
                continue;
            }
            int dist = damerauLevenshtein(target, candidate);
            if (dist == 1) {
                distance1.add(matchCase(candidate, capitalize));
            } else if (dist == 2) {
                distance2.add(matchCase(candidate, capitalize));
            }
        }
    }

    private static String matchCase(String word, boolean capitalize) {
        if (!capitalize || word.isEmpty()) {
            return word;
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    /**
     * Compute Damerau-Levenshtein edit distance (insertions, deletions,
     * substitutions, and adjacent character transpositions).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Damerau-Levenshtein_distance">Damerau-Levenshtein distance (Wikipedia)</a>
     */
    private static int damerauLevenshtein(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                int best = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    best = Math.min(best, dp[i - 2][j - 2] + 1);
                }
                dp[i][j] = best;
            }
        }
        return dp[n][m];
    }
}
