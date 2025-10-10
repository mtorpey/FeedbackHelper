package model;

/**
 * Linked Phrases Class.
 */
public class LinkedPhrases {

    // Instance variables
    private final Phrase first;
    private final Phrase second;
    private final int count;

    /**
     * Constructor.
     *
     * @param first  The first phrase.
     * @param second The second phrase.
     * @param count  The count value for the number of time the phrases have been linked.
     */
    public LinkedPhrases(Phrase first, Phrase second, int count) {
        this.first = first;
        this.second = second;
        this.count = count;
    }

    /**
     * Get the first phrase.
     *
     * @return The first phrase.
     */
    public Phrase getFirst() {
        return first;
    }

    /**
     * Get the second phrase.
     *
     * @return The second phrase.
     */
    public Phrase getSecond() {
        return second;
    }

    /**
     * Get the count of the number of times the phrases have been linked.
     *
     * @return The count of the number of times the phrases have been linked.
     */
    public int getCount() {
        return count;
    }
}
