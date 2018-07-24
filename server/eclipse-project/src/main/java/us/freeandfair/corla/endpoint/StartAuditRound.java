/*
 * Free & Fair Colorado RLA System
 *
 * @title ColoradoRLA
 * @created Aug 12, 2017
 * @copyright 2017 Colorado Department of State
 * @license SPDX-License-Identifier: AGPL-3.0-or-later
 * @creator Joseph R. Kiniry <kiniry@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.endpoint;
import static us.freeandfair.corla.asm.ASMEvent.AuditBoardDashboardEvent.*;
import static us.freeandfair.corla.asm.ASMEvent.CountyDashboardEvent.*;
import static us.freeandfair.corla.asm.ASMEvent.DoSDashboardEvent.*;
import static us.freeandfair.corla.asm.ASMState.DoSDashboardState.COMPLETE_AUDIT_INFO_SET;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import com.google.gson.JsonParseException;

import spark.Request;
import spark.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.asm.ASMEvent;
import us.freeandfair.corla.asm.ASMState.CountyDashboardState;
import us.freeandfair.corla.asm.ASMUtilities;
import us.freeandfair.corla.asm.AuditBoardDashboardASM;
import us.freeandfair.corla.asm.CountyDashboardASM;
import us.freeandfair.corla.controller.ComparisonAuditController;
import us.freeandfair.corla.json.SubmittedAuditRoundStart;
import us.freeandfair.corla.model.CountyDashboard;
import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.util.SuppressFBWarnings;

/**
 * Starts a new audit round for one or more counties.
 *
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @version 1.0.0
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity",
                   "PMD.AtLeastOneConstructor", "PMD.ModifiedCyclomaticComplexity",
                   "PMD.NPathComplexity"})
public class StartAuditRound extends AbstractDoSDashboardEndpoint {
  private static final Logger LOG = LogManager.getLogger(StartAuditRound.class);

  /**
   * The "county " string.
   */
  private static final String COUNTY = "county ";

  /**
   * The event to return for this endpoint.
   */
  private final ThreadLocal<ASMEvent> my_event = new ThreadLocal<ASMEvent>();

  /**
   * {@inheritDoc}
   */
  @Override
  public EndpointType endpointType() {
    return EndpointType.POST;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String endpointName() {
    return "/start-audit-round";
  }

  /**
   * @return STATE authorization is necessary for this endpoint.
   */
  public AuthorizationType requiredAuthorization() {
    return AuthorizationType.STATE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ASMEvent endpointEvent() {
    return my_event.get();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {
    my_event.set(null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String endpointBody(final Request the_request,
                         final Response the_response) {
    if (my_asm.get().currentState() == COMPLETE_AUDIT_INFO_SET) {
      // the audit hasn't started yet, so start round 1 and ignore the parameters
      // we were sent
      my_event.set(DOS_START_ROUND_EVENT);
      return startRoundOne(the_request, the_response);
    } else {
      // start a subsequent round
      my_event.set(DOS_START_ROUND_EVENT);
      return startSubsequentRound(the_request, the_response);
    }
  }

  /**
   * Starts the first audit round.
   *
   * @param the_request The HTTP request.
   * @param the_response The HTTP response.
   * @return the result for endpoint.
   */
  public String startRoundOne(final Request the_request, final Response the_response) {
    // update every county dashboard with a list of ballots to audit
    try {
      final List<CountyDashboard> cdbs = Persistence.getAll(CountyDashboard.class);

      // this flag starts off true if we're going to conjoin it with all the ASM
      // states, and false otherwise as we just assume audit reasonableness in the
      // absence of ASMs
      boolean audit_complete = !DISABLE_ASM;

      for (final CountyDashboard cdb : cdbs) {
        try {
          if (cdb.cvrFile() == null || cdb.manifestFile() == null) {
            LOG.info(COUNTY + cdb + " missed the file upload deadline");
          } else {
            // find the initial window
            final boolean started = ComparisonAuditController.initializeAuditData(cdb);

            // FIXME is this an ad-hoc version of `cdb.auditStatus()`?
            if (started) {
              LOG.info(cdb + " estimated to audit " +
                       cdb.estimatedSamplesToAudit() + " ballots in round 1");
            } else if (cdb.drivingContests().isEmpty()) {
              LOG.info(cdb +
                       " has no driving contests, its audit is complete.");
            } else if (cdb.estimatedSamplesToAudit() == 0) {
              LOG.info(cdb +
                       " needs to audit 0 ballots to " +
                       "achieve its risk limit, its audit is complete.");
            } else {
              LOG.error("unable to start audit for county " + cdb.id());
            }

            Persistence.saveOrUpdate(cdb);
          }
          // update the ASMs for the county and audit board
          if (!DISABLE_ASM) {
            final CountyDashboardASM asm =
                ASMUtilities.asmFor(CountyDashboardASM.class, String.valueOf(cdb.id()));
            asm.stepEvent(COUNTY_START_AUDIT_EVENT);
            final ASMEvent audit_event;
            if (asm.currentState().equals(CountyDashboardState.COUNTY_AUDIT_UNDERWAY)) {
              if (cdb.comparisonAudits().isEmpty()) {
                // the county made its deadline but was assigned no contests to audit
                audit_event = NO_CONTESTS_TO_AUDIT_EVENT;
                asm.stepEvent(COUNTY_AUDIT_COMPLETE_EVENT);
              } else if (cdb.estimatedSamplesToAudit() <= 0) {
                // the county made its deadline but has already achieved its risk limit
                audit_event = RISK_LIMIT_ACHIEVED_EVENT;
                asm.stepEvent(COUNTY_AUDIT_COMPLETE_EVENT);
              } else {
                // the audit started normally
                audit_event = ROUND_START_EVENT;
              }
            } else {
              // the county missed its deadline
              audit_event = COUNTY_DEADLINE_MISSED_EVENT;
            }
            ASMUtilities.step(audit_event, AuditBoardDashboardASM.class,
                              String.valueOf(cdb.id()));
            ASMUtilities.save(asm);

            // figure out whether this county is done, or whether there's an audit to run
            audit_complete &= asm.isInFinalState();
          }
        } catch (final IllegalArgumentException e) {
          e.printStackTrace(System.out);
          serverError(the_response, "could not start round 1 for county " +
                      cdb.id());
          LOG.info("could not start round 1 for county " + cdb.toString());
        } catch (final IllegalStateException e) {
          illegalTransition(the_response, e.getMessage());
        }
      }

      if (audit_complete) {
        my_event.set(DOS_AUDIT_COMPLETE_EVENT);
        ok(the_response, "audit complete");
      } else {
        ok(the_response, "round 1 started");
      }
    } catch (final PersistenceException e) {
      serverError(the_response, "could not start round 1");
    }

    return my_endpoint_result.get();
  }


  /**
   * Figure out what counties we need to do this for, if the list is limited
   * @returns List<CountyDashboard>
   * @param Map<Long, Integer> countyBallots the map from county IDs to absolute numbers of ballots per county
   */
  public List<CountyDashboard> countiesToStart(SubmittedAuditRoundStart start) {
    List<CountyDashboard> result;

    if (start.countyBallots() == null || start.countyBallots().isEmpty()) {
      result = Persistence.getAll(CountyDashboard.class);
    } else {
      result = new ArrayList<>();
      for (final Long id : start.countyBallots().keySet()) {
        result.add(Persistence.getByID(id, CountyDashboard.class));
      }
    }

    return result;
  }

  /**
   * Starts a subsequent audit round.
   *
   * @param the_request The HTTP request.
   * @param the_response The HTTP response.
   * @return the result for endpoint.
   */
  // FIXME remove the two comments below, as well as their cause
  // FindBugs thinks there's a possible NPE, but there's not because
  // badDataContents() would bail on the method before it happened.
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
  public String startSubsequentRound(final Request the_request, final Response the_response) {
    SubmittedAuditRoundStart start = null;
    try {
      start = Main.GSON.fromJson(the_request.body(), SubmittedAuditRoundStart.class);
      if (start == null) {
        badDataContents(the_response, "malformed request data");
      }
    } catch (final JsonParseException e) {
      badDataContents(the_response, "malformed request data: " + e.getMessage());
    }

    try {
      final List<CountyDashboard> cdbs = countiesToStart(start);

      LOG.debug("Starting these counties: " + cdbs);

      // FIXME extract-fn: startRoundForCounty
      //
      // Given a county dashboard `cdb` do the following things:
      // 1. Find the AuditBoardDashboardASM for the county.
      // 2. Skip this county if there's no audit in progress.
      // 3. Throw an exception if there's a round in progress and you're trying to start another one.
      // 4. Figure out the multiplier for this round.
      // 5. Figure out if we're using estimates or starting based on length.
      // 6. Figure out which kind of event we should send the Audit Board.
      // 7. Update the Audit Board about what's happening with our event.
      for (final CountyDashboard cdb : cdbs) {
        final AuditBoardDashboardASM asm =
            ASMUtilities.asmFor(AuditBoardDashboardASM.class, cdb.id().toString());

        // FIXME this could filter the dashboards by those that had audits in progress.
        // 2. there is no audit happening, so skip along
        if (asm.isInInitialState() || asm.isInFinalState()) {
          LOG.trace("no audit ongoing in county " + cdb.id() + ", skipping round start");
          continue;
        }

        // 3. if the county is in the middle of a round, error out
        if (cdb.currentRound() != null) {
          invariantViolation(the_response,
                             "audit round already in progress for county " + cdb.id());
        }

        final ASMEvent audit_event;
        final boolean round_started;
        final BigDecimal multiplier;

        // 4. figure out the start multiplier. what's this all about?
        if (start.multiplier() == null) {
          multiplier = BigDecimal.ONE;
        } else {
          multiplier = start.multiplier();
        }

        // 5. Figure out if we're using estimates or starting based on length
        if (start.useEstimates()) {
            LOG.debug("Starting round using estimates");
          round_started =
              ComparisonAuditController.startNewRoundFromEstimates(cdb, multiplier);
        } else {
          round_started = ComparisonAuditController.
              startNewRoundOfLength(cdb, start.countyBallots().get(cdb.id()), multiplier);
        }

        // 6. Figure out which event we should throw
        if (round_started) {
          LOG.debug("round started for county " + cdb.id());
          audit_event = ROUND_START_EVENT;
        } else {
          // we don't know why the round didn't start, so we need to abort the audit
          LOG.warn("no round started for " + cdb + "; aboring audit!");
          audit_event = ABORT_AUDIT_EVENT;
        }

        // 7. Update the Audit Board about what's happening
        // update the ASM for the audit board
        if (!DISABLE_ASM) {
          asm.stepEvent(audit_event);
          ASMUtilities.save(asm);
        }
      } // end for-loop, or extracted-function.

      ok(the_response, "new audit round started");
    } catch (final PersistenceException e) {
      serverError(the_response, "could not start new audit round");
    }

    return my_endpoint_result.get();
  }
}
