package uk.myoung.feedbackhelper.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PhraseTest {

    static final String COMMENT_A = "I like the style of the code.";
    static final String COMMENT_B = "Please add more comments.";
    Phrase phraseA, phraseB, phraseACopy;

    @BeforeEach
    void setupPhrases() {
        phraseA = new Phrase(COMMENT_A, 42);
        phraseACopy = new Phrase(COMMENT_A, 42);
        phraseB = new Phrase(COMMENT_B); // usage count 1
    }

    @Test
    void stringCorrectA() {
        assertEquals(COMMENT_A, phraseA.getPhraseAsString());
    }

    @Test
    void stringCorrectB() {
        assertEquals(COMMENT_B, phraseB.getPhraseAsString());
    }

    @Test
    void usageCountA() {
        assertEquals(42, phraseA.getUsageCount());
    }

    @Test
    void usageCountDefault() {
        assertEquals(1, phraseB.getUsageCount());
    }

    @Test
    void unused() {
        phraseB.decrementUsageCount();
        assertTrue(phraseB.isUnused());
    }

    @Test
    void increment() {
        phraseA.incrementUsageCount();
        assertEquals(43, phraseA.getUsageCount());
        assertFalse(phraseA.isUnused());
    }

    @Test
    void decrementMinimum() {
        phraseB.decrementUsageCount();
        phraseB.decrementUsageCount();
        assertEquals(0, phraseB.getUsageCount());
        assertTrue(phraseB.isUnused());
    }

    @Test
    void equals() {
        assertEquals(phraseA, phraseACopy);
    }

    @Test
    void equalsSelf() {
        assertEquals(phraseA, phraseA);
    }

    @Test
    void notEqualsOtherType() {
        assertNotEquals(phraseA, COMMENT_A);
    }

    @Test
    void notEquals() {
        assertNotEquals(phraseA, phraseB);
    }

    @Test
    void notEqualsNull() {
        assertNotEquals(phraseB, null);
    }

    @Test
    void string() {
        assertEquals("42:" + COMMENT_A, phraseA.toString());
    }

    @Test
    void compareLess() {
        assertTrue(phraseA.compareTo(phraseB) < 0);
    }

    @Test
    void compareGreater() {
        assertTrue(phraseB.compareTo(phraseA) > 0);
    }

    @Test
    void compareEqual() {
        assertEquals(0, phraseACopy.compareTo(phraseA));
    }

    @Test
    void hashIsComment() {
        phraseACopy.incrementUsageCount();
        assertEquals(phraseACopy.hashCode(), phraseA.hashCode());
    }
}
