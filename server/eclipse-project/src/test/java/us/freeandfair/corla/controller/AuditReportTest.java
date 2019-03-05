package us.freeandfair.corla.controller;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.IOException;

import us.freeandfair.corla.controller.AuditReport;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

@Test
public class AuditReportTest {

  private AuditReportTest() {};

  @Test
  public void renderRow() throws IOException {
    String[] headers = { "a", "b", "c"};
    AuditReport.Row row = new AuditReport.Row(headers);
    row.put("a", "1");
    assertEquals("1", row.get("a"));
    List result = Stream.of("1", null, null)
      .collect(Collectors.toList());
    assertEquals(row.toArray(), result);
  }
}
