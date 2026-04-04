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

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.text.BadLocationException;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Applies the same fields {@link RSyntaxTextArea} updates in {@code doBracketMatching()} so that
 * {@code {[()]}} highlights paint in the correct layer (behind glyphs). Package-private API of RSyntaxTextArea.
 */
final class RSyntaxBracketStateAccessor {

    private static final Field BRACKET_INFO;
    private static final Field MATCH;
    private static final Field DOT_RECT;
    private static final Field LAST_BRACKET_MATCH_POS;
    private static final Field BRACKET_REPAINT_TIMER;

    static {
        try {
            BRACKET_INFO = RSyntaxTextArea.class.getDeclaredField("bracketInfo");
            MATCH = RSyntaxTextArea.class.getDeclaredField("match");
            DOT_RECT = RSyntaxTextArea.class.getDeclaredField("dotRect");
            LAST_BRACKET_MATCH_POS = RSyntaxTextArea.class.getDeclaredField("lastBracketMatchPos");
            BRACKET_REPAINT_TIMER = RSyntaxTextArea.class.getDeclaredField("bracketRepaintTimer");
            BRACKET_INFO.setAccessible(true);
            MATCH.setAccessible(true);
            DOT_RECT.setAccessible(true);
            LAST_BRACKET_MATCH_POS.setAccessible(true);
            BRACKET_REPAINT_TIMER.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private RSyntaxBracketStateAccessor() {
    }

    static void clearBracketHighlightState(RSyntaxTextArea ta) {
        try {
            setField(ta, MATCH, null);
            setField(ta, DOT_RECT, null);
            Point bi = (Point) BRACKET_INFO.get(ta);
            if (bi == null) {
                bi = new Point(-1, -1);
                BRACKET_INFO.set(ta, bi);
            } else {
                bi.setLocation(-1, -1);
            }
            LAST_BRACKET_MATCH_POS.setInt(ta, -1);
            Object timer = BRACKET_REPAINT_TIMER.get(ta);
            if (timer != null) {
                Method stop = timer.getClass().getMethod("stop");
                stop.invoke(timer);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Mirrors successful {@code doBracketMatching} updates (without popup / animation restart beyond stop+optional).
     */
    static void applyStandardMatch(RSyntaxTextArea ta, Point bracketInfo, boolean paintPair) {
        try {
            if (bracketInfo == null || bracketInfo.y < 0) {
                clearBracketHighlightState(ta);
                return;
            }
            Point bi = (Point) BRACKET_INFO.get(ta);
            if (bi == null) {
                bi = new Point();
                BRACKET_INFO.set(ta, bi);
            }
            bi.setLocation(bracketInfo.x, bracketInfo.y);

            Rectangle matchRect = ta.modelToView(bracketInfo.y);
            setField(ta, MATCH, matchRect);
            if (paintPair) {
                setField(ta, DOT_RECT, ta.modelToView(bracketInfo.x));
            } else {
                setField(ta, DOT_RECT, null);
            }
            LAST_BRACKET_MATCH_POS.setInt(ta, bracketInfo.y);

            Object timer = BRACKET_REPAINT_TIMER.get(ta);
            if (timer != null && ta.getAnimateBracketMatching()) {
                Method stop = timer.getClass().getMethod("stop");
                stop.invoke(timer);
                Method restart = timer.getClass().getMethod("restart");
                restart.invoke(timer);
            }
        } catch (ReflectiveOperationException | BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setField(RSyntaxTextArea ta, Field f, Object value) throws IllegalAccessException {
        f.set(ta, value);
    }
}
