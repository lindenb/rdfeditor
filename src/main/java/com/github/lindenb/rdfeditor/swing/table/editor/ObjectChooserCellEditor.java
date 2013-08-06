package com.github.lindenb.rdfeditor.swing.table.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Date;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

import com.github.lindenb.rdfeditor.rdf.JavaDataType;
import com.github.lindenb.rdfeditor.rdf.SchemaAndModel;
import com.github.lindenb.rdfeditor.swing.SelectFileComponent;
import com.github.lindenb.rdfeditor.swing.dialog.AskDialog;
import com.github.lindenb.rdfeditor.swing.dialog.AskURI;
import com.github.lindenb.rdfeditor.swing.table.model.AbstractGenericTableModel;
import com.github.lindenb.rdfeditor.swing.table.model.InstanceListTableModel;
import com.github.lindenb.rdfeditor.swing.table.model.RangeTableDomainModel;
import com.github.lindenb.rdfeditor.swing.table.ui.RDFTableCellRenderer;
import com.github.lindenb.rdfeditor.swing.text.RDFDataTypeTextComponent;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@SuppressWarnings("serial")
public class ObjectChooserCellEditor  
	extends AbstractCellEditor 
	implements TableCellEditor,SchemaAndModel
	{
	private static final Logger LOG = Logger.getLogger("com.github.lindenb");

	
	private SchemaAndModel schemaAndModel;
	private JButton jbutton;
	private boolean userCanceled=true;
	private int propertyColumn;
	private SelectValueDialog componentEditor;
	
	private abstract class SelectValueDialog extends AskDialog
		{
		SelectValueDialog(Property predicate)
			{
			super(SwingUtilities.getWindowAncestor(jbutton));
			
			if(predicate!=null)
				{
				super.contentPane.setBorder(BorderFactory.createTitledBorder(getRDFSchema().shortForm(predicate.getURI())));
				setTitle(getRDFSchema().shortForm(predicate.getURI()));
				}
			
			}
		
		
		public abstract void setCelleEditorValue(Object v);
		public abstract RDFNode getCellEditorValue();
		}
	
	
	
	private class VoidComponentEditor
	extends SelectValueDialog
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
	
	private abstract class AbstractTextFieldComponentEditor
	extends SelectValueDialog
		{
		private RDFDataTypeTextComponent tf;
		AbstractTextFieldComponentEditor(Property predicate,RDFDataTypeTextComponent tf)
			{
			super(predicate);
			this.tf=tf;
			getContentPane().add(tf,BorderLayout.CENTER);
			tf.addPropertyChangeListener(new PropertyChangeListener()
				{
				@Override
				public void propertyChange(PropertyChangeEvent evt)
					{
					okAction.setEnabled(AbstractTextFieldComponentEditor.this.tf.isValidLiteral());
					}
				});
			}
		public void setCelleEditorValue(Object v)
			{
			tf.setText(v==null?"":v.toString());
			}
		public RDFNode getCellEditorValue()
			{
			RDFNode node=tf.getLiteral();
			LOG.info("getCellEditorValue:"+node);
			return node;
			}
		}
	
	private class TextFieldComponentEditor
		extends AbstractTextFieldComponentEditor
		{
		TextFieldComponentEditor(Property predicate,RDFDatatype dataType)
			{
			super(predicate,new RDFDataTypeTextComponent(dataType, new JTextField(20)));
			}
	
		}
	
	private class TextAreaComponentEditor
	extends AbstractTextFieldComponentEditor
		{
		TextAreaComponentEditor(Property predicate,RDFDatatype dataType)
			{
			super(predicate,new RDFDataTypeTextComponent(dataType, new JTextArea(10,50)));
			}
	
		}

	
	private class AbstractChooseObjectComponentEditor
	extends SelectValueDialog
		{
		AbstractGenericTableModel<Resource> tableModel;
		JTable table;
		 AbstractChooseObjectComponentEditor(
				 Property predicate,
				 AbstractGenericTableModel<Resource> tableModel
				 )
			{
			super(predicate);
			this.tableModel=tableModel;
			LOG.info("tableModel:"+tableModel.getRowCount()+"/"+tableModel.getColumnCount()+" "+tableModel.getClass());
			this.table=new JTable(this.tableModel);
			this.table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			RDFTableCellRenderer render=new RDFTableCellRenderer(getRDFSchema(), "");
			for(int i=0;i< this.table.getColumnModel().getColumnCount();++i)
				{
				this.table.getColumnModel().getColumn(i).setCellRenderer(render);
				}
			
			JScrollPane scroll=new JScrollPane(this.table);
			getContentPane().add(scroll,BorderLayout.CENTER);
			this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					okAction.setEnabled(table.getSelectedRow()!=-1);
					}
				});
			this.table.addMouseListener(new MouseAdapter()
				{
				@Override
				public void mouseClicked(MouseEvent evt)
					{
					if(evt.getClickCount()<2) return;
					int r=table.rowAtPoint(evt.getPoint());
					if(r==-1) return;
					table.setRowSelectionInterval(r, r);
					doOKAction();
					}
				});
			}
		
		public void setCelleEditorValue(Object v)
			{
			int r=this.tableModel.getRows().indexOf(v);
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
			Resource object=this.tableModel.getElementAt(r);
			return object;
			}
		}
	
	private class ChooseObjectComponentEditor
		extends AbstractChooseObjectComponentEditor
		{
		private Resource range;
		private Resource newSubject=null;
		ChooseObjectComponentEditor(Property predicate,Resource range)
			{
			super(predicate,new InstanceListTableModel(
					ObjectChooserCellEditor.this.schemaAndModel,
					range
					));
			this.range=range;
			super.bottomPane.add(new JButton(new AbstractAction("... or create a new "+getRDFSchema().shortForm(range.getURI()))
				{
				@Override
				public void actionPerformed(ActionEvent evt)
					{
					AskURI asker=new AskURI(ChooseObjectComponentEditor.this, getRDFDataStore(), ChooseObjectComponentEditor.this.range);
					asker.pack();
					asker.setLocationRelativeTo((Component)evt.getSource());
					asker.setVisible(true);
					if(asker.getResource()==null || asker.getReturnStatus()!=AskURI.OK_OPTION) return;
					
					newSubject=asker.getResource();
					LOG.info("creating "+newSubject+" a new "+ChooseObjectComponentEditor.this.range);
					getRDFDataStore().add(newSubject, RDF.type, ChooseObjectComponentEditor.this.range);
					if(System.getProperty("user.name")!=null)
						{
						getRDFDataStore().add(newSubject, DCTerms.creator, System.getProperty("user.name"));
						}
					getRDFDataStore().add(newSubject,DCTerms.created, new Date().toString());
					doOKAction();
					}
				}));
		
			}
		public RDFNode getCellEditorValue()
			{
			if(newSubject!=null) return newSubject;
			return super.getCellEditorValue();
			}
		}
	
	private class ChooseRangeComponentEditor
		extends AbstractChooseObjectComponentEditor
		{
		ChooseRangeComponentEditor(Property predicate)
			{
			super(predicate,new RangeTableDomainModel(
					ObjectChooserCellEditor.this.schemaAndModel.getRDFDataStore()
					));
			}
		}
	
	private class SelectFileComponentEditor
		extends SelectValueDialog
		{
		private SelectFileComponent selFileComponent;
		public SelectFileComponentEditor(Property predicate)
			{
			super(predicate);
			this.selFileComponent=new SelectFileComponent();
			getContentPane().add(this.selFileComponent,BorderLayout.CENTER);
			this.selFileComponent.addPropertyChangeListener(SelectFileComponent.FILE_CHANGED_PROPERTY, new PropertyChangeListener()
				{
				@Override
				public void propertyChange(PropertyChangeEvent evt)
					{
					okAction.setEnabled(selFileComponent.getFile()!=null);
					}
				});
			}
		@Override
		public RDFNode getCellEditorValue()
			{
			File f=this.selFileComponent.getFile();
			if(f==null) return null;
			return ResourceFactory.createTypedLiteral(
				f.getPath(),
				JavaDataType.javaFile
				);
			}
		
		@Override
		public void setCelleEditorValue(Object v)
			{
			if(v==null || !(v instanceof Literal))
				{
				this.selFileComponent.setFile(null);
				return;
				}
			Literal L=Literal.class.cast(v);
			if(L.getDatatype()==null || !L.getDatatype().equals(JavaDataType.javaFile))
				{
				this.selFileComponent.setFile(null);
				return;
				}
			File f=(File)L.getValue();
			this.selFileComponent.setFile(f);
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
					
					
					componentEditor.pack();
					componentEditor.setLocationRelativeTo(jbutton);
					LOG.info("size"+componentEditor.getPreferredSize());
					componentEditor.setVisible(true);
					if(componentEditor.getReturnStatus()==JOptionPane.OK_OPTION)
						{
						LOG.info("use canceled=false");
						userCanceled=false;
						}
					else
						{
						LOG.info("use canceled=true");
						fireEditingCanceled();
						userCanceled=true;
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
		else if(propObject.equals(RDFS.range))
			{
			componentEditor=new ChooseRangeComponentEditor(RDFS.range);
			}
		else
			{

			componentEditor=new VoidComponentEditor("unknown property");
			ExtendedIterator<Statement> iter=null;
			try
				{
				final Property predicate=Property.class.cast(propObject);
				iter=getRDFSchema().listStatements(predicate,RDFS.range,(RDFNode)null).filterKeep(new Filter<Statement>() {
					@Override
					public boolean accept(Statement stmt)
						{
						if(!stmt.getObject().isResource()) return false;
						if(stmt.getObject().asResource().isAnon()) return false;
						return true;
						}
					});
				while(iter.hasNext())
					{
					Statement stmt=iter.next();
					
					RDFDatatype datatType=TypeMapper.getInstance().getTypeByName(stmt.getObject().asResource().getURI());
					if(datatType!=null)
						{
						componentEditor=new TextFieldComponentEditor(predicate,datatType);
						if(datatType.equals(XSDDatatype.XSDstring))
							{
							componentEditor=new TextAreaComponentEditor(predicate,datatType);
							break;
							}
						if(datatType.equals(JavaDataType.javaFile))
							{
							componentEditor=new SelectFileComponentEditor(predicate);
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
	public Object getCellEditorValue()
		{
		RDFNode node=componentEditor==null?null:componentEditor.getCellEditorValue();
		LOG.info("getCellEditorValue: "+node +" canceled:"+isUserCanceled());
		return node;
		}

	
	@Override
	public void fireModelChanged() {
		this.schemaAndModel.fireModelChanged();
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
