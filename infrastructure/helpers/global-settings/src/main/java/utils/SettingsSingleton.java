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

package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class SettingsSingleton {

    private SettingsSingleton() {
    }

    public static final String FREEMARKER_LOCALE = "locale";
    public static final String FREEMARKER_TIME_ZONE = "time_zone";

    private static final String DEFAULT_LOCALE = "en_US";
    private static final String DEFAULT_TIME_ZONE = "UTC";

    private static String locale = DEFAULT_LOCALE;
    private static String timeZone = DEFAULT_TIME_ZONE;

    public static final String APP_THEME = "theme";
    /** FlatLaf look-and-feel name stored in {@code config.properties}. */
    public static final String DEFAULT_THEME = "Flat IntelliJ";

    private static final Set<String> APP_THEMES = Set.of(
            "Flat Light", "Flat Dark", "Flat IntelliJ", "Flat Darcula");

    private static String theme = DEFAULT_THEME;

    public static final String UI_LANGUAGE = "ui_language";
    private static final String DEFAULT_UI_LANGUAGE = "en";

    private static String uiLanguage = DEFAULT_UI_LANGUAGE;

    public static final String RSYNTAX_THEME = "rsyntax_theme";
    private static final String DEFAULT_RSYNTAX_THEME = "idea.xml";

    private static String rsyntaxTheme = DEFAULT_RSYNTAX_THEME;

    public static final String EXPECTED_FIELDS_VISIBLE = "expected_fields_visible";
    private static final boolean DEFAULT_EXPECTED_FIELDS_VISIBLE = true;
    private static boolean expectedFieldsVisible = DEFAULT_EXPECTED_FIELDS_VISIBLE;

    /**
     * Newline-separated entries for output validation ({@code path} or {@code path:type}), persisted in
     * {@code config.properties}.
     */
    public static final String EXPECTED_FIELDS_LIST = "expected_fields_list";

    private static final List<String> expectedFieldEntries = new ArrayList<>();

    public static Properties defaultAppProperties() {
        Properties properties = new Properties();
        properties.setProperty(FREEMARKER_LOCALE, DEFAULT_LOCALE);
        properties.setProperty(FREEMARKER_TIME_ZONE, DEFAULT_TIME_ZONE);
        properties.setProperty(APP_THEME, DEFAULT_THEME);
        properties.setProperty(RSYNTAX_THEME, DEFAULT_RSYNTAX_THEME);
        properties.setProperty(UI_LANGUAGE, DEFAULT_UI_LANGUAGE);
        properties.setProperty(EXPECTED_FIELDS_VISIBLE, String.valueOf(DEFAULT_EXPECTED_FIELDS_VISIBLE));
        return properties;
    }

    public static String getRSyntaxTheme() {
        return rsyntaxTheme;
    }

    public static void setRSyntaxTheme(String theme) {
        if (theme != null && !theme.isEmpty()) {
            rsyntaxTheme = theme;
        }
    }

    public static String getTheme() {
        return theme;
    }

    public static void setTheme(String newTheme) {
        if (newTheme != null && APP_THEMES.contains(newTheme)) {
            theme = newTheme;
        }
    }

    /**
     * Maps legacy {@code theme=Dark}/{@code Light} and unknown values to a supported FlatLaf theme name.
     */
    public static String normalizeAppTheme(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_THEME;
        }
        if (APP_THEMES.contains(raw)) {
            return raw;
        }
        if ("Dark".equals(raw)) {
            return "Flat Dark";
        }
        if ("Light".equals(raw)) {
            return "Flat Light";
        }
        return DEFAULT_THEME;
    }

    public static void setSettingsFromProperties(Properties properties) {
        locale = properties.getProperty(FREEMARKER_LOCALE, DEFAULT_LOCALE);
        timeZone = properties.getProperty(FREEMARKER_TIME_ZONE, DEFAULT_TIME_ZONE);
        theme = normalizeAppTheme(properties.getProperty(APP_THEME, DEFAULT_THEME));
        rsyntaxTheme = properties.getProperty(RSYNTAX_THEME, DEFAULT_RSYNTAX_THEME);
        uiLanguage = normalizeUiLanguage(properties.getProperty(UI_LANGUAGE, DEFAULT_UI_LANGUAGE));
        expectedFieldsVisible = Boolean.parseBoolean(properties.getProperty(EXPECTED_FIELDS_VISIBLE, String.valueOf(DEFAULT_EXPECTED_FIELDS_VISIBLE)));
        loadExpectedFieldsFromProperty(properties.getProperty(EXPECTED_FIELDS_LIST, ""));
    }

    public static String getUiLanguage() {
        return uiLanguage;
    }

    public static void setUiLanguage(String code) {
        uiLanguage = normalizeUiLanguage(code);
    }

    private static String normalizeUiLanguage(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_UI_LANGUAGE;
        }
        String c = raw.trim().toLowerCase();
        if ("es".equals(c) || c.startsWith("es_")) {
            return "es";
        }
        return "en";
    }

    public static String getLocale() {
        return locale;
    }

    public static String getTimeZone() {
        return timeZone;
    }

    public static boolean isExpectedFieldsVisible() {
        return expectedFieldsVisible;
    }

    public static void setExpectedFieldsVisible(boolean visible) {
        expectedFieldsVisible = visible;
    }

    public static void loadExpectedFieldsFromProperty(String raw) {
        expectedFieldEntries.clear();
        if (raw == null || raw.isBlank()) {
            return;
        }
        for (String line : raw.split("\n", -1)) {
            String t = line.trim();
            if (!t.isEmpty()) {
                expectedFieldEntries.add(t);
            }
        }
    }

    public static String serializeExpectedFieldsList() {
        return String.join("\n", expectedFieldEntries);
    }

    public static List<String> getExpectedFieldEntries() {
        return Collections.unmodifiableList(expectedFieldEntries);
    }

    public static void setExpectedFieldEntries(List<String> entries) {
        expectedFieldEntries.clear();
        if (entries == null) {
            return;
        }
        for (String e : entries) {
            if (e != null) {
                String t = e.trim();
                if (!t.isEmpty()) {
                    expectedFieldEntries.add(t);
                }
            }
        }
    }

    public static String[] expectedFieldsForValidator() {
        return expectedFieldEntries.toArray(new String[0]);
    }

    public static int getExpectedFieldCount() {
        return expectedFieldEntries.size();
    }

}