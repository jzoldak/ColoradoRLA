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

  public List<CVRContestInfo> noisyContestSetup(){
    return noisyContestSetup(1L);
  }

  public List<CVRContestInfo> noisyContestSetup(Long countyId){
    County c = new County("test" + countyId.toString(), countyId);

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

    CVRContestInfo ci = new CVRContestInfo(co, null,null, votes);
    List<CVRContestInfo> contest_info = new ArrayList();
    contest_info.add(ci);

    Persistence.save(c);
    Persistence.save(co);
    Persistence.flush();

    return contest_info;
  }

  public CastVoteRecord noisyCVRSetup() {
    return noisyCVRSetup(1);
  }

  public CastVoteRecord noisyCVRSetup(final Integer position) {
    return noisyCVRSetup(position, noisyContestSetup(Long.valueOf(position)));
  }

  public CastVoteRecord noisyCVRSetup(final Integer position, final List<CVRContestInfo> contest_info) {
    CastVoteRecord cvr = new CastVoteRecord(CastVoteRecord.RecordType.UPLOADED,
                                            null,
                                            1L,
                                            position,
                                            1,
                                            1,
                                            "1",
                                            1,
                                            "1",
                                            "a",
                                            contest_info);
    Persistence.save(cvr);

    // Without flushing the persistence context, `deleteAll(1L)` will
    // throw an exception.
    Persistence.flush();
    return cvr;
  }

  @Test()
  public void reauditTest() {
    // let's pretend the current max revision is 2, which we'll inc, and then
    // assertEquals
    Long curMax = 2L;
    CastVoteRecord cvr = noisyCVRSetup(2);
    cvr.setToReaudited(curMax + 1L);
    Long result = CastVoteRecordQueries.forceUpdate(cvr);
    assertEquals((long) 1L, (long)result);
    Long maxRev = CastVoteRecordQueries.maxRevision(cvr);
    assertEquals((long)maxRev, (long)curMax + 1L);
  }

  @Test()
  public void deleteAllTest() {
    noisyCVRSetup(1);
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
