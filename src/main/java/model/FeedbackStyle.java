package model;

import java.io.Serializable;

/**
 * Preferences for the formatting style used in feedback documents.
 *
 * @param headingStyle String inserted before a heading
 * @param underlineStyle Character used to underline a heading, if any
 * @param lineSpacing Number of blank lines after a section
 * @param lineMarker Bullet point to start a feedback phrase with
 */
public record FeedbackStyle(String headingStyle, String underlineStyle, int lineSpacing, String lineMarker) implements
    Serializable {
    /** Constructor that checks a few things for sanity. */
    public FeedbackStyle {
        if (headingStyle.length() > 0) {
            headingStyle = headingStyle.trim() + " ";
        } else {
            headingStyle = "";
        }

        if (underlineStyle.length() > 1) {
            underlineStyle = "";
        }

        if (lineSpacing < 0) {
            lineSpacing = 0;
        }

        if (lineMarker.length() > 0) {
            lineMarker = lineMarker.trim() + " ";
        } else {
            lineMarker = "- ";
        }
    }
}
