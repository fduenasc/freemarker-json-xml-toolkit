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

package co.com.leronarenwino.i18n;

import co.com.leronarenwino.UiTextKeys;
import utils.SettingsSingleton;

import static co.com.leronarenwino.settings.SettingsNav.CATEGORY_APPEARANCE;
import static co.com.leronarenwino.settings.SettingsNav.CATEGORY_EDITOR;
import static co.com.leronarenwino.settings.SettingsNav.CATEGORY_FREEMARKER;
import static co.com.leronarenwino.settings.SettingsNav.PAGE_APPEARANCE;
import static co.com.leronarenwino.settings.SettingsNav.PAGE_FREEMARKER;
import static co.com.leronarenwino.settings.SettingsNav.PAGE_SYNTAX;

public final class UiMessages {

    private UiMessages() {
    }

    private static boolean es() {
        return "es".equals(SettingsSingleton.getUiLanguage());
    }

    public static String windowTitle() {
        return es()
                ? "Kit FreeMarker JSON/XML (Apache FreeMarker 2.3.34)"
                : "FreeMarker JSON/XML Toolkit (Apache FreeMarker 2.3.34)";
    }

    public static String menuFile() {
        return es() ? "Archivo" : "File";
    }

    public static String menuExit() {
        return es() ? "Salir" : "Exit";
    }

    public static String menuSettings() {
        return es() ? "Configuración…" : "Settings…";
    }

    public static String menuView() {
        return es() ? "Ver" : "View";
    }

    public static String menuShowExpectedFields() {
        return es() ? "Mostrar panel de campos esperados" : "Show Expected Fields Panel";
    }

    public static String panelTemplate() {
        return es() ? "Plantilla" : "Template";
    }

    public static String panelDataModel() {
        return es() ? "Modelo de datos" : "Data Model";
    }

    public static String panelExpectedFields() {
        return es() ? "Campos esperados" : "Expected fields";
    }

    public static String panelRenderedResult() {
        return es() ? "Resultado renderizado" : "Rendered Result";
    }

    public static String templateOk() {
        return es() ? "Plantilla correcta" : "Template OK";
    }

    public static String invalidTemplate() {
        return es() ? "Plantilla no válida" : "Invalid template";
    }

    public static String validJson() {
        return es() ? "JSON válido" : "Valid JSON";
    }

    public static String invalidJson() {
        return es() ? "JSON no válido" : "Invalid JSON";
    }

    public static String templateSyntaxError() {
        return es() ? "Error de sintaxis en la plantilla" : "Template syntax error";
    }

    public static String line() {
        return es() ? "línea" : "line";
    }

    public static String column() {
        return es() ? "columna" : "column";
    }

    public static String footerLineAbbrev() {
        return es() ? "Ln" : "Ln";
    }

    public static String footerColAbbrev() {
        return es() ? "Col" : "Col";
    }

    public static String errorProcessingTemplate() {
        return es() ? "Error al procesar la plantilla: " : "Error processing template: ";
    }

    public static String resolveDataModelMessage(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        if (UiTextKeys.JSON_DATA_MODEL_NULL_ROOT.equals(raw)) {
            return es()
                    ? "Sugerencia: use {} como raíz en lugar de null para el modelo de datos"
                    : "Tip: use {} as root instead of null for the data model";
        }
        if (UiTextKeys.JSON_DATA_MODEL_NEED_OBJECT.equals(raw)) {
            return es()
                    ? "Sugerencia: la raíz debe ser un objeto JSON { ... } en esta aplicación"
                    : "Tip: root should be a JSON object { ... } for this app";
        }
        if (UiTextKeys.JSON_PARSE_FALLBACK.equals(raw)) {
            return invalidJson();
        }
        return raw;
    }

    public static String invalidJsonDialogPrefix() {
        return es() ? "JSON no válido: " : "Invalid JSON: ";
    }

    public static String formatJsonErrorTitle() {
        return es() ? "Error al formatear JSON" : "Format JSON Error";
    }

    public static String dataModelErrorTitle() {
        return es() ? "Error en el modelo de datos" : "Data Model Error";
    }

    public static String restoreLastValid() {
        return es() ? "Restaurar último válido" : "Restore last valid";
    }

    public static String close() {
        return es() ? "Cerrar" : "Close";
    }

