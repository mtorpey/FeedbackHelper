package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.Test;
 
class UtilitiesTest {

    Collection<String> fruits = Set.of("banana", "apple", "strawberry", "grape", "lemon");
    Collection<String> iceCreams = Set.of("vanilla", "chocolate", "strawberry", "honey", "banana");
    
    @Test
    void getAdditions() {
        assertEquals(Set.of("vanilla", "chocolate", "honey"), Utilities.getAdditions(fruits, iceCreams));
    }

    @Test
    void getRemovals() {
        assertEquals(Set.of("apple", "grape", "lemon"), Utilities.getRemovals(fruits, iceCreams));
    }

    @Test
    void getIntersection() {
        assertEquals(Set.of("strawberry", "banana"), Utilities.getIntersection(fruits, iceCreams));
    }
}
