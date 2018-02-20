package uk.ac.open.kmi.squire.rdfdataset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.json.JsonObject;

import uk.ac.open.kmi.squire.utils.SparqlUtils;
import uk.ac.open.kmi.squire.utils.TreeNodez;

/**
 * Note that This implementation instantiates a different TreeNode for each
 * usage of the same property.
 * 
 * @author alessandro
 *
 */
public class ClassSignature {

	private String clazz;

	private Map<String, TreeNodez<String>> origins;

	public ClassSignature(String clazz) {
		this.clazz = clazz;
		this.origins = new HashMap<>();
	}

	/**
	 * Expects syntax as in SPARQL paths:
	 * <http://prop.er.ty/1>/<http://prop.er.ty/2> etc...
	 * 
	 * @param path
	 */
	public void addPath(String path) {
		Pattern pattern = Pattern.compile("<([^<>]+)>");
		Matcher matcher = pattern.matcher(path);
		if (matcher.find()) {
			String first = matcher.group(1);
			TreeNodez<String> firstNode;
			if (origins.containsKey(first)) firstNode = getPath(first);
			else {
				firstNode = new TreeNodez<String>(first);
				origins.put(first, firstNode);
			}
			TreeNodez<String> nextNode = firstNode;
			while (matcher.find())
				nextNode = append(matcher.group(1), nextNode);
		}
	}

	public void addProperty(String property) {
		if (!hasProperty(property)) origins.put(property, new TreeNodez<String>(property));
	}

	public String getOwlClass() {
		return clazz;
	}

	public TreeNodez<String> getPath(String property) {
		return origins.get(property);
	}

	public boolean hasProperty(String property) {
		return origins.containsKey(property);
	}

	public Set<String> listPathOrigins() {
		return origins.keySet();
	}

	/**
	 * It serializes only the paths to a JSON structure such as:
	 * 
	 * <pre>
	 * { 
	 *   p1 : { 
	 *     p1_1 : { ... } , 
	 *     p1_2 : { ... } 
	 *   },
	 *   p2 : { 
	 *     ...
	 *   } 
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	public JsonObject jsonifyPaths() {
		JsonObject main = new JsonObject();
		for (String origin : listPathOrigins())
			main.put(origin, jsonifyPath(getPath(origin)));
		return main;
	}

	private TreeNodez<String> append(String property, TreeNodez<String> path) {
		if (!SparqlUtils.isValidUri(property))
			throw new IllegalArgumentException("Property must respect URI syntax. No qulified names or literals.");
		for (TreeNodez<String> child : path.getChildren())
			if (property.equals(child.getData())) return child;
		TreeNodez<String> child = new TreeNodez<>(property);
		path.addChild(child);
		return child;
	}

	protected JsonObject jsonifyPath(TreeNodez<String> node) {
		JsonObject json = new JsonObject();
		for (TreeNodez<String> child : node.getChildren())
			json.put(child.getData(), jsonifyPath(child));
		return json;
	}

	protected TreeNodez<String> search(String data, TreeNodez<String> node) {
		if (node == null) return null;
		if (node.getData().equals(data)) return node;
		for (TreeNodez<String> child : node.getChildren()) {
			TreeNodez<String> foundNode = search(data, child);
			if (foundNode != null) return foundNode;
		}
		return null;
	}

}
