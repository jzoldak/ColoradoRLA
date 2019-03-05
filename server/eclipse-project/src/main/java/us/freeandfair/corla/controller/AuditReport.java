package us.freeandfair.corla.controller;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import us.freeandfair.corla.csv.CSVWriter;
import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.County;
import us.freeandfair.corla.model.CVRAuditInfo;
import us.freeandfair.corla.model.CVRContestInfo;
import us.freeandfair.corla.model.ComparisonAudit;
import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.query.CastVoteRecordQueries;
import us.freeandfair.corla.query.ComparisonAuditQueries;

/**
 * Find the data for a report and format it to be rendered into a presentation
 * format elsewhere
 **/
public class AuditReport {


  // no instantiation
  private AuditReport () {};


  /**
   * One array to be part of an array of arrays, ie: a table or csv or xlsx.
   * It keeps the headers and fields in order.
   **/
  public static class Row {

    // composition rather than inheritance
    private final Map<String, String> map = new HashMap<String, String>();

    private final String[] headers;

    public Row(String[] headers) {
      this.headers = headers;
    }

    public String get(String key) {
      return this.map.get(key);
    }

    public void put(String key, String value) {
      this.map.put(key, value);
    }

    public List<String> toArray() {
      List<String> a = new ArrayList<String>();
      for (String h: this.headers) {
        a.add(this.get(h));
      }
      return a;
    }
  }

  public static final String[] HEADERS = {
    "dbID",
    "recordType",
    "county",
    "imprintedID",
    "auditBoard",
    "discrepancy",
    "consensus",
    "comment",
    "revision",
    "re-audit ballot comment"
  };

  public static Integer findDiscrepancy(ComparisonAudit audit, CastVoteRecord acvr) {
    if (null != acvr.getRevision()) {
      // this is a reaudited acvr, so we need to recompute the discrepancy
      CastVoteRecord cvr = Persistence.getByID(acvr.getCvrId(), CastVoteRecord.class);
      return audit.computeDiscrepancy(cvr, acvr).getAsInt();
    } else {
      CVRAuditInfo cai = Persistence.getByID(acvr.getCvrId(), CVRAuditInfo.class);
      return audit.getDiscrepancy(cai);
    }
  }

  public static String findCountyName(Long countyId) {
    County c = Persistence.getByID(countyId, County.class);
    if (null != c) {
      return c.name();
    } else {
      return null;
    }
  }

  // should be an acvr
  public static List<String> toCSV(ComparisonAudit audit, CastVoteRecord acvr) {
    Row row = new Row(HEADERS);

    Integer discrepancy = findDiscrepancy(audit, acvr);
    Optional<CVRContestInfo> infoMaybe = acvr.contestInfoForContestResult(audit.contestResult());

    if (infoMaybe.isPresent()) {
      CVRContestInfo info = infoMaybe.get();
      row.put("consensus", info.consensus().toString());
      row.put("comment", info.comment());
    }

    if (0 != discrepancy) {
      row.put("discrepancy", discrepancy.toString());
    } else {
      row.put("discrepancy", null);
    }
    row.put("dbID", acvr.getCvrId().toString());
    row.put("recordType", acvr.recordType().toString());
    row.put("countyName", findCountyName(acvr.countyID()));
    row.put("imprintedID", acvr.imprintedID());
    row.put("auditBoard", acvr.getAuditBoardIndex().toString());
    row.put("revision", acvr.getRevision().toString());
    row.put("re-audit ballot comment", acvr.getComment());
    return row.toArray();
  }

  public static List<List<String>> getRowsFor(String contestName) {
    List<List<String>> rows = new ArrayList();

    ComparisonAudit audit = ComparisonAuditQueries.matching(contestName);
    if (null == audit) {
      rows.add(new ArrayList() {{ add("audit has not started or contest name not found");}});
      return rows;
    }

    List<Long> contestCVRIds = audit.getContestCVRIds();
    List<CastVoteRecord> acvrs = CastVoteRecordQueries.report(contestCVRIds);

    rows.add(Arrays.asList(HEADERS));
    acvrs.forEach(acvr -> rows.add(toCSV(audit, acvr)));

    return rows;
  }
}
