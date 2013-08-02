package com.github.lindenb.rdfeditor.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import com.github.lindenb.rdfeditor.rdf.PropertyAndObject;
import com.github.lindenb.rdfeditor.rdf.SchemaAndModel;
import com.github.lindenb.rdfeditor.swing.table.InDomainTableModel;
import com.github.lindenb.rdfeditor.swing.table.editor.ObjectChooserCellEditor;
import com.github.lindenb.rdfeditor.swing.table.editor.PropertyChooserCellEditor;
import com.github.lindenb.rdfeditor.swing.table.ui.RDFTableCellRenderer;
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
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@SuppressWarnings(value={"serial","rawtypes","unchecked"})
public class InstanceCreator
	extends JDialog
	implements SchemaAndModel
	{
	private SchemaAndModel schemaAndModel;
	private Resource subject;
	private Resource ontClass;
	
	
	private class PropModel extends InDomainTableModel
		{
		PropModel()
			{
			List<PropertyAndObject> L=new ArrayList<PropertyAndObject>();
			while(L.size()<50) L.add(null);
			setRows(L);
			}
		
		
		@Override
		public void setValueAt(Object v, int row, int col)
			{
			if(col!=0)
				{
				return ;
				}
			if(v==null || !(v instanceof Property))
				{
				return;
				}
			
			PropertyAndObject pao=getElementAt(row);
			if(pao==null) pao=new PropertyAndObject();
			pao.setPredicate((Property)v);
			setElementAt(row,pao);
			}
		@Override
		public boolean isCellEditable(int arg0, int arg1)
			{
			return true;
			}
		}
	private PropModel tableModel;
	
	public InstanceCreator(
			Window owner,
			SchemaAndModel schemaAndModel,
			Resource rdfType,
			Resource subject
			)
		{
		super(owner,ModalityType.APPLICATION_MODAL);
		this.schemaAndModel=schemaAndModel;
		this.ontClass=rdfType;
		this.subject=subject;
		
		setTitle("New "+getRDFSchema().shortForm(rdfType.getURI())+" "+subject);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				doOk();
				}
		});
		
		Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds(100,100,screen.width-200,screen.height-200);
		JPanel mainPane=new JPanel(new BorderLayout(5,5));
		setContentPane(mainPane);
		JTabbedPane tabbed=new JTabbedPane();
		mainPane.add(tabbed,BorderLayout.CENTER);
		
		
		
		JPanel top=new JPanel(new BorderLayout(5,5));
		
		
		
		this.tableModel=new PropModel();
		JTable t=new JTable(tableModel);
		for(int i=0;i< t.getColumnModel().getColumnCount();++i)
			{
			TableColumn tc=t.getColumnModel().getColumn(i);
			if(i==0) tc.setCellEditor(new PropertyChooserCellEditor(getRDFSchema(),rdfType));
			if(i==1) tc.setCellEditor(new ObjectChooserCellEditor(0,schemaAndModel,rdfType));
			tc.setCellRenderer(new RDFTableCellRenderer(getRDFSchema(),
					i==0?RDFTableCellRenderer.PREFIX_ARROW_LEFT:RDFTableCellRenderer.PREFIX_ARROW_RIGHT));
			}
		JScrollPane scroll=new JScrollPane(t);
		top.add(scroll,BorderLayout.CENTER);
		
		JPanel left=new JPanel();
		JPanel right=new JPanel();
		
		JSplitPane south=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,right);
		
		
		tabbed.addTab("Properties", new JSplitPane(JSplitPane.VERTICAL_SPLIT,top,south));
		
		
		
		JPanel bottom=new JPanel(new FlowLayout(FlowLayout.TRAILING));
		mainPane.add(bottom,BorderLayout.SOUTH);
		
		AbstractAction action=new AbstractAction("Cancel")
			{
			@Override
			public void actionPerformed(ActionEvent arg0)
				{
				doCancel();
				}
			};
		JButton but=new JButton(action);
		but.setContentAreaFilled(true);
		but.setBackground(Color.ORANGE);
		bottom.add(but);
		
		 action=new AbstractAction("OK")
			{
			@Override
			public void actionPerformed(ActionEvent arg0)
				{
				doOk();
				}
			};
		but=new JButton(action);
		but.setContentAreaFilled(true);
		but.setBackground(Color.GREEN);
		bottom.add(but);
		}
	
	private void doCancel()
		{
		this.setVisible(false);
		this.dispose();
		}
	
	private void doOk()
		{
		if(!validateSchema()) return;
		saveToModel();
		this.setVisible(false);
		this.dispose();
		}
	
	private List<PropertyAndObject> getValidPropertyAndObjects()
		{
		List<PropertyAndObject> L=new ArrayList<PropertyAndObject>(tableModel.getRowCount());
		for(PropertyAndObject p:tableModel.getRows())
			{
			if(p==null) continue;
			if(p.getPredicate()==null) continue;
			if(p.getObject()==null) continue;
			L.add(p);
			}
		return L;
		}
	
	private void saveToModel()
		{
		getRDFDataStore().removeAll(this.subject, null, null);
		
		getRDFDataStore().add(this.subject,RDF.type, this.ontClass);
		for(PropertyAndObject p:getValidPropertyAndObjects())
			{
			getRDFDataStore().add(this.subject, p.getPredicate(), p.getObject());
			}
		}
	
	private boolean validateSchema()
		{
		Vector<String> errors=new Vector<String>();
		
		
		Set<Property> properties=new HashSet<Property>();
		ExtendedIterator<Statement> iter=null;
		try {
			iter=getRDFSchema().listStatements(null, RDFS.domain, this.ontClass);
			while(iter.hasNext())
				{
				Statement stmt=iter.next();
				Resource r1=stmt.getSubject();
				if(!r1.hasProperty(RDF.type,RDF.Property)) continue;
				properties.add(ResourceFactory.createProperty(r1.getURI()));
				}
			} 
		finally
			{
			if(iter!=null) iter.close();
			}
		//check all properties are defined in schema
		for(PropertyAndObject pao:getValidPropertyAndObjects())
			{
			if(!properties.contains(pao.getPredicate()))
				{
				errors.add("Property "+pao.getPredicate()+" is not defined in schema");
				}
			}
		
		final TypeMapper tm=new TypeMapper();
		XSDDatatype.loadXSDSimpleTypes(tm);
		//validate each property
		for(Property p:properties)
			{
			List<PropertyAndObject> poalWithThisProperty=new ArrayList<PropertyAndObject>();
			for(PropertyAndObject pao:getValidPropertyAndObjects())
				{
				if(pao.getPredicate().equals(p)) poalWithThisProperty.add(pao);
				}
			
			try {
				iter=getRDFSchema().listStatements(p,null,(RDFNode)null);
				while(iter.hasNext())
					{
					Statement stmt=iter.next();
					//rdf range
					if(stmt.getPredicate().equals( RDFS.range))
						{
						if(!stmt.getObject().isResource()) continue;
						RDFDatatype xsdType=tm.getTypeByName(stmt.getObject().asResource().getURI());
						if(xsdType==null) continue;
						for(PropertyAndObject pao:poalWithThisProperty)
							{
							if(!pao.getObject().isLiteral())
								{
								errors.add("Property "+pao+" is not defined in a literal");
								continue;
								}
							if(!xsdType.isValidValue(pao.getObject().asLiteral().getValue()))
								{
								errors.add("Property "+pao+" : not a valid "+xsdType);
								continue;
								}
							}
						
 						}
					else if(stmt.getPredicate().equals( OWL.minCardinality) && stmt.getObject().isLiteral())
						{
						int cardinality =stmt.getInt();
						if( poalWithThisProperty.size()< cardinality )
							{
							errors.add(p.toString()+" : expect a min-cardinality="+cardinality);
							continue;
							}
						}
					else if(stmt.getPredicate().equals( OWL.maxCardinality) && stmt.getObject().isLiteral())
						{
						int cardinality =stmt.getInt();
						if( poalWithThisProperty.size()> cardinality )
							{
							errors.add(p.toString()+" : expect a max-cardinality="+cardinality);
							continue;
							}
						}
					else if(stmt.getPredicate().equals( OWL.cardinality) && stmt.getObject().isLiteral())
						{
						int cardinality =stmt.getInt();
						if( poalWithThisProperty.size() != cardinality )
							{
							errors.add(p.toString()+" : expect a cardinality="+cardinality);
							continue;
							}
						}
					
					}
				} 
			finally
				{
				if(iter!=null) iter.close();
				}
			
			}
		
		
		if(errors.isEmpty()) return true;
		JList L=new JList(errors);
		JScrollPane scroll=new JScrollPane(L);
		JPanel pane=new JPanel(new BorderLayout(5,5));
		pane.add(scroll,BorderLayout.CENTER);
		String choices[]={"Fix the errors","I'll fix this later."};
		Object sel=JOptionPane.showInputDialog(this, pane,"Errors/Warnings",JOptionPane.WARNING_MESSAGE,null,choices,choices[0]);
		if(sel!=choices[1]) return false;
		return true;
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
	
	public static InstanceCreator create(
			Component owner,
			SchemaAndModel schemaAndModel,
			Resource ontClass
			)
		{

		String defaultUri;
		int i=0;
		do
			{
			String localName=ontClass.getLocalName();
			if(localName==null) localName="class";
			defaultUri=String.format("urn:%s:%05d",localName,++i);
			} while(schemaAndModel.getRDFDataStore().containsResource(ResourceFactory.createResource(defaultUri)));
		Resource subject=null;
		JTextField tf=new JTextField(defaultUri,50);
		for(;;)
			{
			if(JOptionPane.showConfirmDialog(
					owner,
					tf,
					"Enter a valid URI",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null
					)!=JOptionPane.OK_OPTION) return null;
			if(tf.getText().trim().isEmpty()) return null;
			defaultUri=tf.getText();
			URI uri=null;
			try {
				uri=new URI(tf.getText().trim());
				if(!uri.isAbsolute())
					{
					JOptionPane.showMessageDialog(owner, "uri :" +uri+" is not absolute");
					continue;
					}
				if(!uri.isOpaque())
					{
					JOptionPane.showMessageDialog(owner, "uri :" +uri+" is not opaque");
					continue;
					}
				if(schemaAndModel.getRDFDataStore().containsResource(ResourceFactory.createResource(tf.getText())))
					{
					JOptionPane.showMessageDialog(owner, "model already contains that URI:"+tf.getText());
					continue;
					}
				subject=ResourceFactory.createResource(tf.getText());
				break;
				}
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(owner, "Bad URI:" +uri);
				}
			}
		InstanceCreator dlg=new InstanceCreator(
				SwingUtilities.windowForComponent(owner),
				schemaAndModel,
				ontClass,
				subject
				);
		return dlg;
		}
	}
