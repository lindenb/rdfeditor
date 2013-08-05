package com.github.lindenb.rdfeditor.swing.iframe;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.github.lindenb.rdfeditor.RDFEditorFrame;
import com.github.lindenb.rdfeditor.rdf.PropertyAndObject;
import com.github.lindenb.rdfeditor.swing.table.model.InDomainTableModel;
import com.github.lindenb.rdfeditor.swing.table.model.ResourcesInRangeTableModel;
import com.github.lindenb.rdfeditor.swing.table.ui.RDFTableCellRenderer;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@SuppressWarnings("serial")
public class InstanceViewInternalFrame extends AbstractInternalFrame
	{
	private Resource ontClass;
	private Resource subject;
	private InDomainTableModel inDomainTableModel;
	private JTable inDomainTable;
	private ResourcesInRangeTableModel resourcesInRangeTableModel;
	private JTable resourcesInRangeTable;
	
	public InstanceViewInternalFrame(RDFEditorFrame frame,Resource ontClass,Resource subject)
		{
		super(frame);
		setClosable(true);
		setTitle(subject.toString());
		this.ontClass=ontClass;
		this.subject=subject;
		
		JPanel pane=new JPanel(new BorderLayout());
		setContentPane(pane);
		
		JTextField tf=new JTextField(
				(subject.isAnon()?subject.getId().getLabelString():subject.getURI())
				);
		tf.setEditable(false);
		tf.setFont(new Font("Dialog",Font.PLAIN,18));
		pane.add(tf,BorderLayout.NORTH);
		
		
		JPanel left=new JPanel(new BorderLayout());
		left.setBorder(BorderFactory.createTitledBorder("Object Properties"));
		this.inDomainTableModel=new InDomainTableModel();
		this.inDomainTable=new JTable(inDomainTableModel);
		this.inDomainTable.setDefaultRenderer(Object.class, new RDFTableCellRenderer(getRDFSchema(),RDFTableCellRenderer.PREFIX_ARROW_RIGHT));
		left.add(new JScrollPane(this.inDomainTable));

		this.inDomainTable.addMouseListener(new MouseAdapter()
			{
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()<2) return;
				int row=inDomainTable.getSelectedRow();
				if(row==-1) return;
				PropertyAndObject pao=inDomainTableModel.getElementAt(row);
				if(pao==null || pao.getObject()==null || !pao.getObject().isResource()) return;	
				showSelectedInstance(pao.getObject().asResource());
				}
			});
		
		
		JPanel right=new JPanel(new BorderLayout());
		right.setBorder(BorderFactory.createTitledBorder("What's link here."));
		this.resourcesInRangeTableModel=new ResourcesInRangeTableModel(getRDFEditorFrame(),subject);
		this.resourcesInRangeTable=new JTable(resourcesInRangeTableModel);
		this.resourcesInRangeTable.setDefaultRenderer(Object.class, new RDFTableCellRenderer(getRDFSchema(),RDFTableCellRenderer.PREFIX_ARROW_LEFT));

		this.resourcesInRangeTable.addMouseListener(new MouseAdapter()
			{
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()<2) return;
				int row=resourcesInRangeTable.getSelectedRow();
				if(row==-1) return;
				PropertyAndObject pao=resourcesInRangeTableModel.getElementAt(row);
				if(pao==null || pao.getObject()==null || !pao.getObject().isResource()) return;	
				showSelectedInstance(pao.getObject().asResource());
				}
			});
		
		
		right.add(new JScrollPane(this.resourcesInRangeTable));
		

		pane.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,right),BorderLayout.CENTER);
		fireModelChanged();
		}
	
	public void fireModelChanged()
		{
		if(!getRDFDataStore().contains(getSubject(), RDF.type, getRdfType()))
			{
			this.setVisible(false);
			this.dispose();
			}
		this.inDomainTableModel.clear();
		this.resourcesInRangeTableModel.clear();
		
		List<PropertyAndObject> rows1=new ArrayList<PropertyAndObject>();
		ExtendedIterator<Statement> r1=null;
		try
			{
			r1=getRDFSchema().listStatements(null, RDFS.domain, getRdfType());
			while(r1.hasNext())
				{
				Statement stmt1=r1.next();
				ExtendedIterator<Statement> r2=null;
				try
					{
					r2=getRDFDataStore().listStatements(
							this.getSubject(),
							ResourceFactory.createProperty(stmt1.getSubject().getURI()),
							(RDFNode)null
							);
					while(r2.hasNext())
						{
						Statement stmt2=r2.next();
						rows1.add(new PropertyAndObject(stmt2.getPredicate(),stmt2.getObject()));
						}
					}
				finally
					{
					if(r2!=null) r2.close();
					}
				}
			}
		finally
			{
			if(r1!=null) r1.close();
			}
		this.inDomainTableModel.setRows(rows1);
		
		
		rows1.clear();
		r1=null;
		try
			{
			r1=getRDFSchema().listStatements(null, RDFS.range, getRdfType());
			while(r1.hasNext())
				{
				Statement stmt1=r1.next();
				ExtendedIterator<Statement> r2=null;
				try
					{
					r2=getRDFDataStore().listStatements(
							null,
							ResourceFactory.createProperty(stmt1.getSubject().getURI()),
							this.getSubject()
							);
					while(r2.hasNext())
						{
						Statement stmt2=r2.next();
						rows1.add(new PropertyAndObject(stmt2.getPredicate(),stmt2.getSubject()));
						}
					}
				finally
					{
					if(r2!=null) r2.close();
					}
				}
			}
		finally
			{
			if(r1!=null) r1.close();
			}
		this.resourcesInRangeTableModel.setRows(rows1);
		
		
		
		}
	
	private void showSelectedInstance(Resource x)
		{
		for(JInternalFrame f:getDesktopPane().getAllFrames())
			{
			if(!(f instanceof InstanceViewInternalFrame)) continue;
			if(InstanceViewInternalFrame.class.cast(f).getSubject().equals(x))
				{
				f.moveToFront();
				return;
				}
			}
		Resource rdfType=null;
		
		ExtendedIterator<Statement> r2=null;
		try
			{
			r2=getRDFDataStore().listStatements(
					x,
					RDF.type,
					(RDFNode)null
					);
			while(r2.hasNext())
				{
				Statement stmt2=r2.next();
				if(!stmt2.getObject().isResource()) continue;
				if(!getRDFSchema().contains(stmt2.getObject().asResource(),RDF.type,RDFS.Class)) continue;
				rdfType=stmt2.getObject().asResource();
				break;
				}
			}
		finally
			{
			if(r2!=null) r2.close();
			}
		if(rdfType==null) return;
		InstanceViewInternalFrame ivif=new InstanceViewInternalFrame(this.getRDFEditorFrame(), rdfType, x);
		getDesktopPane().add(ivif);
		ivif.setVisible(true);	
		}

	/** returns the RDFS:Class for this editor */ 
	public Resource getRdfType()
		{
		return ontClass;
		}
	
	public Resource getSubject()
		{
		return this.subject;
		}
	

	
}
