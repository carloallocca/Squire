package uk.ac.open.kmi.squire;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonException;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.run.IndexingTask;
import uk.ac.open.kmi.squire.run.RecommendationTask;

public class Squire {

	/**
	 * Command-line parser for standalone application.
	 * 
	 * @author Alessandro Adamou <alexdma@apache.org>
	 * 
	 */
	private static class Cli {

		private String[] args = null;
		private Options options = new Options();

		public Cli(String[] args) {
			this.args = args;
			options.addOption("d", "datafile", true, "Use a JSON data file to configure recommendations.");
			options.addOption("f", "force", false, "Force reindexing (for 'index' task).");
			options.addOption("h", "help", false, "Show this help.");
			options.addOption("l", "log", false, "Log computation to file, in addition to final reporting.");
			options.addOption("s", "source", true, "SPARQL endpoint where the query is satisfiable"
					+ " (required for 'recommend' unless option 'd/datafile' is specified).");
			options.addOption("t", "target", true, "Target SPARQL endpoint to send the reformulated query"
					+ " (required for 'recommend' unless option 'd/datafile' is specified').");
			options.addOption("S", "strict", false,
					"Forces properties of recommended queries to be of the same type as those of the original query -"
							+ " \"Let object properties be object properties\".");
		}

		/**
		 * Parses command line arguments and acts upon them.
		 */
		public void parse() {
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = null;
			try {
				cmd = parser.parse(options, args);
				if (cmd.hasOption('h'))
					help();
				String[] args = cmd.getArgs();
				if (args.length > 0) {
					if ("index".equals(args[0])) {
						if (args.length == 1) {
							log.error("Task 'index' requires one or more SPARQL endpoints, none given.");
							help(1);
						}
						task = "index";
						forceIndexing = cmd.hasOption('f');
						targetEndpoints = new String[args.length - 1];
						for (int i = 1; i < args.length; i++)
							targetEndpoints[i - 1] = args[i];
					} else if ("recommend".equals(args[0])) {
						if (cmd.hasOption('S'))
							strict = true;
						if (cmd.hasOption('l'))
							tracing = true;
						if (cmd.hasOption('d')) {
							log.info("Using datafile at {}", cmd.getOptionValue('d'));
							log.info("Ignoring other recommendation parmeters such as -s and -t");
							datafile = new File(cmd.getOptionValue('d'));
						} else {
							if (args.length != 2) {
								log.error("Task 'recommend' only accepts up to one argument (a SPARQL query).");
								help(-1);
							}
							if (!(cmd.hasOption('s') && cmd.hasOption('t'))) {
								log.error(
										"Task 'recommend' requires that both -s and -t options be set as SPARQL endpoint URIs.");
								log.error("Alternatively you can put them in a JSON data file and use option -d");
								help(-1);
							}
							sourceEndpoint = cmd.getOptionValue('s');
							targetEndpoints = new String[] { cmd.getOptionValue('t') };
							query = args[1];
						}
						task = "recommend";
					} else {
						log.error("Invalid task " + args[0]);
						help(1);
					}
				} else
					help();
			} catch (UnrecognizedOptionException e) {
				System.err.println(e.getMessage());
				help();
			} catch (ParseException e) {
				log.error("Failed to parse command line properties", e);
				help();

			}
		}

		private void help() {
			help(0);
		}

		/**
		 * Prints help.
		 */
		private void help(int returncode) {
			String syntax = "java [java-opts] -jar [this-jarfile] [options] [TASK] [args]";
			String header = "Options:";
			String footer = "TASK can be one of index|recommend."
					+ "\nIf TASK is 'index', then args is a space-separated list of SPARQL endpoints to be indexed."
					+ "\nIf TASK is 'recommend', then the only argument is a SPARQL query and it must be accompanied with"
					+ " options s/source and t/target, unless d/datafile is specified, which overrides all three.";
			new HelpFormatter().printHelp(syntax, header, options, footer);
			System.exit(returncode);
		}

	}

	private static File datafile;

	private static boolean forceIndexing = false, strict = false, tracing = false;

	private static Logger log = LoggerFactory.getLogger(Squire.class);

	private static String query;

	/**
	 * Only the SPARQL endpoint set by command line using -s
	 */
	private static String sourceEndpoint;

	/**
	 * Only the SPARQL endpoints set by command line (using -t for recommend or
	 * passed as arguments for index).
	 */
	private static String[] targetEndpoints = new String[0];

	private static String task = null;

	public static void main(String[] args) {
		new Cli(args).parse();
		log.debug("Task: {}", task);
		if ("index".equals(task)) {
			log.info("Trying to index {} SPARQL endpoints", targetEndpoints.length);
			new IndexingTask(new HashSet<>(Arrays.asList(targetEndpoints)), forceIndexing).execute();
		} else if ("recommend".equals(task)) {
			if (datafile != null) {
				try {
					JsonObject json = JSON.parse(new FileInputStream(datafile));
					log.info("Found {} groups : {}", json.keys().size(), json.keys());
					for (String key : json.keys())
						unpackAndRun(json, key);
				} catch (FileNotFoundException e) {
					log.error("Data file {} not found", datafile.getAbsolutePath());
					System.exit(-2);
				} catch (JsonException ex) {
					log.error("Data file does not appear to be valid JSON", ex);
					System.exit(-2);
				}
			} else
				new RecommendationTask(query, sourceEndpoint, new HashSet<>(Arrays.asList(targetEndpoints)), null,
						strict, tracing).execute();
		}
		log.info("All done.");
		System.exit(0);
	}

	private static void unpackAndRun(JsonObject json, String key) {
		log.info("Scanning for queries at key '{}'...", key);
		JsonObject group = json.get(key).getAsObject();
		if (!group.hasKey("source"))
			throw new RuntimeException("Key '" + key + "' does not seem to have a value for 'source'");
		if (!group.hasKey("target"))
			throw new RuntimeException("Key '" + key + "' does not seem to have a value for 'target'");
		String endpoint_src = group.get("source").getAsString().value();
		String endpoint_tgt = group.get("target").getAsString().value();
		if (!group.hasKey("queries"))
			log.warn("No queries found for group {}, nothing to do.", key);
		int nQ = 0;
		for (JsonValue v : group.get("queries").getAsArray()) {
			String q = null;
			if (v.isObject()) {
				JsonObject o = v.getAsObject();
				if (o.hasKey("original"))
					q = o.get("original").getAsString().value();
				else if (o.hasKey("query"))
					q = o.get("query").getAsString().value();
			} else if (v.isString())
				q = v.getAsString().value();
			else
				log.error("Field 'queries' expects an array of either strings or"
						+ " objects containing the 'original' or 'query' field.");
			if (q != null)
				new RecommendationTask(q, endpoint_src, Collections.singleton(endpoint_tgt), key + "_q" + (++nQ),
						strict, tracing).execute();
		}

	}

}
