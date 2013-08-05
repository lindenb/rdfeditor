package com.github.lindenb.rdfeditor.swing.table.model;

import com.github.lindenb.rdfeditor.rdf.PropertyAndObject;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

@SuppressWarnings("serial")
public class InDomainTableModel  extends AbstractGenericTableModel<PropertyAndObject>
	{	
	public InDomainTableModel()
		{
		}
	@Override
	public Object getValueOf(PropertyAndObject o, int col) {
		if(o==null) return null;
		switch(col)
			{
			case 0: return o.getPredicate();
			case 1: return o.getObject();
			}
		return null;
		}	
	@Override
	public String getColumnName(int col)
		{
		switch(col)
			{
			case 0: return "Property";
			case 1: return "Value";
			}
		return null;
		}
	@Override
	public int getColumnCount()
		{
		return 2;
		}
	@Override
	public Class<?> getColumnClass(int col)
		{
		switch(col)
			{
			case 0: return Property.class;
			case 1: return RDFNode.class;
			}
		return null;
		}
	}
