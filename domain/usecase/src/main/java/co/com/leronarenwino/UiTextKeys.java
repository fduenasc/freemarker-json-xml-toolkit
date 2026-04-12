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

package co.com.leronarenwino;

/**
 * Stable keys for UI copy resolved in the GUI layer from the configured UI language.
 */
public final class UiTextKeys {

    private static final char SEP = '\u001e';

    public static final String JSON_DATA_MODEL_NULL_ROOT = "uitext.json.data_model_null_root";
    public static final String JSON_DATA_MODEL_NEED_OBJECT = "uitext.json.data_model_need_object";
    public static final String JSON_PARSE_FALLBACK = "uitext.json.parse_fallback";

    /** Prefix for {@link #jsonFormatFailurePayload(String)}. */
    public static final String JSON_FORMAT_FAILED = "uitext.json.format_failed";

    private UiTextKeys() {
    }

    public static String jsonFormatFailurePayload(String innerMessage) {
        return JSON_FORMAT_FAILED + SEP + (innerMessage != null ? innerMessage : "");
    }

    public static boolean isJsonFormatFailure(String message) {
        return message != null && message.startsWith(JSON_FORMAT_FAILED + SEP);
    }

    public static String jsonFormatFailureDetail(String message) {
        if (!isJsonFormatFailure(message)) {
            return message;
        }
        return message.substring((JSON_FORMAT_FAILED + SEP).length());
    }
}
