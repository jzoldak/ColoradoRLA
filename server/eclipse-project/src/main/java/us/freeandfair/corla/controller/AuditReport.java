package us.freeandfair.corla.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import us.freeandfair.corla.csv.CSVWriter;
import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.CVRAuditInfo;
import us.freeandfair.corla.model.ComparisonAudit;
import us.freeandfair.corla.query.CastVoteRecordQueries;
import us.freeandfair.corla.query.ComparisonAuditQueries;


public class AuditReport {


  // no instantiation
  private AuditReport () {};

  // class TypeOne {
    public static final List<String> HEADERS = new ArrayList() {{
      add("dbID");
      add("recordType");
      add("county");
      add("imprintedID");
      add("auditBoard");
      add("discrepancy");
      add("consensus");
      add("comment");
      add("revision");
      add("re-audit ballot comment");
    }};

    // should be an acvr
  public static List<String> toCSV(ComparisonAudit audit, CVRAuditInfo cai) {
      CastVoteRecord acvr = cai.acvr();
      // TODO optimize this
      CVRContestInfo info = acvr.contestInfoForContestResult(audit.contestResult());
      return new ArrayList() {{
        add(acvr.id()); // add("dbID");
        add(acvr.recordType()); // add("recordType");
        add(acvr.countyID());// add("county"); // TODO get county name
        add(acvr.imprintedID());// add("imprintedID");
        add(acvr.getAuditBoardIndex());// add("auditBoard");
        add(audit.getDiscrepancy(cai));// add("discrepancy");
        add(info.consensus());// add("consensus");
        add(info.comment());// add("comment");
        add(acvr.getRevision());// add("revision");
        add(acvr.getComment());// add("re-audit ballot comment");
      }};
    }

  // }

  public static List<List<String>> getRowsFor(String contestName) {
    ComparisonAudit audit = ComparisonAuditQueries.matching(contestName).get(0);
    List<CVRAuditInfo> cais = CastVoteRecordQueries.report(audit.getContestCVRIds());
    List<List<String>> rows = new ArrayList();
    rows.add(HEADERS);
    cais.stream()
      .map(cai -> rows.add(toCSV(audit, cai)))
      .collect(Collectors.toList());
    return rows;
  }




}
