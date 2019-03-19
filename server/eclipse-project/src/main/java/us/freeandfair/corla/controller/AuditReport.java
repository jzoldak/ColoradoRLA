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

import org.apache.commons.lang3.ArrayUtils;

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

  public static final String[] ALL_HEADERS = {
    "db id",
    "record type",
    "county",
    "round",
    "imprinted id",
    "scanner id",
    "batch id",
    "record id",
    "audit board",
    "discrepancy",
    "consensus",
    "comment",
    "random number",
    "random number sequence position",
    "multiplicity",
    "revision",
    "re-audit ballot comment",
    "time of submission"
  };


  public static final String[] ACTIVITY_HEADERS =
    ArrayUtils.removeElements(ArrayUtils.clone(ALL_HEADERS), "randSequencePosition", "rand");

  public static Integer findDiscrepancy(ComparisonAudit audit, CastVoteRecord acvr) {
    if (null != acvr.getRevision()) {
      // this is a reaudited acvr, so we need to recompute the discrepancy
      CastVoteRecord cvr = Persistence.getByID(acvr.getCvrId(), CastVoteRecord.class);
      OptionalInt disc = audit.computeDiscrepancy(cvr, acvr);
      if (disc.isPresent()) {
        return disc.getAsInt();
      } else {
        return null;
      }
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

  public static Row addBaseFields(Row row, ComparisonAudit audit, CastVoteRecord acvr) {
    Integer discrepancy = findDiscrepancy(audit, acvr);
    Optional<CVRContestInfo> infoMaybe = acvr.contestInfoForContestResult(audit.contestResult());

    if (infoMaybe.isPresent()) {
      CVRContestInfo info = infoMaybe.get();
      row.put("consensus", toString(info.consensus()));
      row.put("comment", info.comment());
    }

    if (null != discrepancy && 0 != discrepancy) {
      row.put("discrepancy", renderDiscrepancy(discrepancy));
    } else {
      row.put("discrepancy", null);
    }
    row.put("db id", acvr.getCvrId().toString());
    row.put("record type", acvr.recordType().toString());
    row.put("county", findCountyName(acvr.countyID()));
    row.put("audit board", renderAuditBoard(acvr.getAuditBoardIndex()));
    row.put("round", toString(acvr.getRoundNumber()));
    row.put("imprinted id", acvr.imprintedID());
    row.put("scanner id", toString(acvr.scannerID()));
    row.put("batch id", acvr.batchID());
    row.put("record id", toString(acvr.recordID()));
    row.put("time of submission", renderTimestamp(acvr.timestamp()));
    return row;
  }

  public static Row addActivityFields(Row row, CastVoteRecord acvr) {
    row.put("revision", toString(acvr.getRevision()));
    row.put("re-audit ballot comment", acvr.getComment());
    return row;
  }

  public static Row addResultsFields(Row row, Tribute tribute, Integer multiplicity) {
    row.put("multiplicity", multiplicity);
    return addResultsFields(row, tribute);
  }
  public static Row addResultsFields(Row row, Tribute tribute) {
    row.put("random number", toString(tribute.rand));
    row.put("random number sequence position", toString(tribute.randSequencePosition));
    return row;
  }


  public static class ActivityReport {
    public static final String[] HEADERS =
      ArrayUtils.removeElements(ArrayUtils.clone(ALL_HEADERS), "random number sequence position", "random number", "multiplicity");

    public static final Row newRow() {
      return new Row(HEADERS);
    }
  }

  public static class ResultsReport {
    public static final String[] HEADERS =
      ArrayUtils.removeElements(ArrayUtils.clone(ALL_HEADERS), "revision", "re-audit ballot comment");

    public static final Row newRow() {
      return new Row(HEADERS);
    }
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

    rows.add(Arrays.asList(ActivityReport.HEADERS));
    acvrs.forEach(acvr -> {
        Row row = ActivityReport.newRow();
        rows.add(addActivityFields(addBaseFields(row, audit, acvr), acvr).toArray());
      });

    return rows;
  }

  public static List<List<String>> getResultsReport(String contestName) {
    List<List<String>> rows = new ArrayList();

    List<Tribute> tributes = TributeQueries.forContest(contestName);
    tributes.sort(Comparator.comparing(t -> t.randSequencePosition));

    ComparisonAudit audit = ComparisonAuditQueries.matching(contestName);
    if (null == audit) {
      rows.add(new ArrayList() {{ add("audit has not started or contest name not found");}});
      return rows;
    }

    List<Long> contestCVRIds = audit.getContestCVRIds();
    List<CastVoteRecord> acvrs = CastVoteRecordQueries.resultsReport(contestCVRIds);

    rows.add(Arrays.asList(ResultsReport.HEADERS));
    for (final Tribute tribute: tributes) {
      final String uri = tribute.getUri();
      final String aUri = uri.replaceFirst("^cvr", "acvr");
      final Optional<CastVoteRecord> acvr = acvrs.stream()
        .filter(c -> c.getUri().equals(aUri))
        .findFirst();

      Row row = ResultsReport.newRow();
      if (acvr.isPresent()) {
        Integer multiplicity = audit.multiplicity(acvr.getCvrId());
        rows.add(addResultsFields(addBaseFields(row, audit, acvr.get()), tribute, multiplicity).toArray());
      } else {
        // not yet audited
        rows.add(addResultsFields(row, tribute).toArray());
      }
    }

    return rows;
  }
}
