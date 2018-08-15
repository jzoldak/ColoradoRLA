/** Copyright (C) 2018 the Colorado Department of State  **/

package us.freeandfair.corla.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.model.Contest;
import us.freeandfair.corla.model.County;
import us.freeandfair.corla.model.ContestResult;
import us.freeandfair.corla.persistence.Persistence;


public final class ContestResultQueries {
  /**
   * Private constructor to prevent instantiation.
   */
  private ContestResultQueries() {
    // do nothing
  }

  /**
   * Obtain a persistent ContestResult object for the specified
   * county ID and contest. If there is not already a matching persistent 
   * object, one is created and returned.
   *
   * @param the_county The county.
   * @param the_contest The contest.
   * @return the matched ContestResult object, if one exists.
   */
  public static ContestResult matching(final Contest the_contest) {
    ContestResult result = null;
    final int one = 1;//pmd

    try {
      @SuppressWarnings("PMD.PrematureDeclaration")
      final Session s = Persistence.currentSession();
      final CriteriaBuilder cb = s.getCriteriaBuilder();
      final CriteriaQuery<ContestResult> cq =
          cb.createQuery(ContestResult.class);
      final Root<ContestResult> root = cq.from(ContestResult.class);
      final List<Predicate> conjuncts = new ArrayList<>();
      // conjuncts.add(cb.equal(root.get("my_county"), the_county));
      conjuncts.add(cb.equal(root.get("my_contest"), the_contest));
      cq.select(root).where(cb.and(conjuncts.toArray(new Predicate[conjuncts.size()])));
      final TypedQuery<ContestResult> query = s.createQuery(cq);
      final List<ContestResult> query_results = query.getResultList();
      // there should only be one, if one exists
      if (query_results.size() == one) {
        result = query_results.get(0);
      } else if (query_results.isEmpty()) {
        result = new ContestResult(the_contest);
        Persistence.saveOrUpdate(result);
        return null;
      } else {
        throw new IllegalStateException("unique constraint violated on ContestResult");
      }
    } catch (final PersistenceException e) {
      e.printStackTrace(System.out);
      Main.LOGGER.error("could not query database for contest results");
    }
    if (result == null) {
      Main.LOGGER.debug("found no contest results matching + " + the_contest);
    } else {
      Main.LOGGER.debug("found contest results " + result);
    }
    return result;
  }

  public static ContestResult withContestName(final String contestName) {
    final Session s = Persistence.currentSession();
    final Query q = s.createQuery("select cr from ContestResult cr " +
                                  "where cr.contestName = :contestName");
    q.setParameter("contestName", contestName);
    List<ContestResult> results = q.getResultList();
    if (results.size() == 1) {
      return results.get(0);
    } else {
      return null;
    }
  }

  /**
   * find a ContestResult for a CONTEST if it exists, otherwise, create one
   **/
  public static ContestResult forContest(final Contest contest) {
    ContestResult result = null;
    final int one = 1;

    try {
      final Session s = Persistence.currentSession();
      final CriteriaBuilder cb = s.getCriteriaBuilder();
      final CriteriaQuery<ContestResult> cq =
          cb.createQuery(ContestResult.class);
      final Root<ContestResult> root = cq.from(ContestResult.class);
      final List<Predicate> conjuncts = new ArrayList<>();
      conjuncts.add(cb.equal(root.get("contest"), contest));
      cq.select(root).where(cb.and(conjuncts.toArray(new Predicate[conjuncts.size()])));
      final TypedQuery<ContestResult> query = s.createQuery(cq);
      final List<ContestResult> query_results = query.getResultList();
      // there should only be one, if one exists
      if (query_results.size() == one) {
        result = query_results.get(0);
      } else if (query_results.isEmpty()) {
        result = new ContestResult(contest);
        Persistence.saveOrUpdate(result);
      } else {
        throw new IllegalStateException("unique constraint violated on ContestResult");
      }
    } catch (final PersistenceException e) {
      e.printStackTrace(System.out);
      Main.LOGGER.error("could not query database for contest results");
    }
    return result;
  }


}
