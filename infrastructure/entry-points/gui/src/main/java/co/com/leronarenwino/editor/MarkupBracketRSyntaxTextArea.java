/*
 * This file is part of FreeMarker JSON/XML Toolkit.
 *
 * FreeMarker JSON/XML Toolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FreeMarker JSON/XML Toolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with FreeMarker JSON/XML Toolkit. If not, see <https://www.gnu.org/licenses/>.
 */

package co.com.leronarenwino.editor;

import co.com.leronarenwino.editor.syntax.MarkupAngleBracketMatching;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextarea.RTextArea;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Bracket matching for {@code {[()]}} uses RSyntaxTextArea's normal paint path (behind text).
 * {@code <} / {@code >} as {@link org.fife.ui.rsyntaxtextarea.Token#MARKUP_TAG_DELIMITER} are matched separately
 * and drawn with a translucent fill so glyphs stay readable. Caret handling skips {@link RSyntaxTextArea}'s
 * {@code fireCaretUpdate} so angle and separator brackets are never both active.
 */
public class MarkupBracketRSyntaxTextArea extends RSyntaxTextArea {

    private static final float MARKUP_BRACKET_FILL_ALPHA = 0.24f;

    private final Point sharedBracket = new Point();
    private Rectangle markupMatchRect;
    private Rectangle markupDotRect;

    @Override
    public void setBracketMatchingEnabled(boolean enabled) {
        super.setBracketMatchingEnabled(enabled);
        if (!enabled) {
            RSyntaxBracketStateAccessor.clearBracketHighlightState(this);
            markupMatchRect = null;
            markupDotRect = null;
            repaint();
        } else {
            updateCombinedBracketMatching();
        }
    }

    @Override
    protected void fireCaretUpdate(CaretEvent e) {
        possiblyUpdateCurrentLineHighlightLocation();
        if (e != null && e.getDot() != e.getMark()) {
            RTextArea.cutAction.setEnabled(true);
            RTextArea.copyAction.setEnabled(true);
        } else {
            if (RTextArea.cutAction.isEnabled()) {
                RTextArea.cutAction.setEnabled(false);
                RTextArea.copyAction.setEnabled(false);
            }
        }
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CaretListener.class) {
                ((CaretListener) listeners[i + 1]).caretUpdate(e);
            }
        }
        if (isBracketMatchingEnabled()) {
            updateCombinedBracketMatching();
        }
    }

    @Override
    public void setPaintMatchedBracketPair(boolean paintPair) {
        super.setPaintMatchedBracketPair(paintPair);
        if (isBracketMatchingEnabled()) {
            updateCombinedBracketMatching();
        }
    }

    @Override
    public void setSyntaxScheme(SyntaxScheme scheme) {
        super.setSyntaxScheme(scheme);
        if (isBracketMatchingEnabled()) {
            updateCombinedBracketMatching();
        }
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        if (!isBracketMatchingEnabled()) {
            RSyntaxBracketStateAccessor.clearBracketHighlightState(this);
            markupMatchRect = null;
            markupDotRect = null;
            repaint();
            return;
        }
        if (isDisplayable()) {
            updateCombinedBracketMatching();
        }
    }

    private void repaintMarkupAreas() {
        if (markupMatchRect != null) {
            repaint(inflate(markupMatchRect, 3));
        }
        if (markupDotRect != null) {
            repaint(inflate(markupDotRect, 3));
        }
    }

    private static Rectangle inflate(Rectangle r, int p) {
        return new Rectangle(r.x - p, r.y - p, r.width + 2 * p, r.height + 2 * p);
    }

    private void updateCombinedBracketMatching() {
        repaintMarkupAreas();
        markupMatchRect = null;
        markupDotRect = null;

        if (!isBracketMatchingEnabled()) {
            return;
        }

        int markupAnchor = MarkupAngleBracketMatching.resolveMarkupBracketAnchor(this);
        if (markupAnchor >= 0) {
            RSyntaxBracketStateAccessor.clearBracketHighlightState(this);
            MarkupAngleBracketMatching.fillMatchForAnchor(this, markupAnchor, sharedBracket);
            if (sharedBracket.y >= 0) {
                try {
                    markupMatchRect = modelToView(sharedBracket.y);
                    markupDotRect = getPaintMatchedBracketPair() ? modelToView(sharedBracket.x) : null;
                } catch (BadLocationException ex) {
                    markupMatchRect = null;
                    markupDotRect = null;
                }
            }
        } else {
            RSyntaxUtilities.getMatchingBracketPosition(this, sharedBracket);
            RSyntaxBracketStateAccessor.applyStandardMatch(this, sharedBracket, getPaintMatchedBracketPair());
        }

        repaintMarkupAreas();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isBracketMatchingEnabled()) {
            return;
        }
        if (markupMatchRect == null && markupDotRect == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            if (markupMatchRect != null) {
                paintMarkupBracketOverlay(g2d, this, markupMatchRect);
            }
            if (markupDotRect != null && getPaintMatchedBracketPair()) {
                paintMarkupBracketOverlay(g2d, this, markupDotRect);
            }
        } finally {
            g2d.dispose();
        }
    }

    private static void paintMarkupBracketOverlay(Graphics2D g2d, RSyntaxTextArea rsta, Rectangle r) {
        Color bg = rsta.getMatchedBracketBGColor();
        Color border = rsta.getMatchedBracketBorderColor();
        Object oldAa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            if (bg != null) {
                g2d.setComposite(AlphaComposite.SrcOver.derive(MARKUP_BRACKET_FILL_ALPHA));
                g2d.setColor(bg);
                if (rsta.getAnimateBracketMatching()) {
                    g2d.fillRoundRect(r.x, r.y, r.width, r.height - 1, 5, 5);
                } else {
                    g2d.fillRect(r.x, r.y, r.width, r.height - 1);
                }
                g2d.setComposite(AlphaComposite.SrcOver);
            }
            if (border != null) {
                g2d.setColor(border);
                if (rsta.getAnimateBracketMatching()) {
                    g2d.drawRoundRect(r.x, r.y, r.width, r.height - 1, 5, 5);
                } else {
                    g2d.drawRect(r.x, r.y, r.width, r.height - 1);
                }
            }
        } finally {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
        }
    }
}
