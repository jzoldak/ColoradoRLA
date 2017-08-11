/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Aug 8, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Joe Kiniry <kiniry@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.asm;

import static us.freeandfair.corla.asm.ASMEvent.AuditBoardDashboardEvent.*;
import static us.freeandfair.corla.asm.ASMEvent.CountyDashboardEvent.*;
import static us.freeandfair.corla.asm.ASMEvent.DoSDashboardEvent.*;
import static us.freeandfair.corla.asm.ASMEvent.RLAToolEvent.RLA_TOOL_SKIP_EVENT;
import static us.freeandfair.corla.asm.ASMState.AuditBoardDashboardState.*;
import static us.freeandfair.corla.asm.ASMState.CountyDashboardState.*;
import static us.freeandfair.corla.asm.ASMState.DoSDashboardState.*;
import static us.freeandfair.corla.asm.ASMState.RLAToolState.RLA_TOOL_INITIAL_STATE;

/**
 * The generic idea of an ASM transition function.
 * @trace asm.asm_transition_function
 * @author Joseph R. Kiniry <kiniry@freeandfair.us>
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @version 0.0.1
 */
@SuppressWarnings({"PMD.TooManyStaticImports", "PMD.AvoidDuplicateLiterals"})
public interface ASMTransitionFunction {
  /**
   * The Department of State Dashboard's transition function.
   * @trace asm.dos_dashboard_next_state
   */
  enum DoSDashboardTransitionFunction implements ASMTransitionFunction {
    ONE(new ASMTransition(DOS_INITIAL_STATE, 
                          AUTHENTICATE_STATE_ADMINISTRATOR_EVENT,
                          DOS_AUTHENTICATED)),
    TWO(new ASMTransition(DOS_AUTHENTICATED,
                          ESTABLISH_RISK_LIMIT_FOR_COMPARISON_AUDITS_EVENT,
                          RISK_LIMITS_SET)),
    THREE(new ASMTransition(RISK_LIMITS_SET, 
                            SELECT_CONTESTS_FOR_COMPARISON_AUDIT_EVENT,
                            CONTESTS_TO_AUDIT_IDENTIFIED)),
    FOUR(new ASMTransition(DATA_TO_AUDIT_PUBLISHED, 
                           PUBLIC_SEED_EVENT,
                           RANDOM_SEED_PUBLISHED)),
    FIVE(new ASMTransition(RANDOM_SEED_PUBLISHED, 
                           PUBLISH_BALLOTS_TO_AUDIT_EVENT,
                           BALLOT_ORDER_DEFINED)),
    SIX(new ASMTransition(BALLOT_ORDER_DEFINED, 
                          DOS_SKIP_EVENT,
                          AUDIT_READY_TO_START)),
    // @review kiniry Should this transition just be a DOS_SKIP?
    SEVEN(new ASMTransition(AUDIT_READY_TO_START, 
                            DOS_SKIP_EVENT,
                            DOS_AUDIT_ONGOING)),
    EIGHT(new ASMTransition(DOS_AUDIT_ONGOING, 
                            AUDIT_EVENT,
                            DOS_AUDIT_ONGOING)),
    NINE(new ASMTransition(AUDIT_READY_TO_START, 
                           INDICATE_FULL_HAND_COUNT_CONTEST_EVENT,
                           AUDIT_READY_TO_START)),
    TEN(new ASMTransition(DOS_AUDIT_ONGOING, 
                          INDICATE_FULL_HAND_COUNT_CONTEST_EVENT,
                          DOS_AUDIT_ONGOING)),
    ELEVEN(new ASMTransition(DOS_AUDIT_ONGOING, 
                             COUNTY_AUDIT_COMPLETE_EVENT,
                             DOS_AUDIT_COMPLETE)),
    TWELVE(new ASMTransition(DOS_AUDIT_COMPLETE, 
                             PUBLISH_AUDIT_REPORT_EVENT,
                             AUDIT_RESULTS_PUBLISHED)),
    THIRTEEN(new ASMTransition(DOS_AUDIT_ONGOING, 
                               DOS_REFRESH_EVENT,
                               DOS_AUDIT_ONGOING)),
    FOURTEEN(new ASMTransition(CONTESTS_TO_AUDIT_IDENTIFIED, 
                               PUBLISH_AUDIT_DATA_EVENT,
                               DATA_TO_AUDIT_PUBLISHED));
    
    /**
     * A single transition.
     */
    @SuppressWarnings("PMD.ConstantsInInterface")
    private final transient ASMTransition my_transition;
    
