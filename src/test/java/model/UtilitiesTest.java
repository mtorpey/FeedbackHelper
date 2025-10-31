package model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class UtilitiesTest extends TestCase {

    private List<String> oldList;
    private List<String> newList;

    public void setUp() throws Exception {
        oldList = new ArrayList<String>();
        oldList.add("old-1");
        oldList.add("old-2");
        oldList.add("old-3");

        newList = new ArrayList<String>();
        newList.add("old-1");
        newList.add("new-1");
        newList.add("new-2");
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
}
