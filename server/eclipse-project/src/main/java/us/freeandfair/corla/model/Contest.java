/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * 
 * @created Jul 25, 2017
 * 
 * @copyright 2017 Free & Fair
 * 
 * @license GNU General Public License 3.0
 * 
 * @author Joey Dodds <jdodds@galois.com>
 * 
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.model;

import java.util.Collections;
import java.util.Set;

/**
 * The definition of a contest; comprises a contest name and a set of possible
 * choices.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
public class Contest {
  /**
   * The contest name.
   */
  private final String my_name;

  /**
   * The contest description.
   */
  private final String my_description;
  
  /**
   * The set of contest choices.
   */
  private final Set<Choice> my_choices;
  
  /**
   * Constructs a contest with the specified parameters.
   * 
   * @param the_name The contest name.
   * @param the_description The contest description.
   * @param the_choices The set of contest choices.
   */
  public Contest(final String the_name, final String the_description, 
                 final Set<Choice> the_choices) {
    super();
    my_name = the_name;
    my_description = the_description;
    my_choices = the_choices;
  }
  
  /**
   * @return the contest name.
   */
  public String name() {
    return my_name;
  }
  
  /**
   * @return the contest description.
   */
  public String description() {
    return my_description;
  }
  
  /**
   * @return the contest choices.
   */
  public Set<Choice> choices() {
    return Collections.unmodifiableSet(my_choices);
  }
  
  /**
   * @return a String representation of this contest.
   */
  @Override
  public String toString() {
    return "Contest [name=" + my_name + ", description=" +
           my_description + ", choices=" + my_choices + "]";
  }

  /**
   * Compare this object with another for equivalence.
   * 
   * @param the_other The other object.
   * @return true if the objects are equivalent, false otherwise.
   */
  @Override
  public boolean equals(final Object the_other) {
    boolean result = false;
    if (the_other != null && getClass().equals(the_other.getClass())) {
      final Contest other_contest = (Contest) the_other;
      result &= other_contest.name().equals(name());
      result &= other_contest.description().equals(description());
      result &= other_contest.choices().equals(choices());
    }
    return result;
  }
  
  /**
   * @return a hash code for this object.
   */
  @Override
  public int hashCode() {
    // can't just use toString() because order of choices may differ
    final String hash_string = name() + description() + choices().hashCode();
    return hash_string.hashCode();
  }
}