    public static String jsonFormatErrorBody(String detail) {
        String head = es() ? "JSON no válido:\n\n" : "Invalid JSON:\n\n";
        return head + (detail != null ? detail : "");
    }

    public static String themeNotFound(String path) {
        return (es() ? "No se encontró el recurso del tema: " : "Theme resource not found: ")
                + path;
    }

    public static String themeLoadError(String msg) {
        return (es() ? "Error al cargar el tema: " : "Error loading theme: ") + msg;
    }

    public static String toggleLineWrapTooltip() {
        return es() ? "Alternar ajuste de línea" : "Toggle line wrap";
    }

    public static String formatTemplateAccessible() {
        return es() ? "Formatear plantilla" : "Format Template";
    }

    public static String formatTemplateTooltip() {
        return formatTemplateAccessible();
    }

    public static String singleLineAccessible() {
        return es() ? "Convertir a una sola línea" : "Convert to Single Line";
    }

    public static String singleLineTooltip() {
        return singleLineAccessible();
    }

    public static String formatDataModelAccessible() {
        return es() ? "Formatear JSON del modelo de datos" : "Format data model JSON";
    }

    public static String formatDataModelTooltip() {
        return es()
                ? "JSON con sangría (si el JSON no es válido se muestra un error)"
                : "Pretty-print JSON (invalid JSON shows an error)";
    }

    public static String processTemplateAccessible() {
        return es() ? "Evaluar la plantilla con los datos" : "Evaluate the template with data";
    }

    public static String processTemplateTooltip() {
        return processTemplateAccessible();
    }

    public static String formatOutputJsonAccessible() {
        return es() ? "Formatear salida como JSON" : "Format output as JSON";
    }

    public static String formatOutputJsonTooltip() {
        return formatOutputJsonAccessible();
    }

    public static String clearOutputAccessible() {
        return es() ? "Borrar área de salida" : "Clear output area";
    }

    public static String clearOutputTooltip() {
        return clearOutputAccessible();
    }

    public static String validateExpectedFieldsAccessible() {
        return es() ? "Validar campos esperados" : "Validate Expected Fields";
    }

    public static String validateExpectedFieldsTooltip() {
        return validateExpectedFieldsAccessible();
    }

    public static String expectedFieldsConfigureButton() {
        return es() ? "Configurar…" : "Configure…";
    }

    public static String configureExpectedFieldsAccessible() {
        return es() ? "Configurar campos esperados" : "Configure expected fields";
    }

    public static String configureExpectedFieldsTooltip() {
        return es()
                ? "Abrir el editor de campos (tabla, importación masiva y persistencia)"
                : "Open field editor (table, bulk import, and persistence)";
    }

    public static String expectedFieldsDialogTitle() {
        return es() ? "Campos esperados en la salida" : "Expected output fields";
    }

    public static String expectedFieldsTabTable() {
        return es() ? "Tabla" : "Table";
    }

    public static String expectedFieldsTabImport() {
        return es() ? "Importar texto" : "Import text";
    }

    public static String columnExpectedFieldIndex() {
        return "#";
    }

    public static String columnExpectedFieldPath() {
        return es() ? "Ruta (puntos)" : "Path (dot notation)";
    }

    public static String columnExpectedFieldType() {
        return es() ? "Tipo (opcional)" : "Type (optional)";
    }

    public static String expectedFieldsTableLegend() {
        return es()
                ? "Cada fila es un campo. Columnas: n.º de fila, ruta JSON (puntos), tipo JSON opcional."
                : "Each row is one field. Columns: row number, JSON path (dots), optional JSON type.";
    }

    public static String expectedFieldsAddRow() {
        return es() ? "Añadir" : "Add";
    }

    public static String expectedFieldsModifyRow() {
        return es() ? "Modificar" : "Edit";
    }

    public static String expectedFieldsModifyRowTooltip() {
        return es()
                ? "Editar la fila seleccionada (ruta y tipo)"
                : "Edit the selected row (path and type)";
    }

    public static String expectedFieldsDeleteRow() {
        return es() ? "Eliminar" : "Delete";
    }

    public static String expectedFieldsDeleteRowTooltip() {
        return es() ? "Eliminar las filas seleccionadas" : "Delete the selected rows";
    }

    public static String expectedFieldsSave() {
        return es() ? "Guardar" : "Save";
    }

