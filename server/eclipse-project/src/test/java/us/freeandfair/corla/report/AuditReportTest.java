package us.freeandfair.corla.report;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import us.freeandfair.corla.report.AuditReport;

import org.testng.annotations.Test;
import static org.testng.Assert.*;


@Test
public class AuditReportTest {

  private AuditReportTest() {};


  @Test
  public void generateTest() throws IOException {
    List<String> row1 = new ArrayList() {{ add("wat"); }};
    List<List<String>> rows = new ArrayList(){{ add(row1); add(row1); }};

    AuditReport ar = new AuditReport(rows);
    assertEquals(ar.generateExcelWorkbook()
                 .getSheet("Summary")
                 .getRow(0)
                 .getCell(0)
                 .getStringCellValue()
                 ,
                 "wat");
  }



}
