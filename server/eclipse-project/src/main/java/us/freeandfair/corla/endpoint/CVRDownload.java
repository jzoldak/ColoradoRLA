/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Jul 27, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.endpoint;

import static us.freeandfair.corla.asm.ASMEvent.CountyDashboardEvent.COUNTY_SKIP_EVENT;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import com.google.gson.stream.JsonWriter;

import spark.Request;
import spark.Response;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.asm.ASMEvent;
import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.CastVoteRecord.RecordType;
import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.query.CastVoteRecordQueries;
import us.freeandfair.corla.util.SparkHelper;

/**
 * The ballot manifest download endpoint.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class CVRDownload extends AbstractCountyDashboardEndpoint {
  /**
   * {@inheritDoc}
   */
  @Override
  public EndpointType endpointType() {
    return EndpointType.GET;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String endpointName() {
    return "/cvr";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ASMEvent endpointEvent() {
    return COUNTY_SKIP_EVENT;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  // necessary to break out of the lambda expression in case of IOException
  @SuppressWarnings("PMD.ExceptionAsFlowControl")
  public String endpoint(final Request the_request, final Response the_response) {
    try {
      final OutputStream os = SparkHelper.getRaw(the_response).getOutputStream();
      final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
      final JsonWriter jw = new JsonWriter(bw);
      Persistence.beginTransaction();
      jw.beginArray();
      final Stream<CastVoteRecord> matches = 
          CastVoteRecordQueries.getMatching(RecordType.UPLOADED);
      matches.forEach((the_cvr) -> {
        try {
          jw.jsonValue(Main.GSON.toJson(the_cvr));
          Persistence.currentSession().evict(the_cvr);
        } catch (final IOException e) {
          throw new UncheckedIOException(e);
        } 
      });
      jw.endArray();
      jw.flush();
      jw.close();
      try {
        Persistence.commitTransaction(); 
      } catch (final RollbackException e) {
        Persistence.rollbackTransaction();
      } 
      ok(the_response);
    } catch (final UncheckedIOException | IOException | PersistenceException e) {
      serverError(the_response, "Unable to stream response");
    }
    return my_endpoint_result;
  }
}
