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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

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
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import com.github.lindenb.rdfeditor.rdf.JenaUtils;
import com.github.lindenb.rdfeditor.rdf.PropertyAndObject;
import com.github.lindenb.rdfeditor.rdf.SchemaAndModel;
import com.github.lindenb.rdfeditor.swing.dialog.AskURI;
import com.github.lindenb.rdfeditor.swing.table.editor.ObjectChooserCellEditor;
import com.github.lindenb.rdfeditor.swing.table.editor.PropertyChooserCellEditor;
import com.github.lindenb.rdfeditor.swing.table.model.InDomainTableModel;
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
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

@SuppressWarnings(value={"serial","rawtypes","unchecked"})
public class InstanceCreator
	extends JDialog
	implements SchemaAndModel
	{
	private static final Logger LOG = Logger.getLogger("com.github.lindenb");

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
			
			if(v==null)
				{
				return;
				}
			if(col==0 && !(v instanceof Property))
				{
				return;
				}
			if(col==1 && !(v instanceof RDFNode))
				{
				return;
				}
			
			PropertyAndObject pao=getElementAt(row);
			if(pao==null) pao=new PropertyAndObject();
			switch(col)
				{
				case 0: pao.setPredicate((Property)v); break;
				case 1: pao.setObject((RDFNode)v); break;
				}
		
			setElementAt(row,pao);
			if(row+1==getRowCount())
				{
				for(int i=0;i< 10;++i)
					{
					super.addElement(null);
					}
				}
			}
		@Override
		public boolean isCellEditable(int row, int col)
			{
			PropertyAndObject pao=getElementAt(row);
			if(pao==null || pao.getPredicate()==null)
				{
				return col==0;
				}
			
			if(pao.getPredicate()!=null )
				{
				//the following are read-only
				if(pao.getPredicate().equals(DCTerms.creator)) return false;
				if(pao.getPredicate().equals(DCTerms.created)) return false;
				}
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
		if(!validateSchema())
			{
			LOG.info("schema was not validated");
			return;
			}
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
		List<Statement> to_delete=new ArrayList<Statement>();
		ExtendedIterator<Statement> iter=null;
		try {
			iter=getRDFDataStore().listStatements(this.subject, null,(RDFNode) null);
			while(iter.hasNext())
				{
				Statement stmt=iter.next();
				if(stmt.getPredicate().equals(DCTerms.created)) continue;
				if(stmt.getPredicate().equals(DCTerms.creator)) continue;
				to_delete.add(stmt);
				}
			} 
		finally
			{
			if(iter!=null) iter.close();
			}
		getRDFDataStore().remove(to_delete);
		
		//add creation date
		if(!getRDFDataStore().contains(this.subject,DCTerms.created,(RDFNode)null))
			{
			getRDFDataStore().add(this.subject,DCTerms.created, new Date().toString());
			}
		//add author
		if(!getRDFDataStore().contains(this.subject,DCTerms.creator,(RDFNode)null) &&
			System.getProperty("user.name")!=null)
			{
			getRDFDataStore().add(this.subject,DCTerms.creator,System.getProperty("user.name"));
			}
		//add modification
		getRDFDataStore().add(this.subject,DCTerms.modified,new Date().toString());
		//add rdf:type
		getRDFDataStore().add(this.subject,RDF.type, this.ontClass);
		
		if(System.getProperty("user.name")!=null)
			{
			getRDFDataStore().add(this.subject,DCTerms.contributor,System.getProperty("user.name"));
			}
		
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
					else if(stmt.getPredicate().getURI().equals(XSD.getURI()+"minExclusive") && 
							JenaUtils.convertToBigDecimal(stmt.getObject())!=null
							)
						{
						BigDecimal schemaNum = JenaUtils.convertToBigDecimal(stmt.getObject());
						for(PropertyAndObject pao:poalWithThisProperty)
							{
							BigDecimal modelNum = JenaUtils.convertToBigDecimal(pao.getObject());
							if(modelNum==null)
								{
								errors.add(p.toString()+" illegal value for "+pao);
								continue;
								}
							else if(modelNum.compareTo(schemaNum)<0)
								{
								errors.add(p.toString()+" illegal value for "+pao+" < "+schemaNum);
								continue;
								}
							}
						}
					else if(stmt.getPredicate().getURI().equals(XSD.getURI()+"maxExclusive") && 
							JenaUtils.convertToBigDecimal(stmt.getObject())!=null
							)
						{
						BigDecimal schemaNum = JenaUtils.convertToBigDecimal(stmt.getObject());
						for(PropertyAndObject pao:poalWithThisProperty)
							{
							BigDecimal modelNum = JenaUtils.convertToBigDecimal(pao.getObject());
							if(modelNum==null)
								{
								errors.add(p.toString()+" illegal value for "+pao);
								continue;
								}
							else if(modelNum.compareTo(schemaNum)>0)
								{
								errors.add(p.toString()+" illegal value for "+pao+" > "+schemaNum);
								continue;
								}
							}
						}
					else if(stmt.getPredicate().getURI().equals(XSD.getURI()+"pattern") &&
							stmt.getObject().isLiteral()
							)
						{
						Pattern regex=null;
						try
							{
							regex=Pattern.compile(stmt.getObject().asLiteral().getValue().toString());
							}
						catch(Exception err)
							{
							LOG.error("bad regex in schema",err);
							continue;
							}
						for(PropertyAndObject pao:poalWithThisProperty)
							{
							if(pao.getObject().isLiteral())
								{
								if(!regex.matcher(pao.getObject().asLiteral().getValue().toString()).matches())
									{
									LOG.error(""+pao.getObject()+" doesn't match "+regex.pattern());
									continue;
									}
								}
							else if(pao.getObject().isLiteral() && pao.getObject().asResource().isURIResource())
								{
								if(!regex.matcher(pao.getObject().asResource().getURI()).matches())
									{
									LOG.error(""+pao.getObject()+" doesn't match "+regex.pattern());
									continue;
									}
								}
							}
						
						
						}
					else if(stmt.getPredicate().getURI().equals(XSD.getURI()+"length") && 
							JenaUtils.convertToBigInteger(stmt.getObject())!=null
							)
						{
						BigInteger Len0=JenaUtils.convertToBigInteger(stmt.getObject());
						for(PropertyAndObject pao:poalWithThisProperty)
							{
							if(!pao.getObject().isLiteral()) continue;
							int len1=pao.getObject().asLiteral().getValue().toString().length();
							BigInteger Len1=new BigInteger(String.valueOf(len1));
							if(Len1.compareTo(Len0)!=0)
								{
								errors.add(p.toString()+" illegal length for "+pao+"  length: "+Len0);
								continue;
								}
							}
						}
					else if(stmt.getPredicate().getURI().equals(XSD.getURI()+"minLength") && 
							JenaUtils.convertToBigInteger(stmt.getObject())!=null
							)
						{
						BigInteger Len0=JenaUtils.convertToBigInteger(stmt.getObject());
						for(PropertyAndObject pao:poalWithThisProperty)
							{
							if(!pao.getObject().isLiteral()) continue;
							int len1=pao.getObject().asLiteral().getValue().toString().length();
							BigInteger Len1=new BigInteger(String.valueOf(len1));
							if(Len1.compareTo(Len0)<0)
								{
								errors.add(p.toString()+" illegal length for "+pao+" min-length: "+Len0);
								continue;
								}
							}
						}
					else if(stmt.getPredicate().getURI().equals(XSD.getURI()+"maxLength") && 
							JenaUtils.convertToBigInteger(stmt.getObject())!=null
							)
						{
						BigInteger Len0=JenaUtils.convertToBigInteger(stmt.getObject());
						for(PropertyAndObject pao:poalWithThisProperty)
							{
							if(!pao.getObject().isLiteral()) continue;
							int len1=pao.getObject().asLiteral().getValue().toString().length();
							BigInteger Len1=new BigInteger(String.valueOf(len1));
							if(Len1.compareTo(Len0)>0)
								{
								errors.add(p.toString()+" illegal length for "+pao+" max-length: "+Len0);
								continue;
								}
							}
						}
					}
				} 
			finally
				{
				if(iter!=null) iter.close();
				}
			
			}
		
		
		if(errors.isEmpty())
			{
			LOG.info("no error while validation");
			return true;
			}
		JList L=new JList(errors);
		JScrollPane scroll=new JScrollPane(L);
		JPanel pane=new JPanel(new BorderLayout(5,5));
		pane.add(scroll,BorderLayout.CENTER);
		String choices[]={"Fix the errors","I'll fix this later."};
		int selidx=JOptionPane.showOptionDialog(
				this,
				pane,
				"Errors/Warnings",
				 JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				choices,
				choices[0]
				);
			
		LOG.info("user selected "+selidx);
		if(selidx!=1) return false;
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
		AskURI asker=new AskURI(owner, schemaAndModel.getRDFDataStore(), ontClass);
		asker.pack();
		asker.setLocationRelativeTo(owner);
		asker.setVisible(true);
		if(asker.getResource()==null || asker.getReturnStatus()!=AskURI.OK_OPTION) return null;
		
		InstanceCreator dlg=new InstanceCreator(
				SwingUtilities.windowForComponent(owner),
				schemaAndModel,
				ontClass,
				asker.getResource()
				);
		return dlg;
		}
	}
