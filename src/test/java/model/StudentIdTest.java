package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
 
class StudentIdTest {
 
    @Test
    void createGood() {
        String text = "210001234";
        StudentId studentId = new StudentId(text);
        assertEquals(text, studentId.toString());
    }
}
