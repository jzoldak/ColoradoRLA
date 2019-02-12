package us.freeandfair.corla.query;

import java.util.List;
import java.util.ArrayList;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import static org.testng.Assert.*;


import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.Contest;
import us.freeandfair.corla.model.Choice;
import us.freeandfair.corla.model.County;
import us.freeandfair.corla.model.CVRContestInfo;
import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.query.Setup;

@Test(groups = {"integration"})
public class CastVoteRecordQueriesTest {


  @BeforeTest()
  public void setUp() {
    Setup.setProperties();
    Persistence.beginTransaction();
  }

  @AfterTest()
  public void tearDown() {
    try {
    Persistence.rollbackTransaction();
    } catch (Exception e) {
    }
  }

  @Test()
  public void deleteAllTest() {
    //vvvvv THIS IS ALL NOISE vvvvv
    County c = new County("test", 1L);

    List<Choice> choices = new ArrayList();
    Choice choice = new Choice("why?",
                               "",
                               false,
                               false);
    choices.add(choice);
    Contest co = new Contest("test",
                             c,
                             "",
                             choices,
                             1,
                             1,
                             1);
    co.setID(1L);

    List<String> votes = new ArrayList();
    votes.add("why?");

    //^^^^^ THIS IS ALL NOISE ^^^^^
    CVRContestInfo ci = new CVRContestInfo(co, null,null, votes);
    List<CVRContestInfo> contest_info = new ArrayList();
    contest_info.add(ci);
    CastVoteRecord cvr = new CastVoteRecord(CastVoteRecord.RecordType.UPLOADED,
                                            null,
                                            1L,
                                            1,
                                            1,
                                            1,
                                            "1",
                                            1,
                                            "1",
                                            "a",
                                            contest_info);
    Persistence.save(c);
    Persistence.save(co);
    Persistence.save(cvr);

    // Without flushing the persistence context, `deleteAll(1L)` will
    // throw an exception.
    Persistence.flush();

    // this is the method under test
    Integer result = CastVoteRecordQueries.deleteAll(1L);

    assertEquals((int) result, (int) 1,
                 "a result of 1 means one thing was deleted");
  }

  @Test()
  public void canonicalChoicesTest() {
    CastVoteRecord cvr = noisyCVRSetup(3);

    Integer result = CastVoteRecordQueries.updateCVRContestInfos(1L,"why?","because.");
    assertEquals((int) result, (int) 1,
                 "a result of 1 means one choice was changed");
    Persistence.currentSession().refresh(cvr);
    assertEquals(cvr.contestInfo().toString().contains("choices=[because.]"), true);
  }
}
