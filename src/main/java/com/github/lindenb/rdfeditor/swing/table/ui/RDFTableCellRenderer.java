package com.github.lindenb.rdfeditor.swing.table.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;


@SuppressWarnings("serial")
public class RDFTableCellRenderer
	extends DefaultTableCellRenderer
	{
	private Model schema;
	public RDFTableCellRenderer(Model schema)
		{
		this.schema=schema;
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
		if(schema==null) return c;
		if(value!=null)
			{
			if(value instanceof Resource)
				{
				Resource r=Resource.class.cast(value);
				if(r.isAnon())
					{
					this.setText(r.getId().getLabelString());
					}
				else
					{
					this.setText(schema.shortForm(r.getURI()));
					}
				}
			}
		return c;
		}
	}
