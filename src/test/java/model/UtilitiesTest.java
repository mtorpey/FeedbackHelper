package model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class UtilitiesTest extends TestCase {

    private List<String> oldList;
    private List<String> newList;
    private List<Pair<String>> oldPairs;
    private List<Pair<String>> newPairs;

    public void setUp() throws Exception {
        oldList = new ArrayList<String>();
        oldList.add("old-1");
        oldList.add("old-2");
        oldList.add("old-3");

        newList = new ArrayList<String>();
        newList.add("old-1");
        newList.add("new-1");
        newList.add("new-2");

        oldPairs = new ArrayList<Pair<String>>();
        oldPairs.add(new Pair<>("old-1", "old-2"));
        oldPairs.add(new Pair<>("old-2", "old-3"));

        newPairs = new ArrayList<Pair<String>>();
        newPairs.add(new Pair<>("old-1", "new-1"));
        newPairs.add(new Pair<>("new-1", "new-2"));
    }

    public void testGetAdditionsToList() {
        List<String> additionsToList = Utilities.getAdditionsToList(oldList, newList);
        assertEquals(2, additionsToList.size());
        assertEquals("new-1", additionsToList.get(0));
        assertEquals("new-2", additionsToList.get(1));
    }

    public void testGetRemovalsFromList() {
        List<String> removalsFromList = Utilities.getRemovalsFromList(oldList, newList);
        assertEquals(2, removalsFromList.size());
        assertEquals("old-2", removalsFromList.get(0));
        assertEquals("old-3", removalsFromList.get(1));
    }

    public void testGetIntersection() {
        List<String> intersection = Utilities.getIntersection(oldList, newList);
        assertEquals(1, intersection.size());
        assertEquals("old-1", intersection.get(0));
    }

    public void testGetPairs() {
        List<Pair<String>> pairs = Utilities.getPairs(newList);
        assertEquals(2, pairs.size());
        assertEquals("old-1", pairs.get(0).getFirst());
        assertEquals("new-1", pairs.get(0).getSecond());
        assertEquals("new-1", pairs.get(1).getFirst());
        assertEquals("new-2", pairs.get(1).getSecond());
    }

    public void testGetPairsToRemove() {
        List<Pair<String>> pairsToRemove = Utilities.getPairsToRemove(oldPairs, newPairs);
        assertEquals(2, pairsToRemove.size());
        assertEquals("old-1", pairsToRemove.get(0).getFirst());
        assertEquals("old-2", pairsToRemove.get(0).getSecond());
        assertEquals("old-2", pairsToRemove.get(1).getFirst());
        assertEquals("old-3", pairsToRemove.get(1).getSecond());
    }

    public void testGetPairsToAdd() {
        List<Pair<String>> pairsToAdd = Utilities.getPairsToAdd(oldPairs, newPairs);
        assertEquals(2, pairsToAdd.size());
        assertEquals("old-1", pairsToAdd.get(0).getFirst());
        assertEquals("new-1", pairsToAdd.get(0).getSecond());
        assertEquals("new-1", pairsToAdd.get(1).getFirst());
        assertEquals("new-2", pairsToAdd.get(1).getSecond());
    }
}
