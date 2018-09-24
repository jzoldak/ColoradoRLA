/*
 * Colorado RLA System
 *
 * @title ColoradoRLA
 * @copyright 2018 Colorado Department of State
 * @license SPDX-License-Identifier: AGPL-3.0-or-later
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import us.freeandfair.corla.controller.BallotSelection;

import us.freeandfair.corla.json.CVRToAuditResponse;

import us.freeandfair.corla.model.CastVoteRecord;

/**
 * Ballot sequencing functionality, such as converting a list of CVRs into
 * a sorted, deduplicated list of CVRs.
 *
 * @author Democracy Works, Inc. <dev@democracy.works>
 */
public final class BallotSequencer {
  /**
   * Prevent public construction
   */
  private BallotSequencer() {
  }

  /**
   * Returns sorted, deduplicated list of CVR IDs given a list of CVRs.
   *
   * The sort order must match the order of the ballots in the "pull list" that
   * counties use to fetch ballots. By storing the sorted, deduplicated list of
   * ballots (CVRs) to audit consistently, we can avoid having to sort them
   * again and reap other benefits like easier partitioning to support multiple
   * audit boards.
   *
   * @param cvrs input CVRs
   * @return sorted, deduplicated list of CVR database IDs
   */
  public static List<CastVoteRecord>
      sortAndDeduplicateCVRs(final List<CastVoteRecord> cvrs) {
    // Deduplicate CVRs.
    final Map<Long, CastVoteRecord> deduplicatedCvrs = cvrs.stream()
      .distinct()
      .collect(Collectors.toMap(cvr -> cvr.id(),
                                Function.identity(),
                                (a, b) -> b));

    // Join with ballot manifest for the purposes of sorting by location, then
    // sort it.
    //
    // TODO: Abusing the CVRToAuditResponse class for sorting is wrong; we
    // should reify the "joined CVR / Ballot Manifest" concept.
    final List<CVRToAuditResponse> sortedAuditResponses =
      BallotSelection.toResponseList(deduplicatedCvrs.entrySet().stream()
                                     .map(m -> m.getValue())
                                     .collect(Collectors.toList()));
    sortedAuditResponses.sort(null);

    // Pull CVR database IDs back out of the now-sorted list.
    return sortedAuditResponses
      .stream()
      .map(x -> deduplicatedCvrs.get(x.dbID()))
      .collect(Collectors.toList());
  }
}
