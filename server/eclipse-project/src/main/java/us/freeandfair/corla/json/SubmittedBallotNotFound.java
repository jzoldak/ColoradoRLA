/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Aug 13, 2017
 * @copyright 2017 Colorado Department of State
 * @license SPDX-License-Identifier: AGPL-3.0-or-later
 * @creator Daniel M. Zimmerman <dmz@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.json;

/**
 * A submitted ballot not found ID.
 * 
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @version 1.0.0
 */
public class SubmittedBallotNotFound {
  /**
   * The id.
   */
  private final Long my_id;

  /** flag to indicate whether this is a review-and-reaudit submission
   * - needs to not be final so it can be optional and have a default
   **/
  private final Boolean reaudit;

  /** a comment is required to explain why a reaudit is happening **/
  private final String comment;

  /**
   * Constructs a new SubmittedBallotNotFound.
   * 
   * @param the_id The id.
   */
  public SubmittedBallotNotFound(final Long the_id) {
    my_id = the_id;
    this.reaudit = false;
    this.comment = "";
  }

  /**
   * Constructs a new SubmittedBallotNotFound.
   *
   * @param the_id The id.
   */
  public SubmittedBallotNotFound(final Long the_id,
                                 final Boolean reaudit,
                                 final String comment) {
    my_id = the_id;
    this.comment = comment;
    this.reaudit = reaudit;
  }

  /**
   * @return the id.
   */
  public Long id() {
    return my_id;
  }

  /** reaudit can be null because it is optional **/
  public Boolean isReaudit() {
    return this.reaudit != null && this.reaudit;
  }

  /** get the comment **/
  public String getComment() {
    return this.comment;
  }
}
