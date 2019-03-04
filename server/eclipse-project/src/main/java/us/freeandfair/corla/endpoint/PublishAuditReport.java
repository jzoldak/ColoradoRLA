/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Aug 12, 2017
 * @copyright 2017 Colorado Department of State
 * @license SPDX-License-Identifier: AGPL-3.0-or-later
 * @creator Joseph R. Kiniry <kiniry@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.endpoint;

import spark.Request;
import spark.Response;

import java.util.List;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.cxf.attachment.Rfc5987Util;

import us.freeandfair.corla.asm.ASMEvent;
import us.freeandfair.corla.controller.AuditReport;
import us.freeandfair.corla.csv.CSVWriter;
import us.freeandfair.corla.util.SparkHelper;

/**
 * Download all of the data relevant to public auditing of a RLA.
 * 
 * @author Joseph R. Kiniry <kiniry@freeandfair.us>
 * @version 1.0.0
 */
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class PublishAuditReport extends AbstractDoSDashboardEndpoint {
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
    return "/publish-audit-report";
  }

  /**
   * @return STATE authorization is necessary for this endpoint.
   */
  public AuthorizationType requiredAuthorization() {
    return AuthorizationType.STATE;
  }

  /**
   * Download all of the data relevant to public auditing of a RLA.
   */
  @Override
  public String endpointBody(final Request request,
                        final Response response)  {
    final String contestName = request.queryParams("contestName");

    try {

      final OutputStream os = SparkHelper.getRaw(response).getOutputStream();
      final String fileName = Rfc5987Util.encode(contestName.replace(" ", "+"), "UTF-8") + "-audit-report.csv";
      final List<List<String>> rows = AuditReport.getRowsFor(contestName);

      // these need to come before the OutputStream is written or they will be ignored
      response.header("Content-Type", "text/csv");
      response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\""  );
      ok(response);

      CSVWriter.write(os, rows);
      os.flush();
      // this is effectively the end of the response processing, so it needs to come last
      os.close();

    } catch (final IOException e) {
      serverError(response, "Unable to stream response");
    }

    return my_endpoint_result.get();
  }
}
