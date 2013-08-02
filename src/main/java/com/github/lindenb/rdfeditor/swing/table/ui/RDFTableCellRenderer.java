package com.github.lindenb.rdfeditor.swing.table.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


@SuppressWarnings("serial")
public class RDFTableCellRenderer
	extends DefaultTableCellRenderer
	{
	public static final String PREFIX_NONE="";
	public static final String PREFIX_ARROW_RIGHT="\u2192 ";//->
	public static final String PREFIX_ARROW_LEFT="\u2190 ";//<-
	
	private Model schema;
	private String resourcePrefix;
	public RDFTableCellRenderer(Model schema,String resourcePrefix)
		{
		this.schema=schema;
		this.resourcePrefix=(resourcePrefix==null?PREFIX_NONE:resourcePrefix);
		}
	
	
	public Model getRDFSchema()
		{
		return schema;
		}
	
	@Override
	public Component getTableCellRendererComponent(
			JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
            ) {
		Component c=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
		if(getRDFSchema()==null) return c;
		if(value!=null)
			{
			if(value instanceof Resource)
				{
				String pfx=this.resourcePrefix;
				if(value instanceof Property) pfx="";
				Resource r=Resource.class.cast(value);
				
				if(r.isAnon())
					{
					this.setText("<html>"+pfx+"<a href='#'>"+r.getId().getLabelString()+"</a>");
					}
				else
					{
					this.setText("<html>"+pfx+"<a href='#'>"+getRDFSchema().shortForm(r.getURI())+"</a>");
					}
				}
			else if(value instanceof Literal)
				{
				this.setText(Literal.class.cast(value).getLexicalForm());
				}
			}
		return c;
		}
	}
