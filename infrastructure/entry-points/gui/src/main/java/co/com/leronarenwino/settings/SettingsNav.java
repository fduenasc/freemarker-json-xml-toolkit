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

package co.com.leronarenwino.settings;

/**
 * Stable keys for the IntelliJ-style settings navigation tree and {@link java.awt.CardLayout} pages.
 */
public final class SettingsNav {

    public static final String CATEGORY_APPEARANCE = "cat.appearance_behavior";
    public static final String CATEGORY_EDITOR = "cat.editor";
    public static final String CATEGORY_FREEMARKER = "cat.freemarker";

    public static final String PAGE_APPEARANCE = "page.appearance";
    public static final String PAGE_SYNTAX = "page.syntax";
    public static final String PAGE_FREEMARKER = "page.freemarker";

    private SettingsNav() {
    }

    public record CategoryRef(String key) {
    }

    public record PageRef(String cardKey) {
    }
}