    public static String expectedFieldsSaveTooltip() {
        return es()
                ? "Guardar la lista en la configuración (archivo config.properties)"
                : "Save the list to settings (config.properties file)";
    }

    public static String expectedFieldsSavedMessage() {
        return es() ? "Lista guardada correctamente." : "List saved successfully.";
    }

    public static String expectedFieldsClose() {
        return es() ? "Cerrar" : "Close";
    }

    public static String expectedFieldsUnsavedTitle() {
        return es() ? "Cambios sin guardar" : "Unsaved changes";
    }

    public static String expectedFieldsUnsavedMessage() {
        return es()
                ? "Hay cambios que no se han guardado. ¿Cerrar sin guardar?"
                : "You have unsaved changes. Close without saving?";
    }

    public static String expectedFieldsSelectRowToEdit() {
        return es() ? "Seleccione una fila para modificar." : "Select a row to edit.";
    }

    public static String expectedFieldsSelectRowToDelete() {
        return es() ? "Seleccione al menos una fila para eliminar." : "Select at least one row to delete.";
    }

    public static String expectedFieldsLoadFromText() {
        return es() ? "Sustituir tabla por este texto" : "Replace table from text";
    }

    public static String expectedFieldsImportHelp() {
        return es()
                ? "Separe entradas con comas, espacios o saltos de línea. Tipo opcional: ruta:tipo (string, number, boolean, object, array, null)."
                : "Separate entries with commas, spaces, or newlines. Optional type: path:type (string, number, boolean, object, array, null).";
    }

    public static String expectedFieldsSummaryNone() {
        return es() ? "Ningún campo configurado" : "No fields configured";
    }

    public static String expectedFieldsSummaryOne() {
        return es() ? "1 campo configurado" : "1 field configured";
    }

    public static String expectedFieldsSummaryMany(int n) {
        return es() ? n + " campos configurados" : n + " fields configured";
    }

    public static String validationResultPlaceholder() {
        return es() ? "El resultado de la validación aparecerá aquí" : "Validation result will appear here";
    }

    public static String noExpectedFieldsSpecified() {
        return es() ? "No hay campos esperados definidos" : "No expected fields specified";
    }

    public static String allExpectedFieldsPresent() {
        return es() ? "Todos los campos esperados están presentes" : "All expected fields are present";
    }

    public static String missingFieldsPrefix() {
        return es() ? "Faltan campos: " : "Missing fields: ";
    }

    public static String invalidJsonOutput() {
        return es() ? "Salida JSON no válida" : "Invalid JSON output";
    }

    public static String caretLineColumn(int line, int col) {
        return footerLineAbbrev() + " " + line + ", " + footerColAbbrev() + " " + col;
    }

    public static String caretUnknown() {
        return footerLineAbbrev() + " ?, " + footerColAbbrev() + " ?";
    }

    public static String settingsDialogTitle() {
        return es() ? "Configuración" : "Settings";
    }

    public static String tabEditor() {
        return es() ? "Editor" : "Editor";
    }

    public static String tabSyntaxTheme() {
        return es() ? "Tema de sintaxis" : "Syntax Theme";
    }

    public static String tabFreemarker() {
        return es() ? "FreeMarker" : "FreeMarker";
    }

    /** Category or root row in the settings navigation tree (IntelliJ-style sidebar). */
    public static String settingsCategoryTitle(String categoryKey) {
        return switch (categoryKey) {
            case CATEGORY_APPEARANCE -> es() ? "Apariencia y comportamiento" : "Appearance & Behavior";
            case CATEGORY_EDITOR -> es() ? "Editor" : "Editor";
            case CATEGORY_FREEMARKER -> "FreeMarker";
            default -> categoryKey;
        };
    }

    /** Leaf row title under a category. */
    public static String settingsPageTitle(String pageKey) {
        return switch (pageKey) {
            case PAGE_APPEARANCE -> es() ? "Apariencia" : "Appearance";
            case PAGE_SYNTAX -> es() ? "Resaltado de sintaxis" : "Syntax highlighting";
            case PAGE_FREEMARKER -> es() ? "General" : "General";
            default -> pageKey;
        };
    }

    /** Breadcrumb above the settings detail panel (category › page). */
    public static String settingsBreadcrumb(String pageKey) {
        return switch (pageKey) {
            case PAGE_APPEARANCE ->
                    es() ? "Apariencia y comportamiento › Apariencia" : "Appearance & Behavior › Appearance";
            case PAGE_SYNTAX -> es() ? "Editor › Resaltado de sintaxis" : "Editor › Syntax highlighting";
            case PAGE_FREEMARKER -> es() ? "FreeMarker › General" : "FreeMarker › General";
            default -> "";
        };
    }

