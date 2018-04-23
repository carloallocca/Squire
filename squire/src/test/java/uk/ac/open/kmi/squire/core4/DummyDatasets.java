package uk.ac.open.kmi.squire.core4;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import uk.ac.open.kmi.squire.evaluation.TestGoldStandard;
import uk.ac.open.kmi.squire.rdfdataset.ClassSignature;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.InMemoryRdfDataset;
import uk.ac.open.kmi.squire.rdfdataset.WritableRdfDataset;

/**
 * Utility methods to initialize the fake {@link IRDFDataset}s used in the unit
 * tests.
 * 
 * @author alessandro
 *
 */
public class DummyDatasets {

	public static Map<String, WritableRdfDataset> populate(String path) {
		final Map<String, WritableRdfDataset> datasets = new HashMap<>();
		JsonObject testdata = JSON.parse(TestGoldStandard.class.getResourceAsStream(path));
		if (!testdata.isObject())
			throw new IllegalArgumentException("Could not parse a JSON object from resource at " + path);
		for (String endpoint : testdata.keys()) {
			if (!datasets.containsKey(endpoint)) datasets.put(endpoint, new InMemoryRdfDataset());
			WritableRdfDataset ds = datasets.get(endpoint);
			JsonObject jds = testdata.get(endpoint).getAsObject();
			for (String clazz : jds.keys()) {
				if (!ds.getClassSignatures().containsKey(clazz))
					ds.getClassSignatures().put(clazz, new ClassSignature(clazz));
				ClassSignature sign = ds.getClassSignatures().get(clazz);
				JsonObject jClazz = jds.get(clazz).getAsObject();
				if (jClazz.hasKey("dps"))
					for (Iterator<JsonValue> it = jClazz.get("dps").getAsArray().iterator(); it.hasNext();) {
					String p = it.next().getAsString().value();
					sign.addProperty(p);
					ds.addDatatypeProperty(p);
					}
				if (jClazz.hasKey("ops"))
					for (Iterator<JsonValue> it = jClazz.get("ops").getAsArray().iterator(); it.hasNext();) {
					String p = it.next().getAsString().value();
					sign.addProperty(p);
					ds.addObjectProperty(p);
					}
			}
			ds.rebuildPropertyCoOccurrenceMap();
		}
		return datasets;
	}

}