    /**
     * Create a transition.
     * @param the_pair the (current state, event) pair.
     * @param the_state the state transitioned to when the pair is witnessed.
     */
    DoSDashboardTransitionFunction(final ASMTransition the_transition) {
      my_transition = the_transition;
    }
    
    /**
     * @return the pair encoding this enumeration.
     */
    public ASMTransition value() {
      return my_transition;
    }
  }
  
  /**
   * The County Board Dashboard's transition function.
   * @trace asm.county_dashboard_next_state
   */
  enum CountyDashboardTransitionFunction implements ASMTransitionFunction {
    ONE(new ASMTransition(COUNTY_INITIAL_STATE, 
                          AUTHENTICATE_COUNTY_ADMINISTRATOR_EVENT,
                          COUNTY_AUTHENTICATED)),
    TWO(new ASMTransition(COUNTY_AUTHENTICATED, 
                          ESTABLISH_AUDIT_BOARD_EVENT,
                          AUDIT_BOARD_ESTABLISHED_STATE)),
    THREE(new ASMTransition(AUDIT_BOARD_ESTABLISHED_STATE, 
                            UPLOAD_BALLOT_MANIFEST_EVENT,
                            UPLOAD_BALLOT_MANIFEST_UPLOAD_SUCESSFUL)),
    FOUR(new ASMTransition(AUDIT_BOARD_ESTABLISHED_STATE, 
                           UPLOAD_BALLOT_MANIFEST_EVENT,
                           UPLOAD_BALLOT_MANIFEST_TOO_LATE)),
    FIVE(new ASMTransition(AUDIT_BOARD_ESTABLISHED_STATE, 
                           UPLOAD_BALLOT_MANIFEST_EVENT,
                           UPLOAD_BALLOT_MANIFEST_INTERRUPTED)),
    SIX(new ASMTransition(UPLOAD_BALLOT_MANIFEST_INTERRUPTED, 
                          COUNTY_SKIP_EVENT,
                          AUDIT_BOARD_ESTABLISHED_STATE)),
    SEVEN(new ASMTransition(AUDIT_BOARD_ESTABLISHED_STATE, 
                            UPLOAD_BALLOT_MANIFEST_EVENT,
                            UPLOAD_BALLOT_MANIFEST_FILE_TYPE_WRONG)),
    EIGHT(new ASMTransition(UPLOAD_BALLOT_MANIFEST_FILE_TYPE_WRONG, 
                            COUNTY_SKIP_EVENT,
                            AUDIT_BOARD_ESTABLISHED_STATE)),
    NINE(new ASMTransition(UPLOAD_BALLOT_MANIFEST_CHECKING_HASH, 
                           COUNTY_SKIP_EVENT,
                           UPLOAD_BALLOT_MANIFEST_HASH_WRONG)),
    TEN(new ASMTransition(UPLOAD_BALLOT_MANIFEST_HASH_WRONG, 
                          COUNTY_SKIP_EVENT,
                          AUDIT_BOARD_ESTABLISHED_STATE)),
    ELEVENT(new ASMTransition(UPLOAD_BALLOT_MANIFEST_UPLOAD_SUCESSFUL, 
                              COUNTY_SKIP_EVENT,
                              UPLOAD_BALLOT_MANIFEST_CHECKING_HASH)),
    TWELVE(new ASMTransition(UPLOAD_BALLOT_MANIFEST_CHECKING_HASH, 
                             COUNTY_SKIP_EVENT,
                             UPLOAD_BALLOT_MANIFEST_HASH_VERIFIED)),
    THIRTEEN(new ASMTransition(UPLOAD_BALLOT_MANIFEST_HASH_VERIFIED, 
                               COUNTY_SKIP_EVENT,
                               UPLOAD_BALLOT_MANIFEST_PARSING_DATA)),
    FOURTEEN(new ASMTransition(UPLOAD_BALLOT_MANIFEST_PARSING_DATA, 
                               COUNTY_SKIP_EVENT,
                               UPLOAD_BALLOT_MANIFEST_FILE_TYPE_WRONG)),
    FIFTEEN(new ASMTransition(UPLOAD_BALLOT_MANIFEST_PARSING_DATA, 
                              COUNTY_SKIP_EVENT,
                              UPLOAD_BALLOT_MANIFEST_DATA_PARSED)),
    SIXTEEN(new ASMTransition(UPLOAD_BALLOT_MANIFEST_DATA_PARSED, 
                              UPLOAD_CVRS_EVENT,
                              UPLOAD_CVRS_TOO_LATE)),
    SEVENTEEN(new ASMTransition(UPLOAD_BALLOT_MANIFEST_DATA_PARSED,
                                UPLOAD_CVRS_EVENT,
                                UPLOAD_CVRS_DATA_TRANSMISSION_INTERRUPTED)),
    EIGHTEEN(new ASMTransition(UPLOAD_CVRS_DATA_TRANSMISSION_INTERRUPTED, 
                               COUNTY_SKIP_EVENT,
                               UPLOAD_BALLOT_MANIFEST_DATA_PARSED)),
    NINETEEN(new ASMTransition(UPLOAD_BALLOT_MANIFEST_DATA_PARSED, 
                               UPLOAD_CVRS_EVENT,
                               UPLOAD_CVRS_FILE_TYPE_WRONG)),
    TWENTY(new ASMTransition(UPLOAD_CVRS_FILE_TYPE_WRONG, 
                             COUNTY_SKIP_EVENT,
                             UPLOAD_BALLOT_MANIFEST_DATA_PARSED)),
    TWENTYONE(new ASMTransition(UPLOAD_BALLOT_MANIFEST_DATA_PARSED, 
                                UPLOAD_CVRS_EVENT,
                                UPLOAD_CVRS_UPLOAD_SUCESSFUL)),
    TWENTYTWO(new ASMTransition(UPLOAD_CVRS_UPLOAD_SUCESSFUL, 
                                COUNTY_SKIP_EVENT,
                                UPLOAD_CVRS_CHECKING_HASH)),
    TWENTYTHREE(new ASMTransition(UPLOAD_CVRS_CHECKING_HASH, 
                                  COUNTY_SKIP_EVENT,
                                  UPLOAD_CVRS_HASH_WRONG)),
    TWENTYFOUR(new ASMTransition(UPLOAD_CVRS_HASH_WRONG, 
                                 COUNTY_SKIP_EVENT,
                                 UPLOAD_BALLOT_MANIFEST_DATA_PARSED)),
    TWENTYFIVE(new ASMTransition(UPLOAD_CVRS_CHECKING_HASH, 
                                 COUNTY_SKIP_EVENT,
                                 UPLOAD_CVRS_HASH_VERIFIED)),
    TWENTYSIX(new ASMTransition(UPLOAD_CVRS_HASH_VERIFIED, 
                                COUNTY_SKIP_EVENT,
                                UPLOAD_CVRS_PARSING_DATA)),
    TWENTYSEVEN(new ASMTransition(UPLOAD_CVRS_PARSING_DATA, 
                                  COUNTY_SKIP_EVENT,
                                  UPLOAD_CVRS_FILE_TYPE_WRONG)),
    TWENTYEIGHT(new ASMTransition(UPLOAD_CVRS_PARSING_DATA, 
                                  COUNTY_SKIP_EVENT,
                                  UPLOAD_CVRS_DATA_PARSED)),
    TWENTYNINE(new ASMTransition(UPLOAD_CVRS_DATA_PARSED, 
                                 START_AUDIT_EVENT,
                                 COUNTY_AUDIT_UNDERWAY)),
    THIRTY(new ASMTransition(COUNTY_AUDIT_UNDERWAY, 
                             COUNTY_REFRESH_EVENT,
                             COUNTY_AUDIT_UNDERWAY)),
    THIRTYONE(new ASMTransition(COUNTY_AUDIT_UNDERWAY, 
                                COUNTY_SKIP_EVENT,
                                COUNTY_AUDIT_COMPLETE));

