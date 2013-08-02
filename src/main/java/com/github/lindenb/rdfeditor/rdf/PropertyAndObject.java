package com.github.lindenb.rdfeditor.rdf;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class PropertyAndObject
	{
	private Property predicate;
	private RDFNode object;
	public PropertyAndObject()
		{
		this(null,null);
		}
	public PropertyAndObject(Property predicate,RDFNode object)
		{
		this.predicate=predicate;
		this.object=object;
		}
	public Property getPredicate() {
		return predicate;
	}
	public void setPredicate(Property predicate) {
		this.predicate = predicate;
	}
	public RDFNode getObject() {
		return object;
	}
	public void setObject(RDFNode object) {
		this.object = object;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result
				+ ((predicate == null) ? 0 : predicate.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyAndObject other = (PropertyAndObject) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!predicate.equals(other.predicate))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "PropertyAndObject [predicate=" + predicate + ", object="
				+ object + "]";
	}
	
	
	}
