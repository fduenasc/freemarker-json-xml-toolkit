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

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplateValidator {

    /**
     * Result of checking data-model JSON syntax (and a shallow structural hint for FreeMarker maps).
     *
     * @param syntaxValid {@code false} when {@link com.fasterxml.jackson.core.JsonProcessingException}
     * @param message     empty when OK; parse error text or a short tip when syntax is valid but root is not an object
     * @param line        1-based line from Jackson, or {@code -1} if unknown
     * @param column      1-based column from Jackson, or {@code -1} if unknown
     */
    public record JsonSyntaxCheck(boolean syntaxValid, String message, int line, int column) {
    }

    /**
     * First JSON syntax error in the full editor buffer (do not trim), for squiggles and caret placement.
     *
     * @param charOffset character offset from Jackson, or {@code -1} if unknown
     */
    public record EditorJsonSyntaxFailure(String message, int line1Based, int column1Based, long charOffset) {

        public boolean hasCharOffset() {
            return charOffset >= 0;
        }
    }

    private final TemplateProcessor templateProcessor;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TemplateValidator(TemplateProcessor templateProcessor) {
        this.templateProcessor = templateProcessor;
    }

    public String processTemplate(String templateContent, Map<String, Object> dataModel) throws Exception {
        return templateProcessor.processTemplate(templateContent, dataModel);
    }

    public static List<String> validateFieldsPresentWithTypes(String jsonOutput, String[] expectedFields) throws Exception {
        List<String> missing = new ArrayList<>(expectedFields.length);
        JsonNode jsonNode = MAPPER.readTree(jsonOutput);
        for (String field : expectedFields) {
            if (field.isEmpty()) continue;
            String[] parts = field.split(":");
            String fieldPath = parts[0];
            String expectedType = parts.length > 1 ? parts[1].toLowerCase() : null;
            JsonNode valueNode = getNestedField(jsonNode, fieldPath);
            if (valueNode == null || valueNode.isMissingNode()) {
                missing.add(field);
            } else if (expectedType != null && !matchesType(valueNode, expectedType)) {
                missing.add(field + " (Type mismatch)");
            }
        }
        return missing;
    }

    private static JsonNode getNestedField(JsonNode node, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        JsonNode current = node;
        for (String part : parts) {
            if (current == null || !current.has(part)) {
                return null;
            }
            current = current.get(part);
        }
        return current;
    }

    private static boolean matchesType(JsonNode node, String type) {
        return switch (type) {
            case "string" -> node.isTextual();
            case "number" -> node.isNumber();
            case "boolean" -> node.isBoolean();
            case "object" -> node.isObject();
            case "array" -> node.isArray();
            case "null" -> node.isNull();
            default -> false;
        };
    }

    public static Map<String, Object> parseJsonToDataModel(String json) throws Exception {
        return MAPPER.readValue(json, new TypeReference<>() {
        });
    }

    /**
     * Parses JSON without mutating the source; suitable for live validation of the data model editor.
     */
    public static JsonSyntaxCheck checkDataModelJsonSyntax(String json) {
        String s = json == null ? "" : json.trim();
        if (s.isEmpty()) {
            return new JsonSyntaxCheck(true, "", -1, -1);
        }
        try {
            JsonNode root = MAPPER.readTree(s);
            if (root.isObject()) {
                return new JsonSyntaxCheck(true, "", -1, -1);
            }
            if (root.isNull()) {
                return new JsonSyntaxCheck(true, "Tip: use {} as root instead of null for the data model", -1, -1);
            }
            return new JsonSyntaxCheck(true, "Tip: root should be a JSON object { ... } for this app", -1, -1);
        } catch (JsonProcessingException e) {
            JsonLocation loc = e.getLocation();
            int line = loc != null ? loc.getLineNr() : -1;
            int col = loc != null ? loc.getColumnNr() : -1;
            String msg = e.getOriginalMessage() != null ? e.getOriginalMessage() : e.getMessage();
            return new JsonSyntaxCheck(false, msg != null ? msg : "Invalid JSON", line, col);
        }
    }

    /**
     * Locates the first parse error in {@code json} exactly as stored in the editor (leading/trailing
     * whitespace preserved so {@link JsonLocation#getCharOffset()} matches document positions).
     *
     * @return {@code null} when empty (after trim), whitespace-only, or syntactically valid JSON
     */
    public static EditorJsonSyntaxFailure findJsonSyntaxFailureInFullText(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        if (json.trim().isEmpty()) {
            return null;
        }
        try {
            MAPPER.readTree(json);
            return null;
        } catch (JsonProcessingException e) {
            String msg = e.getOriginalMessage() != null ? e.getOriginalMessage() : e.getMessage();
            JsonLocation loc = e.getLocation();
            if (loc == null) {
                return new EditorJsonSyntaxFailure(msg != null ? msg : "Invalid JSON", -1, -1, -1);
            }
            return new EditorJsonSyntaxFailure(
                    msg != null ? msg : "Invalid JSON",
                    loc.getLineNr(),
                    loc.getColumnNr(),
                    loc.getCharOffset());
        }
    }

    public static String formatFlexibleJson(String input) {
        try {
            Object json = MAPPER.readValue(input, Object.class);
            String pretty = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            return pretty.replace("\r\n", "\n");
        } catch (Exception e1) {
            try {
                String toParse = input;
                if (toParse.trim().startsWith("{") && toParse.contains("\\\"") && !toParse.trim().startsWith("\"")) {
                    toParse = "\"" + toParse + "\"";
                }
                String unescaped = MAPPER.readValue(toParse, String.class);
                Object json = MAPPER.readValue(unescaped, Object.class);
                String pretty = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                return pretty.replace("\r\n", "\n");
            } catch (Exception e2) {
                throw new IllegalArgumentException("El JSON es inválido:\n\n" + e2.getMessage());
            }
        }
    }

    public static String formatFreemarkerTemplateCombined(String template) {
        // Step 1: Add line breaks after FreeMarker directives
        String formatted = template.replaceAll("(<#.*?>)", "$1\n");

        // Step 2: Pretty-print map assignments inside <#assign ... = {...}>
        StringBuilder sb = new StringBuilder();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(<#assign\\s+\\w+\\s*=\\s*)\\{([^}]*)}");
        java.util.regex.Matcher matcher = pattern.matcher(formatted);
        int lastEnd = 0;
        while (matcher.find()) {
            sb.append(formatted, lastEnd, matcher.start());
            String before = matcher.group(1);
            String mapBody = matcher.group(2).trim();
            // Split entries by comma, trim, and join with a single line break and indentation
            String[] entries = mapBody.split("\\s*,\\s*");
            StringBuilder mapFormatted = new StringBuilder("{\n");
            for (int i = 0; i < entries.length; i++) {
                mapFormatted.append("    ").append(entries[i]);
                if (i < entries.length - 1) {
                    mapFormatted.append(",");
                }
                mapFormatted.append("\n");
            }
            mapFormatted.append("}");
            sb.append(before).append(mapFormatted);
            lastEnd = matcher.end();
        }
        sb.append(formatted.substring(lastEnd));
        // Step 3: Remove extra blank lines and trim
        return sb.toString().replaceAll("[\\n\\r]+", "\n").replaceAll("\\n{2,}", "\n").trim();
    }

    public static String toSingleLine(String template) {
        if (template == null) return "";
        // Remove all whitespace between > and <
        String noSpacesBetweenTags = template.replaceAll(">\\s+<", "><");

        // Remove spaces inside {...} blocks only
        StringBuilder result = new StringBuilder();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]*)}");
        java.util.regex.Matcher matcher = pattern.matcher(noSpacesBetweenTags);
        int lastEnd = 0;
        while (matcher.find()) {
            result.append(noSpacesBetweenTags, lastEnd, matcher.start());
            String content = matcher.group(1)
                    .replaceAll("\\s*,\\s*", ",")
                    .replaceAll("\\s*:\\s*", ":")
                    .replaceAll("^\\s+|\\s+$", "");
            result.append("{").append(content).append("}");
            lastEnd = matcher.end();
        }
        result.append(noSpacesBetweenTags.substring(lastEnd));

        // Remove line breaks and collapse multiple spaces to one
        return result.toString().replaceAll("[\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
    }

}