    public static String settingsSearchPlaceholder() {
        return es() ? "Buscar" : "Search";
    }

    public static String settingsSearchTooltip() {
        return es()
                ? "Buscar en la lista de opciones"
                : "Search options in the list";
    }

    public static String settingsHelpTooltip() {
        return es() ? "Ayuda" : "Help";
    }

    public static String settingsHelpMessage() {
        return es()
                ? "Use el panel izquierdo para elegir una sección. Los cambios en tema o sintaxis se aplican al pulsar Aplicar o Aceptar."
                : "Use the left panel to pick a section. Theme and syntax changes apply when you click Apply or OK.";
    }

    public static String settingsRestoreDefaults() {
        return es() ? "Restaurar valores por defecto" : "Restore defaults";
    }

    public static String settingsRestoreDefaultsTooltip() {
        return es()
                ? "Volver a tema IntelliJ, inglés, IDEA, locale en_US y UTC (use Aplicar o Aceptar para guardar)"
                : "Reset to IntelliJ theme, English, IDEA scheme, en_US locale, and UTC (click Apply or OK to save)";
    }

    public static String settingsRestoreDefaultsConfirm() {
        return es()
                ? "¿Restaurar en esta ventana los valores por defecto de la aplicación? (Aún no se guardan en el disco hasta Aplicar o Aceptar.)"
                : "Restore default values in this dialog? (Nothing is saved to disk until you click Apply or OK.)";
    }

    public static String settingsSearchNoResults() {
        return es() ? "Sin coincidencias" : "No matches";
    }

    public static String labelAppTheme() {
        return es() ? "Tema de la interfaz:" : "Theme:";
    }

    public static String labelRsyntaxTheme() {
        return es() ? "Tema RSyntax:" : "RSyntax Theme:";
    }

    public static String labelUiLanguage() {
        return es() ? "Idioma de la interfaz:" : "UI language:";
    }

    public static String labelLocale() {
        return es() ? "Configuración regional:" : "Locale:";
    }

    public static String labelTimeZone() {
        return es() ? "Zona horaria:" : "Time zone:";
    }

    public static String buttonOk() {
        return es() ? "Aceptar" : "OK";
    }

    public static String buttonCancel() {
        return es() ? "Cancelar" : "Cancel";
    }

    public static String buttonApply() {
        return es() ? "Aplicar" : "Apply";
    }

    public static String themeApplyFailed(String msg) {
        return (es() ? "No se pudo aplicar el tema: " : "Failed to apply theme: ") + msg;
    }

    public static String findSearchPlaceholder() {
        return es() ? "Buscar" : "Search";
    }

    public static String findReplacePlaceholder() {
        return es() ? "Reemplazar" : "Replace";
    }

    public static String findSearchFieldTooltip() {
        return es() ? "Texto a buscar" : "Text to search for";
    }

    public static String findRegexTooltip() {
        return es() ? "Activar búsqueda con expresiones regulares" : "Enable regular expression search";
    }

    public static String findMatchCaseTooltip() {
        return es() ? "Coincidir mayúsculas y minúsculas" : "Match case";
    }

    public static String findPrevTooltip() {
        return es() ? "Coincidencia anterior" : "Previous match";
    }

    public static String findNextTooltip() {
        return es() ? "Siguiente coincidencia" : "Next match";
    }

    public static String findCloseTooltip() {
        return es() ? "Cerrar" : "Close";
    }

    public static String findReplaceFieldTooltip() {
        return es() ? "Texto de reemplazo" : "Text to replace with";
    }

    public static String replaceButton() {
        return es() ? "Reemplazar" : "Replace";
    }

    public static String replaceCurrentTooltip() {
        return es() ? "Reemplazar la coincidencia actual" : "Replace current match";
    }

    public static String replaceAllButton() {
        return es() ? "Reemplazar todo" : "Replace All";
    }

    public static String replaceAllTooltip() {
        return es() ? "Reemplazar todas las coincidencias" : "Replace all matches";
    }

    public static String findZeroResults() {
        return es() ? "0 resultados" : "0 results";
    }
}
