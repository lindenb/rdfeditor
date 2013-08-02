package com.github.lindenb.rdfeditor.swing.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


import com.github.lindenb.rdfeditor.ExtSchema;
import com.github.lindenb.rdfeditor.rdf.PropertyAndObject;
import com.github.lindenb.rdfeditor.rdf.SchemaAndModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ResourcesInRangeTableModel
	extends AbstractGenericTableModel<PropertyAndObject>
	implements SchemaAndModel
	{
	private static final long serialVersionUID = 1L;
	private List<Property> columns=new Vector<Property>();
	private SchemaAndModel schemaAndModel;
	
	
	public ResourcesInRangeTableModel(
			SchemaAndModel schemaAndModel,
			Resource subject
			)
		{
		this.schemaAndModel=schemaAndModel;
		List<PropertyAndObject> rows=new ArrayList<PropertyAndObject>();
		Set<Resource> rdfTypes=new HashSet<Resource>();
		ExtendedIterator<Statement> iter=null;
		
		try
			{
			iter=getRDFDataStore().listStatements(null,null,(RDFNode)subject).filterKeep(new Filter<Statement>()
				{
				@Override
				public boolean accept(Statement stmt)
					{
					if(!getRDFDataStore().contains(stmt.getSubject(), RDF.type)) return false;
					return true;
					}
				});
			while(iter.hasNext())
				{
				Statement stmt=iter.next();
				Set<Resource> rdfTypes2=new HashSet<Resource>();
				//search RDF type
				ExtendedIterator<Statement> iter2=null;
				try
					{
					iter2=getRDFSchema().listStatements(stmt.getSubject(), RDF.type,(RDFNode)null);
					while(iter2.hasNext())
						{
						Statement stmt2=iter2.next();
						if(!stmt2.getObject().isResource()) continue;
						if(!getRDFSchema().contains(stmt.getObject().asResource(),RDF.type,RDFS.Class)) continue;
						rdfTypes2.add(stmt.getObject().asResource());
						}
					}
				finally
					{
					if(iter2!=null) iter2.close();
					}
				if(rdfTypes2.isEmpty()) continue;
				rdfTypes.addAll(rdfTypes2);
				
				rows.add(new PropertyAndObject(stmt.getPredicate(),stmt.getSubject()));
				}			
			}
		finally
			{
			if(iter!=null) iter.close();
			iter=null;
			}
		
		Set<Property> properties=new HashSet<Property>();
		for(Resource ontClass:rdfTypes)
			{
			ExtendedIterator<Resource> iterR=null;
			try
				{
				iterR=getRDFSchema().listResourcesWithProperty(RDFS.domain,ontClass).filterKeep(new Filter<Resource>()
					{
					@Override
					public boolean accept(Resource subject)
						{
						return subject.hasLiteral(ExtSchema.inListing, true);
						}
					});
				while(iterR.hasNext())
					{
					Resource r=iterR.next();
					properties.add(ResourceFactory.createProperty(r.getURI()));
					}
			
				}
			finally
				{
				if(iterR!=null) iterR.close();
				iterR=null;
				}			
			}
		super.setRows(rows);
		this.columns=new Vector<Property>(properties);
		}
	
	/*
	public RDFDatatype getColumnDataType(int col)
		{
		StmtIterator iter=null;
		try
			{
			iter=schema.listStatements(columns.get(col), RDFS.range, (RDFNode)null);
			while(iter.hasNext())
				{
				Statement stmt=iter.nextStatement();
				if(!stmt.getObject().isResource()) continue;
				String rangeURI=stmt.getObject().asResource().getURI();
				RDFDatatype dt=this.typeMapper.getTypeByName(rangeURI);
				if(dt!=null) return dt;
				}	
			return null;
			}
		finally
			{
			if(iter!=null) iter.close();
			}
		}*/
	
		
	@Override
	public Class<?> getColumnClass(int col)
		{
		return RDFNode.class;
		}
	
	@Override
	public String getColumnName(int col)
		{
		switch(col)
			{
			case 0: return "Predicate";
			case 1: return "Type";
			case 2: return "URI";
			default: break;
			}
		col-=3;
		return this.getRDFSchema().shortForm(this.columns.get(col).getURI());
		}

	@Override
	public int getColumnCount() {
		return 3+columns.size();
		}
	
	public Object getValueOf(final PropertyAndObject rsrc, int col)
		{
		switch(col)
			{
			case 0: return rsrc.getPredicate();
			case 1:
				{
				StmtIterator iter=null;
				try
					{
					iter=this.getRDFDataStore().listStatements( rsrc.getObject().asResource(),RDF.type,(RDFNode)null);
					while(iter.hasNext())
						{
						return iter.nextStatement().getObject();
						}	
					return null;
					}
				finally
					{
					if(iter!=null) iter.close();
					}
				}
			case 2: return rsrc.getObject();
			
			}
		col-=3;
		StmtIterator iter=null;
		try
			{
			iter=this.getRDFDataStore().listStatements( rsrc.getObject().asResource(),columns.get(col),(RDFNode)null);
			while(iter.hasNext())
				{
				return iter.nextStatement().getObject();
				}	
			return null;
			}
		finally
			{
			if(iter!=null) iter.close();
			}
		}
	
	@Override
	public Model getRDFDataStore() {
		return this.schemaAndModel.getRDFDataStore();
		}
	@Override
	public Model getRDFSchema() {
		return this.schemaAndModel.getRDFSchema();
		}
	@Override
	public void fireModelChanged() {
		this.schemaAndModel.fireModelChanged();
		}

	}
