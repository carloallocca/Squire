package uk.ac.open.kmi.squire;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.run.IndexingTask;

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
			options.addOption("q", "query", true, "Initial SPARQL query (required for 'recommend').");
			options.addOption("s", "source", true,
					"SPARQL endpoint where the query is satisfiable (required for 'recommend')..");
			options.addOption("h", "help", false, "Show this help.");
			options.addOption("t", "target", true,
					"Target SPARQl endpoint to send the reformulated query (required for 'recommend')..");
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
					+ "\nIf TASK is 'index', then args is a space-separated list of SPARQL endpoints to be indexed.";
			new HelpFormatter().printHelp(syntax, header, options, footer);
			System.exit(returncode);
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
						targetEndpoints = new String[args.length - 1];
						for (int i = 1; i < args.length; i++)
							targetEndpoints[i - 1] = args[i];
					} else if ("recommend".equals(args[0]))
						task = "recommend";
					else {
						log.error("Invalid task " + args[0]);
						help(1);
					}
				} else
					help();
			} catch (UnrecognizedOptionException e) {
				System.err.println(e.getMessage());
				help();
			} catch (ParseException e) {
				log.error("Failed to parse comand line properties", e);
				help();

			}
		}

	}

	private static Logger log = LoggerFactory.getLogger(Squire.class);

	private static String task = null;

	private static String[] targetEndpoints = new String[0];

	public static void main(String[] args) {
		new Cli(args).parse();
		log.debug("Task: {}", task);
		if ("index".equals(task)) {
			log.info("Trying to index {} SPARQL endpoints", targetEndpoints.length);
			new IndexingTask(new HashSet<>(Arrays.asList(targetEndpoints))).run();
		} else if ("recommend".equals(task)) {
			log.error("Sorry, not implemented yet through command-line.");
			System.exit(-1);
		}
	}

}
