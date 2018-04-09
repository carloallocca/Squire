package uk.ac.open.kmi.squire.operation;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author carloallocca
 */
public class InstantiateTemplateVar implements Operation<Query> {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final String entity;
	private final Query q;
	private final Var varTemplate;

	public InstantiateTemplateVar(Query q, Var varTemplate, String entity) {
		this.q = q;
		this.varTemplate = varTemplate;
		this.entity = entity;
	}

	@Override
	public Query apply() {
		log.debug("Instantiating template variable {}", this.varTemplate);
		log.debug(" - instantiation : {}", this.entity);
		throw new NotImplementedException("NIY");
	}

	@Override
	public Object[] getOperands() {
		return new Object[] { q, varTemplate, entity };
	}

}
