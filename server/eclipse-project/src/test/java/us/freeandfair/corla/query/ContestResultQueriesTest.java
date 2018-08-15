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
import us.freeandfair.corla.model.ContestResult;
import us.freeandfair.corla.model.Contest;
import us.freeandfair.corla.model.County;

public class ContestResultQueriesTest {


  @BeforeTest(enabled=false)
  public void setUp() {
    Setup.setProperties();
    Persistence.beginTransaction();
  }

  @AfterTest(enabled=false)
  public void tearDown() {
    try {
    Persistence.rollbackTransaction();
    } catch (Exception e) {
    }
  }

  @Test(enabled=false)
  public void forCountyTest() {
    County county = new County("cba", 321L);
    Persistence.saveOrUpdate(county);
    Assert.assertEquals((Long)county.version(),(Long) 0L);
    Contest contest = new Contest("fed",
                                  county,
                                  "desc",
                                  new ArrayList<>(),
                                  1,
                                  1,
                                  0);
    Persistence.saveOrUpdate(contest);
    Assert.assertEquals((Long)contest.version(),(Long) 0L);

    ContestResult crSetup = new ContestResult(contest);
    Persistence.saveOrUpdate(crSetup);
    Assert.assertEquals((Long)crSetup.version(),(Long) 0L);
    ContestResult cr = ContestResultQueries.withContestName("fed");
    Assert.assertEquals((Long)cr.version(),(Long) 0L);
    Assert.assertEquals(cr.getContestName(), "fed");
  }
}
