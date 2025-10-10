package model;

import java.util.Objects;

/**
 * Generic Pair Class.
 *
 * @param <T> The data type to use in the pair.
 */
public class Pair<T> {

    // Instance variables
    private final T first;
    private final T second;

    /**
     * Constructor.
     *
     * @param first  The first item of the pair.
     * @param second The second item of the pair.
     */
    public Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Get the first item of the pair.
     *
     * @return The first item of the pair.
     */
    public T getFirst() {
        return this.first;
    }

    /**
     * Get the second item of the pair.
     *
     * @return The second item of the pair.
     */
    public T getSecond() {
        return this.second;
    }

    /**
     * Define how a Pair should be classified as being equal to another Pair.
     *
     * @param o The object to compare to.
     * @return True if objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    /**
     * Define how to compute the hashcode of the object.
     *
     * @return An integer which is the hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    /**
     * String representation of the Pair Object.
     *
     * @return The string representation of the object.
     */
    @Override
    public String toString() {
        return "Pair{" + "first=" + first + ", second=" + second + '}';
    }
}
