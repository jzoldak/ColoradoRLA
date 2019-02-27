package us.freeandfair.corla.csv;


import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/** to render a CSV as an http response  **/
public class CSVWriter {

  /** no instantiation **/
  private CSVWriter () {}

  /**
   * write rows/records to an output stream, like maybe a Spark response output
   * stream
   **/
  public static void write(final OutputStream os,
                           final List<List<String>> rows) throws IOException {
    Writer writer = new BufferedWriter(new OutputStreamWriter(os));
    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

    for (List<String> row: rows) {
      csvPrinter.printRecord(row);
    }
  }
}
