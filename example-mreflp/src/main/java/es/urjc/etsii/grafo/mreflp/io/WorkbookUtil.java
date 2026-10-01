package es.urjc.etsii.grafo.mreflp.io;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Reads only the primary top-left table, stopping at its first empty instance cell. */
public final class WorkbookUtil {
    private WorkbookUtil() {}
    public static List<PublishedReference> read(Path workbook) throws IOException {
        var references = new ArrayList<PublishedReference>();
        var keys = new HashSet<String>();
        try (var input = Files.newInputStream(workbook); var book = new XSSFWorkbook(input)) {
            for (int table = 1; table <= 16; table++) {
                String sheetName = "Table A." + table;
                Sheet sheet = Objects.requireNonNull(book.getSheet(sheetName), "Missing " + sheetName);
                int capacity = 2 + (table - 1) % 4;
                for (int rowIndex = 2; ; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null || text(row, 0).isBlank()) break;
                    String id = text(row, 0).trim() + "-r" + capacity;
                    if (table <= 8) {
                        add(references, keys, row, id, "BKV", 1, -1, -1, sheetName, false);
                        add(references, keys, row, id, "re-ILP", 2, -1, 3, sheetName, false);
                        add(references, keys, row, id, "SDP", 4, -1, 5, sheetName, false);
                        add(references, keys, row, id, "AMA2", 6, -1, 7, sheetName, false);
                        add(references, keys, row, id, "GRASP", 8, -1, 9, sheetName, false);
                        add(references, keys, row, id, "LMLS", 10, -1, 11, sheetName, false);
                        add(references, keys, row, id, "LMLS_relaxed", 12, 13, 14, sheetName, false);
                    } else if (table <= 12) {
                        add(references, keys, row, id, "BKV", 1, -1, -1, sheetName, false);
                        add(references, keys, row, id, "re-ILP", 2, -1, 3, sheetName, false);
                        add(references, keys, row, id, "GRASP", 4, -1, 5, sheetName, false);
                        boolean flagged = table == 9 && id.startsWith("sko100_");
                        add(references, keys, row, id, "LMLS", 6, -1, 7, sheetName, flagged);
                        add(references, keys, row, id, "LMLS_relaxed", 8, 9, 10, sheetName, false);
                    } else {
                        add(references, keys, row, id, "re-ILP", 1, -1, 2, sheetName, false);
                        add(references, keys, row, id, "GRASP", 3, -1, 4, sheetName, false);
                        add(references, keys, row, id, "GRASP_relaxed", 5, 6, 7, sheetName, false);
                        add(references, keys, row, id, "LMLS", 8, -1, 9, sheetName, false);
                        add(references, keys, row, id, "LMLS_relaxed", 10, 11, 12, sheetName, false);
                    }
                }
            }
        }
        return List.copyOf(references);
    }

    private static void add(List<PublishedReference> references, Set<String> keys, Row row, String id, String method,
                            int scoreColumn, int averageColumn, int timeColumn, String sheet, boolean flagged) {
        String value = text(row, scoreColumn);
        Double score = number(value);
        if (score == null) return;
        if (score < 0) throw new IllegalArgumentException("Negative objective at " + sheet + ":" + row.getRowNum());
        // Preserve fractional published objectives as source anomalies instead of rounding them.
        flagged |= score != Math.rint(score);
        if (!keys.add(id + ":" + method)) throw new IllegalArgumentException("Duplicate reference " + id + ":" + method);
        String cell = org.apache.poi.ss.util.CellReference.convertNumToColString(scoreColumn) + (row.getRowNum() + 1);
        references.add(new PublishedReference(id, method, score, number(text(row, averageColumn)), number(text(row, timeColumn)),
                value.endsWith("*"), sheet, cell, flagged));
    }
    private static String text(Row row, int column) {
        if (column < 0 || row.getCell(column) == null) return "";
        Cell cell = row.getCell(column);
        CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        return switch (type) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> Double.toString(cell.getNumericCellValue());
            case BLANK -> "";
            default -> throw new IllegalArgumentException("Unexpected workbook cell " + cell.getAddress());
        };
    }
    private static Double number(String value) {
        if (value.isBlank() || value.equals("-") || value.equals("n/a")) return null;
        double number = Double.parseDouble(value.replace("*", ""));
        if (!Double.isFinite(number)) throw new IllegalArgumentException("Nonfinite reference");
        return number;
    }
}
