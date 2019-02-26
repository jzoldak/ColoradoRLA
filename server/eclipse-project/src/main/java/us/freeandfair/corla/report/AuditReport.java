package us.freeandfair.corla.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

public class AuditReport {
  private List<List<String>> rows;

  public AuditReport(List<List<String>> rows) {
    this.rows = rows;
  }

  private Row addRow(final Sheet sheet) {
    int nextRowNum;
    if (sheet.getLastRowNum() == 0 && sheet.getPhysicalNumberOfRows() == 0) {
      nextRowNum = 0; // first row
    } else {
      nextRowNum = sheet.getLastRowNum() + 1;
    }

    return sheet.createRow(nextRowNum);
  }

  private Row addValue(final Row row, final String value) {
    // getLastCellNum starts at -1, just to be fun
    int nextCellNum = row.getLastCellNum();
    if (row.getLastCellNum() == -1) {
      nextCellNum = 0; // first row
    } else {
      nextCellNum = row.getLastCellNum();
    }

    Cell cell = row.createCell(nextCellNum);
    cell.setCellValue(value);

    return row;
  }

  private Row addValues(final Row row, final List<String> values) {
    values.stream()
      .forEach(value -> this.addValue(row, value));

    return row;
  }

  public Sheet addRows(Sheet sheet, final List<List<String>> rows) {
    rows.stream()
      .forEach(row -> this.addValues(this.addRow(sheet), row));

    return sheet;
  }

  public Workbook generateExcelWorkbook() {
    final Workbook workbook = new XSSFWorkbook();
    final Sheet summary = workbook.createSheet("Summary");
    this.addRows(summary, this.rows);

    return workbook;
  }

  /**
   * @return the Excel representation of this report, as a byte array.
   * @exception IOException if the report cannot be generated.
   */
  public byte[] generateExcel() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final Workbook workbook = generateExcelWorkbook();
    workbook.write(baos);
    baos.flush();
    baos.close();
    workbook.close();

    return baos.toByteArray();
  }
}
