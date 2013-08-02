package com.github.lindenb.rdfeditor.rdf;

import com.hp.hpl.jena.rdf.model.Model;

public interface SchemaAndModel {
public Model getRDFSchema();
public Model getRDFDataStore();
}
