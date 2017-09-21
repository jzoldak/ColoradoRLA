/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Jul 27, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @model_review Joe Kiniry <kiniry@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.model;

import static us.freeandfair.corla.util.EqualsHashcodeHelper.nullableEquals;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import us.freeandfair.corla.model.CVRContestInfo.ConsensusValue;
import us.freeandfair.corla.persistence.AuditReasonIntegerMapConverter;
import us.freeandfair.corla.persistence.PersistentEntity;

/**
 * The county dashboard.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
@Entity
@Cacheable(true)
@Table(name = "county_dashboard")
@SuppressWarnings({"PMD.ImmutableField", "PMD.TooManyMethods", "PMD.TooManyFields",
    "PMD.GodClass", "PMD.ExcessiveImports", "checkstyle:methodcount",
    "PMD.ExcessivePublicCount", "PMD.CyclomaticComplexity"})
public class CountyDashboard implements PersistentEntity, Serializable {
  /**
   * The minimum number of members on an audit board.
   */
  public static final int MIN_AUDIT_BOARD_MEMBERS = 2;
  
  /**
   * The minimum number of members on an audit round sign-off.
   */
  public static final int MIN_ROUND_SIGN_OFF_MEMBERS = 2;
  
  /**
   * The "no content" constant.
   */
  private static final Integer NO_CONTENT = null;
  
  /**
   * The "my_dashboard" string.
   */
  private static final String MY_DASHBOARD = "my_dashboard";
  
  /**
   * The "index" string.
   */
  private static final String INDEX = "index";

  /**
   * The "my_id" string.
   */
  private static final String MY_ID = "my_id";
  
  /**
   * The "dashboard_id" string.
   */
  private static final String DASHBOARD_ID = "dashboard_id";
  
  /**
   * The serialVersionUID.
   */
  private static final long serialVersionUID = 1; 
  
  /**
   * The database ID; this is always the county ID.
   */
  @Id
  private Long my_id;
  
  /**
   * The county.
   */
  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn
  private County my_county;
  
  /**
   * The version (for optimistic locking).
   */
  @Version
  private Long my_version;

  /**
   * The timestamp of the most recent set of uploaded CVRs.
   */
  private Instant my_cvr_upload_timestamp;
  
  /**
   * The number of CVRs imported.
   */
  @Column(nullable = false) 
  private Integer my_cvrs_imported = 0;
  
  /**
   * The timestamp of the most recent uploaded ballot manifest. 
   */
  private Instant my_manifest_upload_timestamp;
  
  /**
   * The number of ballots described in the ballot manifest.
   */
  @Column(nullable = false)
  private Integer my_ballots_in_manifest = 0;
  
  /**
   * The timestamp for the start of the audit.
   */
  private Instant my_audit_timestamp; 
  
  /**
   * The members of the audit board.
   */
  @ElementCollection(fetch = FetchType.LAZY)
  @OrderColumn(name = INDEX)
  @CollectionTable(name = "audit_board",
                   joinColumns = @JoinColumn(name = DASHBOARD_ID, 
                                             referencedColumnName = MY_ID))
  private List<AuditBoard> my_audit_boards = new ArrayList<>();
 
  /**
   * The current audit board.
   */
  private Integer my_current_audit_board_index;
  
  /**
   * The audit rounds.
   */
  @ElementCollection(fetch = FetchType.LAZY)
  @OrderColumn(name = INDEX)
  @CollectionTable(name = "round", 
                   joinColumns = @JoinColumn(name = DASHBOARD_ID,
                                             referencedColumnName = MY_ID))
  private List<Round> my_rounds = new ArrayList<>();
  
  /**
   * The current audit round.
   */
  private Integer my_current_round_index;
  
  /**
   * The sequence of CVRs to audit, stored as CVRAuditInfo records.
   */
  @OneToMany(cascade = CascadeType.ALL, mappedBy = MY_DASHBOARD, 
             fetch = FetchType.LAZY, orphanRemoval = true)
  @OrderBy("my_index")
  private List<CVRAuditInfo> my_cvr_audit_info = new ArrayList<>();
  
  /**
   * The set of contests driving the audit.
   */
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "driving_contest",
             joinColumns = @JoinColumn(name = DASHBOARD_ID,
                                       referencedColumnName = MY_ID),
             inverseJoinColumns = @JoinColumn(name = "contest_id", 
                                              referencedColumnName = MY_ID))
  private Set<Contest> my_driving_contests = new HashSet<>();
  
  /**
   * The audit data.
   */
  @OneToMany(cascade = CascadeType.ALL, mappedBy = MY_DASHBOARD, 
             fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<CountyContestComparisonAudit> my_comparison_audits = 
      new HashSet<>(); 
  
  /**
   * The audit investigation reports.
   */
  @ElementCollection(fetch = FetchType.LAZY)
  @OrderColumn(name = INDEX)
  @CollectionTable(name = "audit_investigation_report",
                   joinColumns = @JoinColumn(name = DASHBOARD_ID, 
                                             referencedColumnName = MY_ID))
  private List<AuditInvestigationReportInfo> my_investigation_reports = 
      new ArrayList<>();
  
  /**
   * The audit interim reports.
   */
  @ElementCollection(fetch = FetchType.LAZY)
  @OrderColumn(name = INDEX)
  @CollectionTable(name = "audit_intermediate_report",
                   joinColumns = @JoinColumn(name = DASHBOARD_ID, 
                                             referencedColumnName = MY_ID))
  private List<IntermediateAuditReportInfo> my_intermediate_reports = 
      new ArrayList<>();
  
  /**
   * The number of ballots audited.
   */
  @Column(nullable = false)
  private Integer my_ballots_audited = 0;
  
  /**
   * The estimated ballots to audit.
   */
  private Integer my_estimated_ballots_to_audit = 0;

  /**
   * The optimistic ballots to audit.
   */
  private Integer my_optimistic_ballots_to_audit = 0;
  
  /**
   * The length of the audited prefix of the list of ballots to audit;
   * equivalent to the index of the CVR currently under audit.
   */
  private Integer my_audited_prefix_length;
  
  /**
   * The number of samples that have been audited so far.
   */
  private Integer my_audited_sample_count;
  
  /**
   * The number of discrepancies found in the audit so far.
   */
  @Column(nullable = false, name = "discrepancies", columnDefinition = "text")
  @Convert(converter = AuditReasonIntegerMapConverter.class)
  private Map<AuditReason, Integer> my_discrepancies = new HashMap<>();
  
  /**
   * The number of disagreements found in the audit so far.
   */
  @Column(nullable = false, name = "disagreements", columnDefinition = "text")
  @Convert(converter = AuditReasonIntegerMapConverter.class)
  private Map<AuditReason, Integer> my_disagreements = new HashMap<>();
  
  /**
   * Constructs an empty county dashboard, solely for persistence.
   */
  public CountyDashboard() {
    super();
  }
  
  /**
   * Constructs a new county dashboard for the specified county.
   * 
   * @param the_county The county.
   */
  public CountyDashboard(final County the_county) {
    super();
    my_county = the_county;
    my_id = the_county.id();
  }
  
  /**
   * @return the database ID for this dashboard, which is the same as
   * its county ID.
   */
  @Override
  public Long id() {
    return my_id;
  }
  
  /**
   * Sets the database ID for this dashboard. This operation is unsupported on
   * this class.
   * 
   * @param the_id The ID. 
   * @exception UnsupportedOperationException always.
   */
  @Override
  public final void setID(final Long the_id) {
    throw new UnsupportedOperationException("setID() not supported on county dashboard");
  }
  
  /**
   * @return the version for this dashboard.
   */
  @Override
  public Long version() {
    return my_version;
  }
  
  /**
   * @return the county for this dashboard.
   */
  public County county() {
    return my_county;
  }
  
  /**
   * @return the CVR upload timestamp. A return value of null means
   * that no CVRs have been uploaded for this county.
   */
  public Instant cvrUploadTimestamp() {
    return my_cvr_upload_timestamp;
  }
  
  /**
   * Sets a new CVR upload timestamp, replacing the previous one.
   * 
   * @param the_timestamp The new upload timestamp.
   */
  public void setCVRUploadTimestamp(final Instant the_timestamp) {
    my_cvr_upload_timestamp = the_timestamp;
  }  
  
  /**
   * @return the ballot manifest upload timestamp. A return value of null means
   * that no ballot manifest has been uploaded for this county.
   */
  public Instant manifestUploadTimestamp() {
    return my_manifest_upload_timestamp;
  }
  
  /**
   * Sets a new CVR upload timestamp, replacing the previous one.
   * 
   * @param the_timestamp The new upload timestamp.
   */
  public void setManifestUploadTimestamp(final Instant the_timestamp) {
    my_manifest_upload_timestamp = the_timestamp;
  }  
  
  /**
   * @return the audit timestamp. A return value of null means
   * that no audit has been started.
   */
  public Instant auditTimestamp() {
    return my_audit_timestamp;
  }
  
  /**
   * Sets a new audit timestamp, replacing the previous one.
   * 
   * @param the_timestamp The new audit timestamp.
   */
  public void setAuditTimestamp(final Instant the_timestamp) {
    my_audit_timestamp = the_timestamp;
  }  

  /**
   * @return the current audit board.
   */
  public AuditBoard currentAuditBoard() {
    if (my_current_audit_board_index == null) {
      return null; 
    } else {
      return my_audit_boards.get(my_current_audit_board_index);
    }
  }
  
  /**
   * @return the entire list of audit boards.
   */
  public List<AuditBoard> auditBoards() {
    return Collections.unmodifiableList(my_audit_boards);
  }
  
  /**
   * Signs in the specified audit board as of the present time; 
   * the supplied set of electors must be the full set of electors on
   * the board. The previous audit board, if any, is signed out if it
   * had not yet been signed out.
   * 
   * @param the_members The members.
   */
  public void signInAuditBoard(final List<Elector> the_members) {
    if (my_current_audit_board_index == null) {
      my_current_audit_board_index = my_audit_boards.size();
    } else {
      final AuditBoard current = my_audit_boards.get(my_current_audit_board_index);
      current.setSignOutTime(Instant.now());
      my_current_audit_board_index = my_current_audit_board_index + 1;
    }
    my_audit_boards.add(new AuditBoard(the_members, Instant.now()));
  }
  
  /**
   * Signs out the current audit board.
   * 
   * @exception IllegalStateException if no audit board is signed in.
   */
  public void signOutAuditBoard() {
    if (my_current_audit_board_index == null) {
      throw new IllegalArgumentException("no audit board signed in");
    } else {
      final AuditBoard current = my_audit_boards.get(my_current_audit_board_index);
      current.setSignOutTime(Instant.now());
      my_current_audit_board_index = NO_CONTENT;
    }
  }
  
  /**
   * Define the CVRs to audit. This clears any existing list of CVRs to 
   * audit, as well as the ACVRs associated with them.
   * 
   * @param the_cvrs_to_audit A list of the CVRs to audit, in the order 
   * they should be examined. It may contain duplicates, which are dealt with 
   * appropriately when submitting audit CVRs.
   * @exception IllegalArgumentException if any null CVRs are in the list.
   */
  public void setCVRsToAudit(final List<CastVoteRecord> the_cvrs_to_audit) 
      throws IllegalArgumentException {
    if (the_cvrs_to_audit.contains(null)) {
      throw new IllegalArgumentException("null elements in audit cvr list");
    }
    my_cvr_audit_info.clear();
    addCVRsToAudit(the_cvrs_to_audit);
  }

  /**
   * Adds more CVRs to audit, appending them to the existing list. This
   * does not modify any already submitted ACVRs or county audit counters.
   * 
   * @param the_cvrs_to_add A list of the CVRs to append, in the order they
   * should be added.
   * @exception IllegalArgumentException if any null CVRs are in the list.
   */
  public void addCVRsToAudit(final List<CastVoteRecord> the_cvrs_to_add) {
    if (the_cvrs_to_add.contains(null)) {
      throw new IllegalArgumentException("null elements in audit cvr list");
    }
    for (final CastVoteRecord cvr : the_cvrs_to_add) {
      // create a persistent record
      final CVRAuditInfo cvrai = new CVRAuditInfo(this, cvr, my_cvr_audit_info.size());
      my_cvr_audit_info.add(cvrai);
    }
  }
  
  /**
   * @return all the audit rounds.
   */
  public List<Round> rounds() {
    return Collections.unmodifiableList(my_rounds);
  }
  
  /**
   * @return the current audit round, or null if no round is in progress.
   */
  public Round currentRound() {
    if (my_current_round_index == null) {
      return null; 
    } else {
      return my_rounds.get(my_current_round_index);
    }
  }

  /**
   * Begins a new round with the specified number of ballots to audit
   * and expected achieved prefix length, starting at the specified index 
   * in the random audit sequence. 
   * 
   * @param the_number_of_ballots The number of ballots.
   * @param the_start_index The start index.
   * @exception IllegalStateException if a round is currently ongoing.
   */
  public void startRound(final int the_number_of_ballots, 
                         final int the_prefix_length,
                         final int the_start_index) {
    if (my_current_round_index == null) {
      my_current_round_index = my_rounds.size();
    } else {
      throw new IllegalStateException("cannot start a round while one is running");
    }
    // note UI round indexing is from 1, not 0
    final Round round = new Round(my_current_round_index + 1, 
                                  Instant.now(), 
                                  the_number_of_ballots,
                                  my_ballots_audited,
                                  the_prefix_length,
                                  the_start_index);
    updateRound(round);
    my_rounds.add(round);
  }
  
  /**
   * Updates a round object with the disagreements and discrepancies in the
   * audited prefix range between its start index and the audited prefix length.
   * 
   * @param the_round The round to update.
   */
  private void updateRound(final Round the_round) {
    int index = the_round.startAuditedPrefixLength();
    final Map<Contest, AuditReason> audit_reasons = new HashMap<>();
    final Set<AuditReason> discrepancies = new HashSet<>();
    final Set<AuditReason> disagreements = new HashSet<>();
    while (index < my_audited_prefix_length && 
           the_round.actualCount() < the_round.expectedCount()) {
      final CVRAuditInfo cvrai = my_cvr_audit_info.get(index);
      for (final CountyContestComparisonAudit ca : comparisonAudits()) {
        audit_reasons.put(ca.contest(), ca.auditReason());
        if (!discrepancies.contains(ca.auditReason()) && 
            ca.computeDiscrepancy(cvrai.cvr(), cvrai.acvr()).isPresent()) {
          discrepancies.add(ca.auditReason());
        }
      }
      for (final CVRContestInfo ci : cvrai.acvr().contestInfo()) {
        final AuditReason reason = audit_reasons.get(ci.contest());
        if (ci.consensus() == ConsensusValue.NO) {
          disagreements.add(reason);
        }
      }
      the_round.addAuditedBallot();
      for (final AuditReason r : discrepancies) {
        the_round.addDiscrepancy(r);
      }
      for (final AuditReason r : disagreements) {
        the_round.addDisagreement(r);
      }
      index = index + 1;
    }
  }
  
  /**
   * Ends the current round.
   * 
   * @param the_signatories The signatories for round sign-off.
   * @exception IllegalStateException if there is no current round.
   */
  public void endRound(final List<Elector> the_signatories) {
    if (my_current_round_index == null) {
      throw new IllegalStateException("no round to end");
    } else {
      final Round round = my_rounds.get(my_current_round_index);
      round.setSignatories(the_signatories);
      round.setEndTime(Instant.now());
      round.setActualCount(my_ballots_audited - round.previousBallotsAudited());
      round.setActualAuditedPrefixLength(my_audited_prefix_length);
      my_current_round_index = NO_CONTENT;
    }
  }
  
  /**
   * @return the number of ballots remaining in the current round, or 0
   * if there is no current round.
   */
  public int ballotsRemainingInCurrentRound() {
    final int result;
    
    if (my_current_round_index == null) {
      result = 0;
    } else {
      final Round round = currentRound();
      result = round.expectedCount() + round.previousBallotsAudited() -
               my_ballots_audited; 
    }
    
    return result;
  }
  
  /**
   * @return the list of CVR audit info (for legacy RLA algorithm).
   */
  public List<CVRAuditInfo> cvrAuditInfo() {
    return Collections.unmodifiableList(my_cvr_audit_info);
  }
  
  /**
   * @return the set of comparison audits being performed.
   */
  public Set<CountyContestComparisonAudit> comparisonAudits() {
    return Collections.unmodifiableSet(my_comparison_audits);
  }
  
  /**
   * Sets the comparison audits being performed. 
   * 
   * @param the_comparison_audits The comparison audits.
   */
  public void 
      setComparisonAudits(final Set<CountyContestComparisonAudit> the_comparison_audits) {
    my_comparison_audits.clear();
    my_comparison_audits.addAll(the_comparison_audits);
  }
  
  /** 
   * @return the set of contests driving the audit.
   */
  public Set<Contest> drivingContests() {
    return Collections.unmodifiableSet(my_driving_contests);
  }
  
  /**
   * Sets the contests driving the audit.
   * 
   * @param the_driving_contests The contests.
   */
  public void setDrivingContests(final Set<Contest> the_driving_contests) {
    my_driving_contests.clear();
    my_driving_contests.addAll(the_driving_contests);
  }
  
  /**
   * Submits an audit investigation report.
   * 
   * @param the_report The audit investigation report.
   */
  public void submitInvestigationReport(final AuditInvestigationReportInfo the_report) {
    my_investigation_reports.add(the_report);
  }
  
  /**
   * @return the list of submitted audit investigation reports.
   */
  public List<AuditInvestigationReportInfo> investigationReports() {
    return Collections.unmodifiableList(my_investigation_reports);
  }
  
  /**
   * Submits an audit investigation report.
   * 
   * @param the_report The audit investigation report.
   */
  public void submitIntermediateReport(final IntermediateAuditReportInfo the_report) {
    my_intermediate_reports.add(the_report);
  }
  
  /**
   * @return the list of submitted audit interim reports.
   */
  public List<IntermediateAuditReportInfo> intermediateReports() {
    return Collections.unmodifiableList(my_intermediate_reports);
  }
  
  /**
   * @return the current CVR under audit. This is the first entry in the list 
   * of CVRs to audit that has no corresponding ACVR. Returns null if there is 
   * no next CVR to audit.
   */
  public Long cvrUnderAudit() {
    if (my_cvr_audit_info == null || my_audited_prefix_length == null ||
        my_cvr_audit_info.size() <= my_audited_prefix_length) {
      return null;
    } else {
      return my_cvr_audit_info.get(my_audited_prefix_length).cvr().id();
    }
  }
  
  /**
   * @return the number of ballots audited.
   */
  public Integer ballotsAudited() {
    return my_ballots_audited;
  }
  
  /**
   * Adds an audited ballot. This adds it both to the total and to
   * the current audit round, if one is ongoing.
   */
  public void addAuditedBallot() {
    my_ballots_audited = my_ballots_audited + 1;
    if (my_current_round_index != null) {
      my_rounds.get(my_current_round_index).addAuditedBallot();
    }
  }
 
  /**
   * Removes an audited ballot. This removes it both from the total and
   * from the current audit round, if one is ongoing.
   */
  public void removeAuditedBallot() {
    my_ballots_audited = my_ballots_audited - 1;
    if (my_current_round_index != null) {
      my_rounds.get(my_current_round_index).removeAuditedBallot();
    }  
  }
  
  /**
   * @return the number of CVRs in the CVR import.
   */
  public Integer cvrsImported() {
    return my_cvrs_imported;
  }
  
  /**
   * Sets the number of CVRs imported.
   * 
   * @param the_cvrs_imported The number.
   */
  public void setCVRsImported(final Integer the_cvrs_imported) {
    my_cvrs_imported = the_cvrs_imported;
  }
  
  /**
   * @return the number of ballots described in the ballot manifest.
   */
  public Integer ballotsInManifest() {
    return my_ballots_in_manifest;
  }
  
  /**
   * Sets the number of ballots described in the ballot manifest.
   * 
   * @param the_ballots_in_manifest The number.
   */
  public void setBallotsInManifest(final Integer the_ballots_in_manifest) {
    my_ballots_in_manifest = the_ballots_in_manifest;
  }
  
  /**
   * Ensures that a counter exists in the specified map for the specified key.
   * 
   * @param the_map The map.
   * @param the_key The key.
   */
  private void ensureCounterExists(final Map<AuditReason, Integer> the_map,
                                   final AuditReason the_key) {
    if (!the_map.containsKey(the_key)) {
      the_map.put(the_key, 0);
    }
  }
  
  /**
   * @return the numbers of discrepancies found in the audit so far, 
   * categorized by contest audit reason.
   */
  public Map<AuditReason, Integer> discrepancies() {
    return Collections.unmodifiableMap(my_discrepancies);
  }
  
  /**
   * Adds a discrepancy for the specified audit reason. This adds it both to the 
   * total and to the current audit round, if one is ongoing.
   * 
   * @param the_reason The reason.
   */
  public void addDiscrepancy(final AuditReason the_reason) {
    ensureCounterExists(my_discrepancies, the_reason);
    my_discrepancies.put(the_reason, my_discrepancies.get(the_reason) + 1); 
    if (my_current_round_index != null) {
      my_rounds.get(my_current_round_index).addDiscrepancy(the_reason);
    } 
  }
  
  /**
   * Removes a discrepancy for the specified audit reason. This removes it 
   * both from the total and from the current audit round, if one is ongoing.
   * 
   * @param the_reason The reason.
   */
  public void removeDiscrepancy(final AuditReason the_reason) {
    ensureCounterExists(my_discrepancies, the_reason);
    my_discrepancies.put(the_reason, my_discrepancies.get(the_reason) - 1);
    if (my_current_round_index != null) {
      my_rounds.get(my_current_round_index).removeDiscrepancy(the_reason);
    }
  }
  
  /**
   * @return the numbers of disagreements found in the audit so far,
   * categorized by contest audit reason.
   */
  public Map<AuditReason, Integer> disagreements() {
    return my_disagreements;
  }
  
  /**
   * Adds a disagreement for the specified audit reason. This adds it both to the 
   * total and to the current audit round, if one is ongoing.
   * 
   * @param the_reason The reason.
   */
  public void addDisagreement(final AuditReason the_reason) {
    ensureCounterExists(my_disagreements, the_reason);
    my_disagreements.put(the_reason, my_disagreements.get(the_reason) + 1);
    if (my_current_round_index != null) {
      my_rounds.get(my_current_round_index).addDisagreement(the_reason);
    } 
  }
  
  /**
   * Removes a disagreement for the specified audit reason. This removes it 
   * both from the total and from the current audit round, if one is ongoing.
   * 
   * @param the_reason The reason.
   */
  public void removeDisagreement(final AuditReason the_reason) {
    ensureCounterExists(my_disagreements, the_reason);
    my_disagreements.put(the_reason, my_disagreements.get(the_reason) - 1);
    if (my_current_round_index != null) {
      my_rounds.get(my_current_round_index).removeDisagreement(the_reason);
    } 
  }
  
  /**
   * @return the estimated number of ballots to audit.
   */
  public Integer estimatedBallotsToAudit() {
    return my_estimated_ballots_to_audit;
  }
  
  /**
   * Sets the estimated number of ballots to audit. 
   * 
   * @param the_estimated_ballots_to_audit The estimated number of ballots to audit.
   */
  public void setEstimatedBallotsToAudit(final int the_estimated_ballots_to_audit) {
    my_estimated_ballots_to_audit = the_estimated_ballots_to_audit;
  }
  
  /**
   * @return the optimistic number of ballots to audit.
   */
  public Integer optimisticBallotsToAudit() {
    return my_optimistic_ballots_to_audit;
  }
  
  /**
   * Sets the optimistic number of ballots to audit. 
   * 
   * @param the_optimistic_ballots_to_audit The optimistic number of ballots 
   * to audit.
   */
  public void setOptimisticBallotsToAudit(final int the_optimistic_ballots_to_audit) {
    my_optimistic_ballots_to_audit = the_optimistic_ballots_to_audit;
  }
  
  /**
   * @return the length of the audited prefix of the sequence of
   * ballots to audit (i.e., the number of audited ballots that 
   * "count").
   */
  public Integer auditedPrefixLength() {
    return my_audited_prefix_length;
  }
  
  /**
   * Sets the length of the audited prefix of the sequence of
   * ballots to audit.
   * 
   * @param the_audited_prefix_length The audited prefix length.
   */
  public void setAuditedPrefixLength(final int the_audited_prefix_length) {
    my_audited_prefix_length = the_audited_prefix_length;
    if (my_current_round_index != null) {
      my_rounds.get(my_current_round_index).
          setActualAuditedPrefixLength(the_audited_prefix_length);
    }
  }

  /**
   * @return the number of samples that have been included in the
   * audit calculations so far.
   */
  public Integer auditedSampleCount() {
    return my_audited_sample_count;
  }
  
  /**
   * Sets the number of samples that have been included in the
   * audit calculations so far.
   * 
   * @param the_audited_sample_count The audited sample count.
   */
  public void setAuditedSampleCount(final int the_audited_sample_count) {
    my_audited_sample_count = the_audited_sample_count;
  }

  /**
   * @return a String representation of this contest.
   */
  @Override
  public String toString() {
    return "CountyDashboard [county=" + id() + "]";
  }

  /**
   * Compare this object with another for equivalence.
   * 
   * @param the_other The other object.
   * @return true if the objects are equivalent, false otherwise.
   */
  @Override
  public boolean equals(final Object the_other) {
    boolean result = true;
    if (the_other instanceof CountyDashboard) {
      final CountyDashboard other_cdb = (CountyDashboard) the_other;
      // there can only be one county dashboard in the system for each
      // ID, so we check their equivalence by ID
      result &= nullableEquals(other_cdb.id(), id());
    } else {
      result = false;
    }
    return result;
  }
  
  /**
   * @return a hash code for this object.
   */
  @Override
  public int hashCode() {
    return id().hashCode();
  }
}
