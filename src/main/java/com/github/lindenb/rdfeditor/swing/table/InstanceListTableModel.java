package com.github.lindenb.rdfeditor.swing.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


import com.github.lindenb.rdfeditor.ExtSchema;
import com.github.lindenb.rdfeditor.rdf.SchemaAndModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class InstanceListTableModel
	extends AbstractGenericTableModel<Resource>
	implements SchemaAndModel
	{
	private static final long serialVersionUID = 1L;
	private List<Property> columns=new Vector<Property>();
	private SchemaAndModel schemaAndModel;

	public InstanceListTableModel(
			SchemaAndModel schemaAndModel,
			List<Property> columns
			)
		{
		this.columns=new Vector<Property>(columns);
		this.schemaAndModel=schemaAndModel;
		}
	
	public InstanceListTableModel(
			SchemaAndModel schemaAndModel,
			Resource ontClass
			)
		{
		this.schemaAndModel=schemaAndModel;
		
		ExtendedIterator<Resource> iter=null;
		try
			{
			iter=getRDFSchema().listResourcesWithProperty(RDFS.domain,ontClass).filterKeep(new Filter<Resource>()
				{
				@Override
				public boolean accept(Resource subject)
					{
					return subject.hasLiteral(ExtSchema.inListing, true);
					}
				});
			while(iter.hasNext())
				{
				Resource r=iter.next();
				this.columns.add(ResourceFactory.createProperty(r.getURI()));
				}
			if(this.columns.isEmpty()) System.err.println("No column foud for "+ontClass);
			}
		finally
			{
			if(iter!=null) iter.close();
			iter=null;
			}
		
		
		try
			{
			List<Resource> L=new ArrayList<Resource>();
			iter=getRDFDataStore().listResourcesWithProperty(RDF.type,ontClass);
			while(iter.hasNext())
				{
				L.add(iter.next());
				}
			this.setRows(L);
			}
		finally
			{
			if(iter!=null) iter.close();
			}
		
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
		return this.getRDFSchema().shortForm(this.columns.get(col).getURI());
		}
	
	
	
	public void setColumns(List<Property> columns) {
		this.columns = new Vector<Property>(columns);
		fireTableStructureChanged();
		}
	
	public List<Property> getColumns()
		{
		return columns;
		}
	
	
	@Override
	public int getColumnCount() {
		return columns.size();
		}
	
	public Object getValueOf(final Resource rsrc, int col)
		{
		StmtIterator iter=null;
		try
			{
			iter=this.getRDFDataStore().listStatements( rsrc,columns.get(col),(RDFNode)null);
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
	
	}
