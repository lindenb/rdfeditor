package com.github.lindenb.rdfeditor.swing.table;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@SuppressWarnings("serial")
public class RangeTableDomain extends AbstractGenericTableModel<Resource>
	{
	public RangeTableDomain(Model schema)
		{
		List<Resource> v=new Vector<Resource>();
		Iterator<RDFDatatype> iter= TypeMapper.getInstance().listTypes();
		while(iter.hasNext())
			{
			v.add(ResourceFactory.createResource(iter.next().getURI()));
			}
		
		if(v.isEmpty())
			{
			LOG.info("no dataType found in schema");
			}
		
		ExtendedIterator<Resource> r=null;
		try {
			r=schema.listSubjectsWithProperty(RDF.type, RDFS.Class);
			while(r.hasNext())
				{
				v.add(r.next());
				}
			}
		finally
			{
			if(r!=null) r.close();
			}
		if(v.isEmpty())
			{
			LOG.info("nothing found in range rdf:type/RDFS:class");
			}
		LOG.info("rows: "+v);
		super.setRows(v);
		}

	

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Class<?> getColumnClass(int col)
		{
		switch (col)
			{
			case 0: return Resource.class;
			case 1: return Boolean.class;
			default: return Object.class;
			}
		}

	@Override
	public String getColumnName(int col) {
		switch (col)
			{
			case 0: return "URI";
			case 1: return "DataType";
			default: return "$"+(col+1);
			}
		}

	@Override
	public Object getValueOf(Resource o, int col)
		{
		switch (col)
			{
			case 0: return o;
			case 1: return TypeMapper.getInstance().getTypeByName(o.getURI())!=null;
			default: return null;
			}
		}

}
