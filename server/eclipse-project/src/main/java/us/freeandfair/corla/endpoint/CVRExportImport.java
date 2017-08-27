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

import static us.freeandfair.corla.asm.ASMEvent.CountyDashboardEvent.UPLOAD_CVRS_EVENT;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.Instant;

import javax.persistence.PersistenceException;

import com.google.gson.JsonSyntaxException;

import spark.Request;
import spark.Response;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.asm.ASMEvent;
import us.freeandfair.corla.csv.DominionCVRExportParser;
import us.freeandfair.corla.model.CastVoteRecord.RecordType;
import us.freeandfair.corla.model.County;
import us.freeandfair.corla.model.CountyDashboard;
import us.freeandfair.corla.model.UploadedFile;
import us.freeandfair.corla.model.UploadedFile.FileStatus;
import us.freeandfair.corla.model.UploadedFile.HashStatus;
import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.query.CastVoteRecordQueries;

/**
 * The "CVR export import" endpoint.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
@SuppressWarnings({"PMD.AtLeastOneConstructor", "PMD.ExcessiveImports"})
public class CVRExportImport extends AbstractCountyDashboardEndpoint {
  /**
   * {@inheritDoc}
   */
  @Override
  public EndpointType endpointType() {
    return EndpointType.POST;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String endpointName() {
    return "/import-cvr-export";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ASMEvent endpointEvent() {
    return UPLOAD_CVRS_EVENT;
  }

  /**
   * Updates the appropriate county dashboard to reflect a new 
   * CVR export upload.
   * @param the_response The response object (for error reporting).
   * @param the_county_id The county ID.
   * @param the_timestamp The timestamp.
   */
  private void updateCountyDashboard(final Response the_response, 
                                     final Long the_county_id, 
                                     final Instant the_timestamp) {
    final CountyDashboard cdb = Persistence.getByID(the_county_id, CountyDashboard.class);
    if (cdb == null) {
      serverError(the_response, "could not locate county dashboard");
    } else {
      cdb.setCVRUploadTimestamp(the_timestamp);
      try {
        Persistence.saveOrUpdate(cdb);
      } catch (final PersistenceException e) {
        serverError(the_response, "could not update county dashboard");
      }
    }
  }
  
  /**
   * Parses an uploaded CVR export and attempts to persist it to the database.
   * 
   * @param the_response The response (for error reporting).
   * @param the_file The uploaded file.
   */
  // the CSV parser can throw arbitrary runtime exceptions, which we must catch
  @SuppressWarnings({"PMD.AvoidCatchingGenericException"})
  private void parseFile(final Response the_response, final UploadedFile the_file) {  
    try (InputStream bmi_is = the_file.file().getBinaryStream()) {
      final InputStreamReader bmi_isr = new InputStreamReader(bmi_is, "UTF-8");
      final DominionCVRExportParser parser = 
          new DominionCVRExportParser(bmi_isr, 
                                      Persistence.getByID(the_file.countyID(), County.class));
      CastVoteRecordQueries.deleteMatching(the_file.countyID(), RecordType.UPLOADED);
      if (parser.parse()) {
        Main.LOGGER.info(parser.recordCount().getAsInt() + " CVRs parsed from file " + 
                         the_file.id());
//        final OptionalLong count = 
//            CastVoteRecordQueries.countMatching(RecordType.UPLOADED);
//        if (count.isPresent()) {
//          Main.LOGGER.info(count.getAsLong() + " uploaded CVRs in storage");
//        }
        updateCountyDashboard(the_response, the_file.countyID(), the_file.timestamp());
        the_file.setStatus(FileStatus.IMPORTED_AS_CVR_EXPORT);
        Persistence.saveOrUpdate(the_file);
        okJSON(the_response, Main.GSON.toJson(the_file));
      } else {
        Main.LOGGER.info("could not parse malformed CVR export file " + the_file.id());
        badDataContents(the_response, "malformed CVR export file " + the_file.id());
      }
    } catch (final RuntimeException | IOException e) {
      Main.LOGGER.info("could not parse malformed CVR export file " + the_file.id() + 
                       ": " + e);
      badDataContents(the_response, "malformed CVR export file " + the_file.id());
    } catch (final SQLException e) {
      Main.LOGGER.info("could not read file " + the_file.id() + 
                       " from persistent storage");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings({"PMD.ConfusingTernary"})
  public String endpoint(final Request the_request, final Response the_response) {    
    // we know we have county authorization, so let's find out which county
    final County county = Authentication.authenticatedCounty(the_request);

    if (county == null) {
      unauthorized(the_response, "unauthorized administrator for CVR export upload");
      return my_endpoint_result.get();
    }
    
    try {
      final UploadedFile file =
          Main.GSON.fromJson(the_request.body(), UploadedFile.class);
      if (file == null) {
        badDataContents(the_response, "nonexistent file");
      } else if (!file.countyID().equals(county.id())) {
        unauthorized(the_response, "county " + county.id() + " attempted to import " + 
                                   "file uploaded by county " + file.countyID());
      } else if (file.hashStatus() == HashStatus.VERIFIED) {
        parseFile(the_response, file);
      } else {
        badDataContents(the_response, "attempt to import a file without a verified hash");
      }
    } catch (final JsonSyntaxException e) {
      badDataContents(the_response, "malformed request: " + e.getMessage());
    }
    
    return my_endpoint_result.get();
  }
}
