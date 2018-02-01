package uk.ac.open.kmi.squire.rdfdataset;

import org.apache.jena.ontology.OntResource;

public class UnsupportedResourceTypeException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 2738371612027059532L;

    private Class<OntResource> resourceType;

    public UnsupportedResourceTypeException(Class<OntResource> resourceType) {
        this.setResourceType(resourceType);
    }

    public UnsupportedResourceTypeException(Class<OntResource> resourceType, String message) {
        super(message);
        this.setResourceType(resourceType);
    }

    public Class<OntResource> getResourceType() {
        return resourceType;
    }

    protected void setResourceType(Class<OntResource> resourceType) {
        this.resourceType = resourceType;
    }

}
