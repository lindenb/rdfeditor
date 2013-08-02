package com.github.lindenb.rdfeditor.swing.list;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

@SuppressWarnings({ "rawtypes", "serial" })
public class PropertiesListModel
	extends AbstractListModel
	implements ComboBoxModel,ListModel
	{
	//private Model schema;
	//private Resource ontClass;
	private Vector<Property> properties=new Vector<Property>();
	private Object selected;
	public PropertiesListModel(Model schema,Resource ontClass)
		{	
		//this.schema=schema;
		//this.ontClass=ontClass;
		ExtendedIterator<Resource> iter=null;
		try
			{
			iter=schema.listResourcesWithProperty(RDFS.domain, ontClass);
			while(iter.hasNext())
				{	
				properties.add(ResourceFactory.createProperty(iter.next().getURI()));
				}
			}
		finally
			{
			if(iter!=null) iter.close();
			}
		}

	@Override
	public Object getElementAt(int idx) {
		return properties.get(idx);
	}
	
	@Override
	public int getSize() {
		return properties.size();
		}
	
	@Override
	public Object getSelectedItem() {
		return selected;
	}

	@Override
	public void setSelectedItem(Object selected) {
		this.selected=selected;
		
	}


	}
