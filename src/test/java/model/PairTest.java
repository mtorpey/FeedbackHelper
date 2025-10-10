package model;

import junit.framework.TestCase;

public class PairTest extends TestCase {

    private Pair<String> stringPair;
    private Pair<Phrase> phrasePair;

    public void setUp() throws Exception {
        stringPair = new Pair<String>("one", "two");
        phrasePair = new Pair<Phrase>(new Phrase("phrase 1"), new Phrase("phrase 2"));
    }

    public void testStringGetFirst() {
        assertEquals("one", stringPair.getFirst());
    }

    public void testStringGetSecond() {
        assertEquals("two", stringPair.getSecond());
    }

    public void testPhraseGetFirst() {
        assertEquals("phrase 1", phrasePair.getFirst().getPhraseAsString());
    }

    public void testPhraseGetSecond() {
        assertEquals("phrase 2", phrasePair.getSecond().getPhraseAsString());
    }

    public void testToString() {
        assertEquals("Pair{first=phrase 1, second=phrase 2}", phrasePair.toString());
    }
}
