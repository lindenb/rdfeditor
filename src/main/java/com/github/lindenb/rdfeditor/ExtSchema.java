package com.github.lindenb.rdfeditor;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class ExtSchema
	{
	public static String getURI()
		{
		return "urn:com.github.lindenb:schema";
		}
	public static final Property unique=ResourceFactory.createProperty(getURI(), "unique");
	public static final Property regex=ResourceFactory.createProperty(getURI(), "regex");
	public static final Property minLength=ResourceFactory.createProperty(getURI(), "minLength");
	public static final Property maxLength=ResourceFactory.createProperty(getURI(), "maxLength");
	public static final Property inListing=ResourceFactory.createProperty(getURI(), "in-listing");
	}
