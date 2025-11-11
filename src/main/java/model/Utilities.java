package model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities Class.
 */
public class Utilities {

    /**
     * Get a set of additions when comparing an old set to a new set.
     *
     * @param oldSet The old set.
     * @param newSet The new set.
     * @param <T> The type of the data in the sets.
     * @return The additions to the old set.
     */
    public static <T> Set<T> getAdditionsToSet(Collection<T> oldSet, Collection<T> newSet) {
        Set<T> oldSetCopy = new HashSet<>(oldSet);
        Set<T> newSetCopy = new HashSet<>(newSet);
        newSetCopy.removeAll(oldSetCopy);
        return newSetCopy;
    }

    /**
     * Get a set of removals when comparing an old set to a new set.
     *
     * @param oldSet The old set.
     * @param newSet The new set.
     * @param <T> The type of the data in the sets.
     * @return The removals from the old set.
     */
    public static <T> Set<T> getRemovalsFromSet(Collection<T> oldSet, Collection<T> newSet) {
        Set<T> oldSetCopy = new HashSet<>(oldSet);
        Set<T> newSetCopy = new HashSet<>(newSet);
        oldSetCopy.removeAll(newSetCopy);
        return oldSetCopy;
    }

    /**
     * Get a set of everything that is the same between an old set and a new set.
     *
     * @param oldSet The old set.
     * @param newSet The new set.
     * @param <T> The type of the data in the sets.
     * @return The set of everything that stayed the same.
     */
    public static <T> Set<T> getIntersection(Collection<T> oldSet, Collection<T> newSet) {
        Set<T> oldSetCopy = new HashSet<>(oldSet);
        Set<T> newSetCopy = new HashSet<>(newSet);
        newSetCopy.retainAll(oldSetCopy);
        return newSetCopy;
    }
}
