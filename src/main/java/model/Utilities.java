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

    /**
     * Create a list of pairs of elements in a list.
     *
     * @param newList The list to create pairs from.
     * @param <T>     The type of the data in the list.
     * @return A list of pairs created from elements in the given list.
     */
    public static <T> List<Pair<T>> getPairs(List<T> newList) {
        List<Pair<T>> pairsList = new ArrayList<Pair<T>>();

        for (int i = 0; i < newList.size() - 1; i++) {
            pairsList.add(new Pair<T>(newList.get(i), newList.get(i + 1)));
        }

        return pairsList;
    }

    /**
     * Find pairs to remove when comparing an old list to a new list.
     *
     * @param oldList The old list.
     * @param newList The new list.
     * @param <T>     The type of the object in the lists.
     * @return A list of pairs to remove.
     */
    public static <T> List<Pair<T>> getPairsToRemove(List<Pair<T>> oldList, List<Pair<T>> newList) {
        return getRemovalsFromList(oldList, newList);
    }

    /**
     * Find pairs to add when comparing an old list to a new list.
     *
     * @param oldList The old list.
     * @param newList The new list.
     * @param <T>     The type of the object in the lists.
     * @return A list of pairs to add.
     */
    public static <T> List<Pair<T>> getPairsToAdd(List<Pair<T>> oldList, List<Pair<T>> newList) {
        return getAdditionsToList(oldList, newList);
    }
}
