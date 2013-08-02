package com.github.lindenb.rdfeditor.swing.iframe;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.github.lindenb.rdfeditor.RDFEditorFrame;
import com.github.lindenb.rdfeditor.rdf.JenaUtils;
import com.github.lindenb.rdfeditor.swing.InstanceCreator;
import com.github.lindenb.rdfeditor.swing.table.AbstractGenericTableModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/** list all the rdf:type */
@SuppressWarnings("serial")
public class OntClassInternalFrame
	extends AbstractInternalFrame
	{
	private AbstractGenericTableModel<Resource> instanceListTableModel;
	private JTable instanceTable;
	public OntClassInternalFrame(RDFEditorFrame owner)
		{
		super(owner);
		this.setTitle("Classes");
		
		List<Resource> L=new ArrayList<Resource>();
		ResIterator iter=getRDFSchema().listResourcesWithProperty(RDF.type,RDFS.Class);
		while(iter.hasNext())
			{
			L.add(iter.nextResource());
			}
		iter.close();
		
		this.instanceListTableModel = new AbstractGenericTableModel<Resource>(L)
			{
			@Override
			public Class<?> getColumnClass(int col)
				{
				switch(col)
					{
					case 0: case 1: return RDFNode.class;
					case 2: return Integer.class;
					default: return Object.class;
					}
				}
			@Override
				public String getColumnName(int col) {
				switch(col)
					{
					case 0: return "Label";
					case 1: return "Comment";
					case 2: return "Count";
					default: return "$"+(col+1);
					}
				}
			@Override
			public int getColumnCount()
				{
				return 3;
				}
			
			@Override
			public Object getValueOf(final Resource o, int col)
				{
				switch(col)
					{
					case 0: return JenaUtils.one(o, RDFS.label);
					case 1: return JenaUtils.one(o, RDFS.comment);
					case 2:
						{
						int n=0;
						StmtIterator iter=null;
						try
							{
							iter=getRDFDataStore().listStatements(null,RDF.type,o);
							while(iter.hasNext())
								{
								iter.next();
								++n;
								}
							return n;
							}
						finally
							{
							if(iter!=null) iter.close();
							}
						}
					default: return null;
					}
				}
			};
		
		
		this.instanceTable=new JTable(this.instanceListTableModel);
		JScrollPane scroll=new JScrollPane(this.instanceTable);
		JPanel pane=new JPanel(new BorderLayout(5,5));
		pane.add(scroll,BorderLayout.CENTER);
		setContentPane(pane);
		
		
		AbstractAction action=new AbstractAction("View Instances")
				{
				@Override
				public void actionPerformed(ActionEvent evt)
					{
					showSelectedInstanceFrame();
					}
				};
		action.setEnabled(false);		
				

		this.instanceTable.addMouseListener(new MouseAdapter()
			{
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()>1) showSelectedInstanceFrame();
				}
			});
		
		
		JPanel bottom=new JPanel(new FlowLayout(FlowLayout.TRAILING));
		bottom.add(new JButton(action));
		pane.add(bottom,BorderLayout.SOUTH);
		
		
		getActionMap().put("view.instances", action);
		
		
		action=new AbstractAction("Create Instance")
			{
			@Override
			public void actionPerformed(ActionEvent evt)
				{
				createInstanceDialog();
				}
			};
		action.setEnabled(false);
		getActionMap().put("create.instance", action);
		bottom.add(new JButton(action));
		
		
		this.instanceTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.instanceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
			{
			
			@Override
			public void valueChanged(ListSelectionEvent evt)
				{
				getActionMap().get("view.instances").setEnabled(instanceTable.getSelectedRowCount()!=0);
				getActionMap().get("create.instance").setEnabled(instanceTable.getSelectedRowCount()!=0);
				}
			});
		
		
		

		}
	
	private void createInstanceDialog()
		{
		int row=instanceTable.getSelectedRow();
		if(row==-1) return;
		Resource ontClass= instanceListTableModel.getElementAt(row);
		
		InstanceCreator dlg=InstanceCreator.create(this,getRDFEditorFrame(),ontClass);
		if(dlg==null) return;
		dlg.setVisible(true);
		fireModelChanged();
		}
	
	private void showSelectedInstanceFrame()
		{
		int row=instanceTable.getSelectedRow();
		if(row==-1) return;
		Resource ontClass= instanceListTableModel.getElementAt(row);
		if(ontClass==null)
			{
			System.err.println("Uhhh???");
			return;
			}
		for(JInternalFrame f:getDesktopPane().getAllFrames())
			{
			if(!(f instanceof InstanceListInternalFrame)) continue;
			InstanceListInternalFrame ilif=InstanceListInternalFrame.class.cast(f);
			if(ilif.getOntClass()!=ontClass) continue;
			ilif.moveToFront();
			return;
			}
		InstanceListInternalFrame f=new InstanceListInternalFrame(
				getRDFEditorFrame(),
				ontClass
				);
		this.getDesktopPane().add(f);
		f.setVisible(true);
		}
	
	}
