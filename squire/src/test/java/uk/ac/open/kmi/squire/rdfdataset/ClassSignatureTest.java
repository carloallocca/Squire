package uk.ac.open.kmi.squire.rdfdataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.sparql.vocabulary.FOAF;
import org.junit.Test;

import uk.ac.open.kmi.squire.utils.TreeNodez;

public class ClassSignatureTest {

	private String clazz = FOAF.Person.getURI();
	private String prop1 = FOAF.knows.getURI();
	private String prop2 = FOAF.name.getURI();

	@Test
	public void testPath() throws Exception {
		String input = "<" + prop1 + ">/<" + prop2 + ">";
		ClassSignature sign = new ClassSignature(clazz);
		assertEquals(clazz, sign.getOwlClass());
		sign.addPath(input);
		assertTrue(sign.listPathOrigins().contains(prop1));
		boolean found = false;
		for (TreeNodez<String> nod : sign.getPath(prop1).getChildren())
			if (prop2.equals(nod.getData())) found = true;
		assertTrue(found);
	}

	@Test
	public void testSerialize() throws Exception {
		String input = "<" + prop1 + ">/<" + prop2 + ">";
		ClassSignature sign = new ClassSignature(clazz);
		sign.addPath(input);
		sign.jsonifyPaths();
	}

}
