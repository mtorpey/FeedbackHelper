package model;

import junit.framework.TestCase;

public class PhraseTest extends TestCase {

    private Phrase phrase;

    public void setUp() throws Exception {
        phrase = new Phrase("This is a phrase");
    }

    public void testGetPhraseAsString() {
        assertEquals("This is a phrase", phrase.getPhraseAsString());
    }

    public void testGetUsageCount() {
        assertEquals(0, phrase.getUsageCount());
    }

    public void testSetUsageCount() {
        phrase.setUsageCount(100);
        assertEquals(100, phrase.getUsageCount());
    }

    public void testIncrementUsageCount() {
        phrase.setUsageCount(0);
        phrase.incrementUsageCount();
        assertEquals(1, phrase.getUsageCount());
    }

    public void testDecrementUsageCount() {
        phrase.setUsageCount(1);
        phrase.decrementUsageCount();
        assertEquals(0, phrase.getUsageCount());
    }

    public void testDecrementBeyondUsageCount() {
        phrase.setUsageCount(0);
        phrase.decrementUsageCount();
        assertEquals(0, phrase.getUsageCount());
    }

    public void testEquals() {
        Phrase newPhrase = new Phrase("New phrase");
        assertTrue(phrase.equals(phrase));
        assertFalse(phrase.equals(newPhrase));
    }

    public void testCompare() {
        Phrase newPhrase = new Phrase("New phrase");
        newPhrase.incrementUsageCount();

        // Other phrase usage count is 0, newPhrase usage count is 1
        // 0-1 = -1,
        assertEquals(-1, newPhrase.compareTo(phrase));
    }

    public void testToString() {
        assertEquals("This is a phrase", phrase.toString());
    }
}
