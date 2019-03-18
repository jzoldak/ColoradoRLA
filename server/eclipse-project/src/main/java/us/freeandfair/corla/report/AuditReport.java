/*
 * Colorado RLA System
 */

package us.freeandfair.corla.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.List;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Generate a POI Excel workbook containing audit report sheets.
 *
 * Sheets include:
 *
 * - Summary
 */
public class AuditReport {
  /**
   * Font size to use for all cells.
   */
  private static final Short FONT_SIZE = 12;

  /**
   * Internal output stream required for writing out the workbook.
   */
  private final ByteArrayOutputStream baos;

  /**
   * Initializes the AuditReport
   */
  public AuditReport() {
    this.baos = new ByteArrayOutputStream();
  }

  /**
   * Given some raw data, generate the actual POI workbook.
   *
   * @param rows the raw data
   * @return the POI workbook ready for output
   */
  private Workbook generateWorkbook(final List<List<String>> rows) {
    final Workbook workbook = new XSSFWorkbook();
    final Sheet summary = workbook.createSheet("Summary");

    final Font boldFont = workbook.createFont();
    final Font regFont = workbook.createFont();

    final CellStyle boldStyle = workbook.createCellStyle();
    final CellStyle regStyle = workbook.createCellStyle();

    regFont.setFontHeightInPoints(FONT_SIZE);
    regStyle.setFont(regFont);

    boldFont.setFontHeightInPoints(FONT_SIZE);
    boldFont.setBold(true);
    boldStyle.setFont(boldFont);

    for (int i = 0; i < rows.size(); i++) {
      final Row poiRow = summary.createRow(i);
      final List<String> dataRow = rows.get(i);

      for (int j = 0; j < dataRow.size(); j++) {
        final Cell cell = poiRow.createCell(j);
        cell.setCellValue(dataRow.get(j));
        // Embolden header rows
        if (i == 0) {
          cell.setCellStyle(boldStyle);
        } else {
          cell.setCellStyle(regStyle);
        }
      }
    }

    return workbook;
  }

  /**
   * Generate the byte-array representation of this POI workbook.
   *
   * @return the Excel representation of this report
   * @exception IOException if the report cannot be generated.
   */
  public byte[] generate(final List<List<String>> rows) throws IOException {
    final Workbook workbook = this.generateWorkbook(rows);
    workbook.write(this.baos);
    workbook.close();

    return this.baos.toByteArray();
  }
}
