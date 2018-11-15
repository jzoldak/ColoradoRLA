package us.freeandfair.corla.cli;

import java.util.stream.Collectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

// import org.apache.commons.cli.CommandLineParser;
// import org.apache.commons.cli.DefaultParser;
// import org.apache.commons.cli.CommandLine;
// import org.apache.commons.cli.Options;

import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.controller.ContestCounter;

public class Main {

  private Main() {}

  public static void load(Properties properties, java.io.InputStream fileStream)
  throws java.io.IOException {
    try {
      properties.load(fileStream);
    } catch (final IOException e) {
      // figure this out with a cli parser library
      // throw e;
    }
  }

  public static Properties loadProperties(String path)
    throws java.io.IOException, java.io.FileNotFoundException {
    final Properties properties = new Properties();
    load(properties, ClassLoader.getSystemResourceAsStream("us/freeandfair/corla/default.properties"));
    if (null != path) {
      try{
        final File file = new File(path);
        load(properties, new FileInputStream(file));
      } catch (final java.io.FileNotFoundException e) {
        // figure this out with a cli parser library
        // throw e;
      }
    }
    return properties;
  }

  public static void count(java.io.PrintStream out) {
    Persistence.beginTransaction();
    Boolean force = false;
    ContestCounter.countAllContests(force).stream()
      .map(Persistence::persist)
      .map(cr -> {out.println(cr); return cr;})
      .collect(Collectors.toList());
    Persistence.commitTransaction();
    out.println("counted all contests");
  }

  public static void handle(final String propPath, final String subcommand)
    throws java.io.IOException, java.io.FileNotFoundException {
    Persistence.setProperties(loadProperties(propPath));
    switch (subcommand) {
    case "count":
      count(System.out);
      break;
    default:
      System.out.println("help text");
    }
  }

  public static void main(final String... args) {
    if (args.length < 2) {
      System.out.println("help text");
      System.exit(1);
    }
    // idk about all this, I think we need a cli parser
    final String propPath = args[0];
    final String subcommand = args[1];
    try {
      handle(propPath, subcommand);
    } catch (java.io.FileNotFoundException e) {
      System.out.println(" file not found for args: " + args);
    } catch (java.io.IOException e) {
      System.out.println(" idk what happened " + e.getMessage());
    }
  }
}
