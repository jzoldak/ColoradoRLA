package us.freeandfair.corla.model;

import static us.freeandfair.corla.util.EqualsHashcodeHelper.nullableEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import us.freeandfair.corla.persistence.PersistentEntity;
import us.freeandfair.corla.persistence.StringSetConverter;

/**
 * A class representing the results for a contest across counties.
 */
@Entity
@Cacheable(true)
@Table(name = "contest_result",
       uniqueConstraints = {
         @UniqueConstraint(columnNames = {"contest_id"}) },
       indexes = { @Index(name = "idx_cr_contest",
                          columnList = "contest_id",
                          unique = true),
                   @Index(name = "idx_ccr_contest", columnList = "contest_id") })
public class ContestResult implements PersistentEntity, Serializable {
  /**
   * The "my_id" string.
   */
  private static final String MY_ID = "my_id";

  /**
   * The "result_id" string.
   */
  private static final String RESULT_ID = "result_id";

  /**
   * The serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  /**
   * The version (for optimistic locking).
   */
  @Version
  private Long version;

  /**
   * The contest relation.
   */
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn
  private Contest contest;

  /**
   * The set of contest winners.
   */
  @Column(name = "winners", columnDefinition = "text")
  @Convert(converter = StringSetConverter.class)
  final private Set<String> winners = new HashSet<>();

  /**
   * The set of contest losers.
   */
  @Column(name = "losers", columnDefinition = "text")
  @Convert(converter = StringSetConverter.class)
  final private Set<String> losers = new HashSet<>();

  /**
   * A map from choices to vote totals.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "contest_vote_total",
                   joinColumns = @JoinColumn(name = RESULT_ID,
                                             referencedColumnName = MY_ID))
  @MapKeyColumn(name = "choice")
  @Column(name = "vote_total")
  private Map<String, Integer> vote_totals = new HashMap<>();

  /**
   * Constructs a new empty ContestResult (solely for persistence).
   */
  public ContestResult() {
    super();
  }

  /**
   * Constructs a new ContestResult for the specified county ID and
   * contest.
   *
   * @param the_contest The contest.
   */
  public ContestResult(final Contest the_contest) {
    super();
    this.contest = the_contest;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long id() {
    return id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setID(final Long the_id) {
    this.id = the_id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long version() {
    return version;
  }

  /**
   * @return the contest
   */
  public Contest contest() {
    return this.contest;
  }

  /**
   * @return the winners for thie ContestResult.
   */
  public Set<String> getWinners() {
    return Collections.unmodifiableSet(this.winners);
  }

  /**
   * @return the losers for this ContestResult.
   */
  public Set<String> getLosers() {
    return Collections.unmodifiableSet(this.losers);
  }

  /**
   * @return a map from choices to vote totals.
   */
  public Map<String, Integer> getVoteTotals() {
    return Collections.unmodifiableMap(this.vote_totals);
  }

  /**
   * @param voteTotals a map from choices to vote totals.
   */
  public void setVoteTotals(final Map<String, Integer> voteTotals) {
    this.vote_totals = voteTotals;
  }

  /**
   * @return a String representation of this contest.
   */
  @Override
  public String toString() {
    return "ContestResult [id=" + id() + "]";
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
    if (the_other instanceof CountyContestResult) {
      final ContestResult other_result = (ContestResult) the_other;
      // compare by database ID, since that is the only
      // context in which they can reasonably be compared
      result &= nullableEquals(other_result.id(), id());
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
  /**
   * A reverse integer comparator, for sorting lists of integers in reverse.
   */
  public static class ReverseIntegerComparator
    implements Comparator<Integer>, Serializable {
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Compares two integers. Returns the negation of the regular integer
     * comparator result.
     *
     * @param the_first The first integer.
     * @param the_second The second integer.
     */
    public int compare(final Integer the_first, final Integer the_second) {
      return the_second.compareTo(the_first);
    }
  }
}
