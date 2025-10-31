package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities Class.
 */
public class Utilities {

    /**
     * Get a list of additions when comparing an old list to a new list.
     *
     * @param oldList The old list.
     * @param newList The new list.
     * @param <T>     The type of the data in the lists.
     * @return The additions to the old list.
     */
    public static <T> List<T> getAdditionsToList(List<T> oldList, List<T> newList) {
        List<T> oldListCopy = new ArrayList<>(oldList);
        List<T> newListCopy = new ArrayList<>(newList);
        newListCopy.removeAll(oldListCopy);
        return newListCopy;
    }

    /**
     * Get a list of removals when comparing an old list to a new list.
     *
     * @param oldList The old list.
     * @param newList The new list.
     * @param <T>     The type of the data in the lists.
     * @return The removals from the old list.
     */
    public static <T> List<T> getRemovalsFromList(List<T> oldList, List<T> newList) {
        List<T> oldListCopy = new ArrayList<>(oldList);
        List<T> newListCopy = new ArrayList<>(newList);
        oldListCopy.removeAll(newListCopy);
        return oldListCopy;
    }

    /**
     * Get a list of everything that is the same between an old list and a new list.
     *
     * @param oldList The old list.
     * @param newList The new list.
     * @param <T>     The type of the data in the lists.
     * @return The list of everything that stayed the same.
     */
    public static <T> List<T> getIntersection(List<T> oldList, List<T> newList) {
        List<T> oldListCopy = new ArrayList<>(oldList);
        List<T> newListCopy = new ArrayList<>(newList);
        newListCopy.retainAll(oldListCopy);
        return newListCopy;
    }
}
