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
import us.freeandfair.corla.query.CountyQueries;

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
    "re-audit ballot comment",
    "timestamp"
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

  public static Map<Long,String> countyNames = new HashMap();

  public static String findCountyName(Long countyId) {
    String name = countyNames.get(countyId);
    if (null != name) {
      return name;
    } else {
      name = CountyQueries.getName(countyId);
      countyNames.put(countyId, name);
      return name;
    }
  }

  public static String toString(Object o) {
    if (null != o) {
      return o.toString();
    } else {
      return null;
    }
  }

  public static String renderAuditBoard(Integer auditBoardIndex) {
    Integer i = Integer.valueOf(auditBoardIndex);
    i++; // present 1-based rather than 0-based
    return i.toString();
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

    if (null != discrepancy && 0 != discrepancy) {
      row.put("discrepancy", discrepancy.toString());
    } else {
      row.put("discrepancy", null);
    }
    row.put("dbID", acvr.getCvrId().toString());
    row.put("recordType", acvr.recordType().toString());
    row.put("county", findCountyName(acvr.countyID()));
    row.put("imprintedID", acvr.imprintedID());
    row.put("auditBoard", renderAuditBoard(acvr.getAuditBoardIndex()));
    row.put("revision", toString(acvr.getRevision()));
    row.put("re-audit ballot comment", acvr.getComment());
    row.put("timestamp", acvr.timestamp().toString());
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
