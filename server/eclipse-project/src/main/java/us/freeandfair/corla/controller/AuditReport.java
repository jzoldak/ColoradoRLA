package us.freeandfair.corla.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import us.freeandfair.corla.csv.CSVWriter;
import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.CVRAuditInfo;
import us.freeandfair.corla.model.CVRContestInfo;
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
    if (null != acvr && null != acvr.contestInfo()) {
      // TODO optimize this
      Optional<CVRContestInfo> infoMaybe = acvr.contestInfoForContestResult(audit.contestResult());
      if (infoMaybe.isPresent()) {
        CVRContestInfo info = infoMaybe.get();
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
      } else {
        return new ArrayList() {{ add(cai.cvr().id());add("Not yet audited."); }};
      }
    } else {
      return new ArrayList() {{ add(cai.cvr().id());add("Not yet audited."); }};
    }
  }

  // }

  public static List<List<String>> getRowsFor(String contestName) {
    ComparisonAudit audit;
    List<List<String>> rows = new ArrayList();

    List<ComparisonAudit> audits = ComparisonAuditQueries.matching(contestName);
    if (0 == audits.size()) {
      rows.add(new ArrayList() {{ add("audit has not started");}});
      return rows;
    } else {
      audit = audits.get(0); // fixme: there is only ever one
    }

    List<Long> contestCVRIds = audit.getContestCVRIds();
    List<CVRAuditInfo> cais = CastVoteRecordQueries.report(contestCVRIds);

    rows.add(HEADERS);
    cais.forEach(cai -> rows.add(toCSV(audit, cai)));

    return rows;
  }




}
