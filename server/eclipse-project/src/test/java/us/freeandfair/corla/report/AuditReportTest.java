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
    List<String> headers = new ArrayList() {{
      // log of audit calculation events for a contest
      add("dbID");
      add("recordType");
      add("county");
      add("imprintedID");
      add("auditBoard");
      add("discrepancy");
      add("consensus");
      add("comment");
      add("revision"); // if it had been re-audited
      add("re-audit ballot comment");
    }};
    List<String> row1 = new ArrayList() {{ add("123"); add("AUDITOR_ENTERED");}};
    List<String> row2 = new ArrayList() {{ add("456"); add("PHANTOM_BALLOT");}};

    List<List<String>> rows = new ArrayList();
    rows.add(headers);
    rows.add(row1);
    rows.add(row2);

    AuditReport ar = new AuditReport(rows);
    assertEquals(ar.generateExcelWorkbook()
                 .getSheet("Summary")
                 .getRow(0)
                 .getCell(0)
                 .getStringCellValue()
                 ,
                 "dbID");
  }



}