    /**
     * A single transition.
     */
    @SuppressWarnings("PMD.ConstantsInInterface")
    private final transient ASMTransition my_transition;
    
    /**
     * Create a transition.
     * @param the_pair the (current state, event) pair.
     * @param the_state the state transitioned to when the pair is witnessed.
     */
    CountyDashboardTransitionFunction(final ASMTransition the_transition) {
      my_transition = the_transition;
    }
    
    /**
     * @return the pair encoding this enumeration.
     */
    public ASMTransition value() {
      return my_transition;
    }
  }
  
  /**
   * The Audit Board Dashboard's transition function.
   * @trace asm.audit_board_dashboard_next_state
   */
  enum AuditBoardDashboardTransitionFunction implements ASMTransitionFunction {
    ONE(new ASMTransition(AUDIT_INITIAL_STATE, 
                          AUDIT_SKIP_EVENT,
                          AUDIT_IN_PROGRESS_STATE)),
    TWO(new ASMTransition(AUDIT_IN_PROGRESS_STATE, 
                          REPORT_MARKINGS_EVENT,
                          AUDIT_IN_PROGRESS_STATE)),
    THREE(new ASMTransition(AUDIT_IN_PROGRESS_STATE, 
                            REPORT_BALLOT_NOT_FOUND_EVENT,
                            AUDIT_IN_PROGRESS_STATE)),
    FOUR(new ASMTransition(AUDIT_IN_PROGRESS_STATE, 
                           SUBMIT_AUDIT_INVESTIGATION_REPORT_EVENT,
                           AUDIT_IN_PROGRESS_STATE)),
    FIVE(new ASMTransition(AUDIT_IN_PROGRESS_STATE, 
                           SUBMIT_INTERMEDIATE_AUDIT_REPORT_EVENT,
                           INTERMEDIATE_AUDIT_REPORT_SUBMITTED_STATE)),
    SIX(new ASMTransition(AUDIT_IN_PROGRESS_STATE, 
                          SUBMIT_AUDIT_REPORT_EVENT,
                          AUDIT_REPORT_SUBMITTED_STATE)),
    SEVEN(new ASMTransition(INTERMEDIATE_AUDIT_REPORT_SUBMITTED_STATE,
                            AUDIT_SKIP_EVENT,
                            AUDIT_IN_PROGRESS_STATE)),
    EIGHT(new ASMTransition(AUDIT_IN_PROGRESS_STATE, 
                            AUDIT_REFRESH_EVENT,
                            AUDIT_IN_PROGRESS_STATE));
    
