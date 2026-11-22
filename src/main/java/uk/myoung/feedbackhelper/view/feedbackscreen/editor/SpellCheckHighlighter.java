package uk.myoung.feedbackhelper.view.feedbackscreen.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import uk.myoung.feedbackhelper.model.SpellCheckReadOnly;
import uk.myoung.feedbackhelper.model.SpellCheckReadOnly.Misspelling;

/**
 * Highlights misspelled words in a text component with red wavy underlines.
 */
public class SpellCheckHighlighter {

    private static final Color WAVE_COLOR = new Color(220, 50, 47);

    private static final Highlighter.HighlightPainter SQUIGGLE_PAINTER =
        new DefaultHighlighter.DefaultHighlightPainter(null) {
            @Override
            public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
                try {
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
                    Rectangle r = shape.getBounds();
                    drawWavyUnderline(g, r.x, r.x + r.width - 1, r.y + r.height - 2);
                    return r;
                } catch (BadLocationException e) {
                    return null;
                }
            }
        };

    private final JTextComponent textComponent;
    private final SpellCheckReadOnly spellChecker;
    private final List<Object> highlightTags = new ArrayList<>();
    private List<Misspelling> currentMisspellings = List.of();

    public static SpellCheckHighlighter attach(JTextComponent textComponent, SpellCheckReadOnly spellChecker) {
        SpellCheckHighlighter highlighter = new SpellCheckHighlighter(textComponent, spellChecker);
        highlighter.install();
        return highlighter;
    }

    private SpellCheckHighlighter(JTextComponent textComponent, SpellCheckReadOnly spellChecker) {
        this.textComponent = textComponent;
        this.spellChecker = spellChecker;
    }

    private void install() {
        if (spellChecker == null) {
            return;
        }
        textComponent
            .getDocument()
            .addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        scheduleRefresh();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        scheduleRefresh();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        scheduleRefresh();
                    }
                }
            );
        spellChecker.addListener(this::scheduleRefresh);
        scheduleRefresh();
    }

    private void scheduleRefresh() {
        SwingUtilities.invokeLater(this::refresh);
    }

    public void refresh() {
        if (spellChecker == null) {
            return;
        }
        Highlighter highlighter = textComponent.getHighlighter();
        for (Object tag : highlightTags) {
            highlighter.removeHighlight(tag);
        }
        highlightTags.clear();

        if (!spellChecker.isEnabled() || !textComponent.isEditable()) {
            currentMisspellings = List.of();
            return;
        }

        currentMisspellings = spellChecker.findMisspellings(textComponent.getText());
        for (Misspelling m : currentMisspellings) {
            try {
                highlightTags.add(highlighter.addHighlight(m.start(), m.end(), SQUIGGLE_PAINTER));
            } catch (BadLocationException ignored) {
                // Document changed before refresh ran; the next queued refresh will update highlights.
            }
        }
    }

    public Optional<Misspelling> getMisspellingAt(int offset) {
        return currentMisspellings
            .stream()
            .filter(m -> m.start() <= offset && offset <= m.end())
            .findFirst();
    }

    /** Draw a 2px-tall zigzag line along the baseline between xStart and xEnd. */
    private static void drawWavyUnderline(Graphics g, int xStart, int xEnd, int y) {
        Color previousColor = g.getColor();
        g.setColor(WAVE_COLOR);
        for (int x = xStart; x < xEnd; x += 4) {
            int xMid = Math.min(x + 2, xEnd);
            int xNext = Math.min(x + 4, xEnd);
            g.drawLine(x, y, xMid, y - 2);
            if (xMid < xNext) {
                g.drawLine(xMid, y - 2, xNext, y);
            }
        }
        g.setColor(previousColor);
    }
}
