package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FeedbackStyleTest {

    @Test
    void createDefault() {
        FeedbackStyle style = new FeedbackStyle("", "", 1, "- ");
        assertEquals("", style.headingStyle());
        assertEquals("", style.underlineStyle());
        assertEquals(1, style.lineSpacing());
        assertEquals("- ", style.lineMarker());
    }

    @Test
    void createGood() {
        FeedbackStyle style = new FeedbackStyle("#", "=", 2, "•");
        assertEquals("# ", style.headingStyle());
        assertEquals("=", style.underlineStyle());
        assertEquals(2, style.lineSpacing());
        assertEquals("• ", style.lineMarker());
    }

    @Test
    void createSpecial() {
        FeedbackStyle style = new FeedbackStyle(" #   ", "-=", -42, "");
        assertEquals("# ", style.headingStyle());
        assertEquals("", style.underlineStyle());
        assertEquals(0, style.lineSpacing());
        assertEquals("- ", style.lineMarker());
    }
}
