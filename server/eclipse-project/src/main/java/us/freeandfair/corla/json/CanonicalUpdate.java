package us.freeandfair.corla.json;

import java.util.List;

public class CanonicalUpdate {
  public String contestId; // the contest db id
  public String name; // the new name
  public Long countyId;
  public List<ChoiceChange> choices;

  public class ChoiceChange {
    public String oldName;
    public String newName;
  }
}
