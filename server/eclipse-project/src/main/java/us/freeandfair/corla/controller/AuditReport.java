package us.freeandfair.corla.controller;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import us.freeandfair.corla.csv.CSVWriter;
import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.County;
import us.freeandfair.corla.model.CVRAuditInfo;
import us.freeandfair.corla.model.CVRContestInfo;
import us.freeandfair.corla.model.ComparisonAudit;
import us.freeandfair.corla.model.Tribute;
import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.query.CastVoteRecordQueries;
import us.freeandfair.corla.query.ComparisonAuditQueries;
import us.freeandfair.corla.query.CountyQueries;
import us.freeandfair.corla.query.TributeQueries;

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
    "round",
    "imprintedID",
    "auditBoard",
    "discrepancy",
    "consensus",
    "comment",
    "revision",
    "rand",
    "randSequencePosition",
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
    if (null == auditBoardIndex) {
      return null;
    } else {
      Integer i = Integer.valueOf(auditBoardIndex);
      i++; // present 1-based rather than 0-based
      return i.toString();
    }
  }

  /**
   * Prepend a plus sign on positive integers to make it clear that it is positive.
   * Negative numbers will have the negative sign
   **/
  public static String renderDiscrepancy(Integer discrepancy) {
    if (discrepancy > 0) {
      return String.format("+%d", discrepancy);
    } else {
      return discrepancy.toString();
    }
  }

  /** US local date time **/
  private static final DateTimeFormatter MMDDYYYY =
    DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

  public static String renderTimestamp(Instant timestamp) {
    return MMDDYYYY.format(LocalDateTime
                           .ofInstant(timestamp,
                                      TimeZone.getDefault().toZoneId()));
  }

  public static String maybeToString(Object o) {
    if (null != o) {
      return o.toString();
    } else {
      return null;
    }
  }

  // should be an acvr
  public static Row toRow(ComparisonAudit audit, CastVoteRecord acvr) {
    Row row = new Row(HEADERS);

    Integer discrepancy = findDiscrepancy(audit, acvr);
    Optional<CVRContestInfo> infoMaybe = acvr.contestInfoForContestResult(audit.contestResult());

    if (infoMaybe.isPresent()) {
      CVRContestInfo info = infoMaybe.get();
      row.put("consensus", maybeToString(info.consensus()));
      row.put("comment", info.comment());
    }

    if (null != discrepancy && 0 != discrepancy) {
      row.put("discrepancy", renderDiscrepancy(discrepancy));
    } else {
      row.put("discrepancy", null);
    }
    row.put("dbID", acvr.getCvrId().toString());
    row.put("recordType", acvr.recordType().toString());
    row.put("county", findCountyName(acvr.countyID()));
    row.put("imprintedID", acvr.imprintedID());
    row.put("auditBoard", renderAuditBoard(acvr.getAuditBoardIndex()));
    row.put("round", maybeToString(acvr.getRoundNumber()));
    row.put("revision", toString(acvr.getRevision()));
    row.put("re-audit ballot comment", acvr.getComment());
    row.put("timestamp", renderTimestamp(acvr.timestamp()));
    return row;
  }

  public static Row toRow(ComparisonAudit audit, CastVoteRecord acvr, Tribute tribute) {
    Row row = toRow(audit, acvr);
    row.put("rand", tribute.rand.toString());
    row.put("randSequencePosition", tribute.randSequencePosition.toString());
    return row;
  }

  public static List<List<String>> getContestActivity(String contestName) {
    List<List<String>> rows = new ArrayList();

    ComparisonAudit audit = ComparisonAuditQueries.matching(contestName);
    if (null == audit) {
      rows.add(new ArrayList() {{ add("audit has not started or contest name not found");}});
      return rows;
    }

    List<Long> contestCVRIds = audit.getContestCVRIds();
    List<CastVoteRecord> acvrs = CastVoteRecordQueries.activityReport(contestCVRIds);
    acvrs.sort(Comparator.comparing(CastVoteRecord::timestamp));

    rows.add(Arrays.asList(HEADERS));
    acvrs.forEach(acvr -> rows.add(toRow(audit, acvr).toArray()));

    return rows;
  }

  public static List<List<String>> getResultsReport(String contestName) {
    List<List<String>> rows = new ArrayList();
    rows.add(Arrays.asList(HEADERS));

    List<Tribute> tributes = TributeQueries.forContest(contestName);
    tributes.sort(Comparator.comparing(t -> t.randSequencePosition));

    ComparisonAudit audit = ComparisonAuditQueries.matching(contestName);
    if (null == audit) {
      rows.add(new ArrayList() {{ add("audit has not started or contest name not found");}});
      return rows;
    }

    List<Long> contestCVRIds = audit.getContestCVRIds();
    List<CastVoteRecord> acvrs = CastVoteRecordQueries.resultsReport(contestCVRIds);

    for (final Tribute tribute: tributes) {
      final String uri = tribute.getUri();
      final String aUri = uri.replaceFirst("^cvr", "acvr");
      final Optional<CastVoteRecord> acvr = acvrs.stream()
        .filter(c -> c.getUri().equals(aUri))
        .findFirst();
      if (acvr.isPresent()) {
        rows.add(toRow(audit, acvr.get(), tribute).toArray());
      } else {
        rows.add(new ArrayList() {{ add("error could not find matching acvr for random number " + tribute.rand.toString() );}});
      }
    }

    return rows;
  }
}
