package model;

import junit.framework.TestCase;

public class LinkedPhrasesTest extends TestCase {

    private Phrase phrase1;
    private Phrase phrase2;
    private LinkedPhrases linkedPhrases;

    public void setUp() throws Exception {
        phrase1 = new Phrase("phrase 1");
        phrase2 = new Phrase("phrase 2");
        linkedPhrases = new LinkedPhrases(phrase1, phrase2, 1);
    }

    public void testGetFirst() {
        assertEquals(phrase1, linkedPhrases.getFirst());
    }

    public void testGetSecond() {
        assertEquals(phrase2, linkedPhrases.getSecond());
    }

    public void testGetCount() {
        assertEquals(1, linkedPhrases.getCount());
    }
}
