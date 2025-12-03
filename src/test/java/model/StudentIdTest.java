package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
 
class StudentIdTest {
 
    @ParameterizedTest
    @ValueSource(strings = {"210001234", "090003445", "mct26", "Pg-acl", "_abc#!", "m?ch4^l{undergrad}"})
    void createGood(String text) {
        StudentId studentId = new StudentId(text);
        assertEquals(text, studentId.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "Michael Young", " ", "a\\b", "\"james\"", "'finn'", "曹操", "🕴️"})
    void createBad(String text) {
        assertThrows(IllegalArgumentException.class, () -> new StudentId(text));
    }

    @Test
    void compareLower() {
        StudentId s1 = new StudentId("jimmy");
        StudentId s2 = new StudentId("john");
        assertTrue(s1.compareTo(s2) < 0);
    }

    @Test
    void compareHigher() {
        StudentId s1 = new StudentId("Michael");
        StudentId s2 = new StudentId("340002933");
        assertTrue(s1.compareTo(s2) > 0);
    }
}
