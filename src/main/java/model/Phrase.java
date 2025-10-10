package model;

import java.util.Objects;

/**
 * Phrase Class.
 */
public class Phrase implements Comparable<Phrase> {

    // Instance variables
    private final String phraseAsString;
    private int usageCount;

    /**
     * Constructor.
     *
     * @param phraseAsString - The phrase as a string value.
     */
    public Phrase(String phraseAsString) {
        this.phraseAsString = phraseAsString;
    }

    /**
     * Get the string representation of the phrase.
     *
     * @return The phrase as a string.
     */
    public String getPhraseAsString() {
        return this.phraseAsString;
    }

    /**
     * Get the usage count of the phrase.
     *
     * @return The usage count of the phrase.
     */
    public int getUsageCount() {
        return this.usageCount;
    }

    /**
     * Set the usage count of the phrase.
     *
     * @param usageCount The usage count of the phrase.
     */
    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    /**
     * Increment the usage count by 1.
     */
    public void incrementUsageCount() {
        this.usageCount++;
    }

    /**
     * Decrement the usage count by 1.
     */
    public void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }

    /**
     * String representation of the Phrase Object.
     *
     * @return The string representation of the object.
     */
    @Override
    public String toString() {
        return phraseAsString;
    }

    /**
     * Define how a Phrase should be classified as being equal to another Phrase.
     *
     * @param o The object to compare to.
     * @return True if objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phrase phrase = (Phrase) o;
        return Objects.equals(phraseAsString, phrase.phraseAsString);
    }

    /**
     * Define how to compute the hashcode of the object.
     *
     * @return An integer which is the hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(phraseAsString);
    }

    /**
     * Define how to compare phrases when sorting them using Collections.
     *
     * @return The difference of the usageCounts.
     */
    @Override
    public int compareTo(Phrase o) {
        return o.getUsageCount() - this.getUsageCount();
    }
}
