package view;

/**
 * Phrase Type Enum.
 */
public enum PhraseType {

    // Phrase type values
    CUSTOM("Custom"),
    FREQUENTLY_USED("Frequently Used");
    //INSIGHTS("Insights");

    // Instance variable
    public final String phraseTypeAsString;

    /**
     * Constructor.
     *
     * @param phraseTypeAsString The phrase type as a string.
     */
    PhraseType(String phraseTypeAsString) {
        this.phraseTypeAsString = phraseTypeAsString;
    }

    /**
     * Get the phrase type as a string.
     *
     * @return The phrase type as a string.
     */
    public String getPhraseTypeAsString() {
        return phraseTypeAsString;
    }

}
