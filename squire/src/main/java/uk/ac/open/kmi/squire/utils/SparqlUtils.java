package uk.ac.open.kmi.squire.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SparqlUtils {

	private static JsonParser jsonParser = new JsonParser();

	private static Logger log = LoggerFactory.getLogger(SparqlUtils.class);

	public static List<String> getValuesFromSparqlJson(String sparqlResultJson, String variable) {
		List<String> output = new ArrayList<>();
		JsonArray results = jsonParser.parse(sparqlResultJson).getAsJsonObject().get("results").getAsJsonObject()
				.getAsJsonArray("bindings");
		for (JsonElement result1 : results) {
			JsonObject jClazz = result1.getAsJsonObject().getAsJsonObject(variable);
			String value = jClazz.get("value").getAsString();
			try {
				new URI(value); // To test the syntax
				output.add(value);
			} catch (URISyntaxException ex) {
				log.error("Bad URI synax for string '{}'", value);
			}
		}
		return output;
	}

}