    /**
     * A single transition.
     */
    @SuppressWarnings("PMD.ConstantsInInterface")
    private final transient ASMTransition my_transition;
    
    /**
     * Create a transition.
     * @param the_pair the (current state, event) pair.
     * @param the_state the state transitioned to when the pair is witnessed.
     */
    AuditBoardDashboardTransitionFunction(final ASMTransition the_transition) {
      my_transition = the_transition;
    }
    
    /**
     * @return the pair encoding this enumeration.
     */
    public ASMTransition value() {
      return my_transition;
    }
  }
  
  /**
   * The RLA Tools transition function.
   * @trace asm.rla_tool_asm
   */
  enum RLATransitionFunction implements ASMTransitionFunction {
    ONE(new ASMTransition(RLA_TOOL_INITIAL_STATE, 
                          RLA_TOOL_SKIP_EVENT,
                          DOS_INITIAL_STATE)),
    TWO(new ASMTransition(DOS_INITIAL_STATE, 
                          RLA_TOOL_SKIP_EVENT,
                          DOS_AUDIT_ONGOING)),
    THREE(new ASMTransition(DOS_AUDIT_ONGOING, 
                            RLA_TOOL_SKIP_EVENT,
                            COUNTY_INITIAL_STATE)),
    FOUR(new ASMTransition(COUNTY_INITIAL_STATE, 
                           RLA_TOOL_SKIP_EVENT,
                           UPLOAD_BALLOT_MANIFEST_TOO_LATE)),
    FIVE(new ASMTransition(COUNTY_INITIAL_STATE, 
                           RLA_TOOL_SKIP_EVENT,
                           UPLOAD_CVRS_TOO_LATE)),
    SIX(new ASMTransition(COUNTY_INITIAL_STATE, 
                          RLA_TOOL_SKIP_EVENT,
                          COUNTY_AUDIT_UNDERWAY)),
    SEVEN(new ASMTransition(COUNTY_AUDIT_UNDERWAY, 
                            RLA_TOOL_SKIP_EVENT,
                            AUDIT_INITIAL_STATE)),
    EIGHT(new ASMTransition(AUDIT_INITIAL_STATE, 
                            RLA_TOOL_SKIP_EVENT,
                            AUDIT_REPORT_SUBMITTED_STATE)),
    NINE(new ASMTransition(AUDIT_REPORT_SUBMITTED_STATE, 
                           RLA_TOOL_SKIP_EVENT,
                           COUNTY_AUDIT_COMPLETE)),
    TEN(new ASMTransition(COUNTY_AUDIT_COMPLETE, 
                          RLA_TOOL_SKIP_EVENT,
                          DOS_AUDIT_COMPLETE)),
    ELEVEN(new ASMTransition(DOS_AUDIT_COMPLETE, 
                             RLA_TOOL_SKIP_EVENT,
                             AUDIT_RESULTS_PUBLISHED));

    /**
     * A single transition.
     */
    @SuppressWarnings("PMD.ConstantsInInterface")
    private final transient ASMTransition my_transition;
    
    /**
     * Create a transition.
     * @param the_pair the (current state, event) pair.
     * @param the_state the state transitioned to when the pair is witnessed.
     */
    RLATransitionFunction(final ASMTransition the_transition) {
      my_transition = the_transition;
    }
    
    /**
     * @return the transition.
     */
    public ASMTransition value() {
      return my_transition;
    }
  }
  
  /**
   * @return the transition of this transition function element.
   */
  ASMTransition value();
}
