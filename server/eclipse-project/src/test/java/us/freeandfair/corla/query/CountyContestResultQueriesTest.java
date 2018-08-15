package us.freeandfair.corla.query;

import java.util.List;
import java.util.ArrayList;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.Assert;

import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.query.Setup;
import us.freeandfair.corla.model.CountyContestResult;
import us.freeandfair.corla.model.Contest;
import us.freeandfair.corla.model.County;

public class CountyContestResultQueriesTest {


  @BeforeTest
  public void setUp() {
    Setup.setProperties();
    Persistence.beginTransaction();
  }

  @AfterTest
  public void tearDown() {
    try {
      Persistence.rollbackTransaction();
    } catch (Exception e) {
    }
  }

  @Test()
  public void forCountyTest() {
    County county = new County("abc", 123L);
    Persistence.saveOrUpdate(county);
    Assert.assertEquals((Long)county.version(),(Long) 0L);
    Contest contest = new Contest("def",
                                  county,
                                  "desc",
                                  new ArrayList<>(),
                                  1,
                                  1,
                                  0);
    Persistence.saveOrUpdate(contest);
    Assert.assertEquals((Long)contest.version(),(Long) 0L);
    CountyContestResult ccr = new CountyContestResult(county, contest);
    Persistence.saveOrUpdate(ccr);
    Assert.assertEquals((Long)ccr.version(),(Long) 0L);
    List<CountyContestResult> ccrs = CountyContestResultQueries.withContestName("def");
    Assert.assertEquals((Long)ccr.version(),(Long) 0L);
    Assert.assertEquals(ccrs.size(), 1);
  }
}
