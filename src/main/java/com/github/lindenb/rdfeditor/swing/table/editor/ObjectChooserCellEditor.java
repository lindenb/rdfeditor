package com.github.lindenb.rdfeditor.swing.table.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import com.github.lindenb.rdfeditor.rdf.SchemaAndModel;
import com.github.lindenb.rdfeditor.swing.table.InstanceListTableModel;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.RDFS;

@SuppressWarnings("serial")
public class ObjectChooserCellEditor  
	extends AbstractCellEditor 
	implements TableCellEditor,SchemaAndModel
	{
	private SchemaAndModel schemaAndModel;
	private JButton jbutton;
	private boolean userCanceled=true;
	private int propertyColumn;
	private ComponentEditor componentEditor;
	
	private interface ComponentEditor
		{
		public Component getComponent();
		public void setCelleEditorValue(Object v);
		public RDFNode getCellEditorValue();
		}
	
	private abstract class AbtractComponentEditor extends JPanel implements ComponentEditor
		{
		AbtractComponentEditor(Property predicate)
			{
			super(new BorderLayout(5,5));
			if(predicate!=null)
				{
				this.setBorder(BorderFactory.createTitledBorder(getRDFSchema().shortForm(predicate.getURI())));
				}
			}
		public Component getComponent()
			{
			return this;
			}
		public abstract void setCelleEditorValue(Object v);
		public abstract RDFNode getCellEditorValue();
		}
	
	private class VoidComponentEditor
	extends AbtractComponentEditor
		{
		VoidComponentEditor(String msg)
			{
			super(null);
			this.add(new JLabel(msg),BorderLayout.CENTER);
			
			}
		public void setCelleEditorValue(Object v)
			{
			}
		public RDFNode getCellEditorValue()
			{
			return null;
			}
		}
	
	private class TextFieldComponentEditor
		extends AbtractComponentEditor
		{
		private JTextField tf=new JTextField();
		private RDFDatatype dataType;
		TextFieldComponentEditor(Property predicate,RDFDatatype dataType)
			{
			super(predicate);
			this.dataType=dataType;
			this.add(tf,BorderLayout.CENTER);
			}
		public void setCelleEditorValue(Object v)
			{
			tf.setText(v==null?"":v.toString());
			}
		public RDFNode getCellEditorValue()
			{
			return ResourceFactory.createTypedLiteral(tf.getText(), this.dataType);
			}
		}
	
	private class TextAreaComponentEditor
	extends AbtractComponentEditor
		{
		private JTextArea tf=new JTextArea(10,50);
		private RDFDatatype dataType;
		TextAreaComponentEditor(Property predicate,RDFDatatype dataType)
			{
			super(predicate);
			this.dataType=dataType;
			this.add(new JScrollPane(tf),BorderLayout.CENTER);
			}
		public void setCelleEditorValue(Object v)
			{
			tf.setText(v==null?"":v.toString());
			}
		public RDFNode getCellEditorValue()
			{
			return ResourceFactory.createTypedLiteral(tf.getText(), this.dataType);
			}
		}
	
	private class ChooseObjectComponentEditor
	extends AbtractComponentEditor
		{
		InstanceListTableModel ilt;
		JTable table;
		ChooseObjectComponentEditor(Property predicate,Resource range)
			{
			super(predicate);
			this. ilt=new InstanceListTableModel(ObjectChooserCellEditor.this.schemaAndModel,range);
			this.table=new JTable(ilt);
			this.table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane scroll=new JScrollPane(this.table);
			this.add(scroll,BorderLayout.CENTER);
			}
		
		public void setCelleEditorValue(Object v)
			{
			int r=this.ilt.getRows().indexOf(v);
			if(r==-1)
				{
				this.table.getSelectionModel().clearSelection();
				}
			else
				{
				this.table.setRowSelectionInterval(r, r);
				}
			}
		
		public RDFNode getCellEditorValue()
			{
			int r=this.table.getSelectedRow();
			if(r==-1) return null;
			Resource object=this.ilt.getElementAt(r);
			return object;
			}
		}
	
	
	public ObjectChooserCellEditor(
			int propertyColumn,
			SchemaAndModel schemaAndModel,
			Resource ontClass
			)
		{
		this.propertyColumn=propertyColumn;
		this.schemaAndModel=schemaAndModel;
		
		this.jbutton=new JButton();
		this.jbutton.setBorderPainted(false);
		this.jbutton.addActionListener(new ActionListener()
				{
				@Override
				public void actionPerformed(ActionEvent evt)
					{
					userCanceled=false;
					
					
					
					
					JOptionPane jop=new JOptionPane(componentEditor.getComponent(),JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null);
	                JDialog dialog = jop.createDialog(jbutton,"X");
	                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	                    dialog.setVisible(true);
	                   
	                    if(null == jop.getValue()) {
	                    System.out.println("User closed dialog");
	                    userCanceled=true;
					}
				else {
	                    switch(((Integer)jop.getValue()).intValue()) {
	                    case JOptionPane.OK_OPTION:
	                        System.out.println("User selected OK");
	                        break;
	                    case JOptionPane.CANCEL_OPTION:
	                    	userCanceled=true;
	                    	fireEditingCanceled();
	                        break;
	                    default:
	                        System.out.println("User selected " + jop.getValue());
	                    }
	                }
	                    fireEditingStopped();
				}});
		}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
	boolean isSelected, int row, int column)
		{
		Object propObject = table.getValueAt(row, this.propertyColumn);
		if(propObject==null || !(propObject instanceof Property))
			{
			componentEditor=new VoidComponentEditor("select RDF:Property please.");
			}
		else
			{
			final TypeMapper tm=new TypeMapper();
			XSDDatatype.loadXSDSimpleTypes(tm);

			componentEditor=new VoidComponentEditor("unknown property");
			ExtendedIterator<Statement> iter=null;
			try
				{
				final Property predicate=Property.class.cast(propObject);
				iter=getRDFSchema().listStatements(predicate,RDFS.range,(RDFNode)null).filterKeep(new Filter<Statement>() {
					@Override
					public boolean accept(Statement stmt)
						{
						System.err.println("searching "+stmt);
						if(!stmt.getObject().isResource()) return false;
						if(stmt.getObject().asResource().isAnon()) return false;
						System.err.println("ok? "+stmt);
						return true;
						}
					});
				while(iter.hasNext())
					{
					Statement stmt=iter.next();
					System.err.println(stmt);
					RDFDatatype datatType=tm.getTypeByName(stmt.getObject().asResource().getURI());
					if(datatType!=null)
						{
						componentEditor=new TextFieldComponentEditor(predicate,datatType);
						if(datatType.equals(XSDDatatype.XSDstring))
							{
							componentEditor=new TextAreaComponentEditor(predicate,datatType);
							break;
							}
						}
					else
						{
						componentEditor=new ChooseObjectComponentEditor(predicate,stmt.getObject().asResource());
						break;
						}
					}
				}
			finally
				{
				if(iter!=null) iter.close();
				}
			}
		return jbutton;
		}
	
	 @Override
    public boolean isCellEditable(EventObject e)
        {
        if(e instanceof MouseEvent)
            {
            MouseEvent m=(MouseEvent)e;
            if(m.getClickCount()<2)  return false;
            }
        return super.isCellEditable(e);
        }

	 public boolean isUserCanceled() {
		return userCanceled;
	 }
	 
	@Override
	public Object getCellEditorValue() {
		return componentEditor==null?null:componentEditor.getCellEditorValue();
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
