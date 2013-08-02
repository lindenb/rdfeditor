package com.github.lindenb.rdfeditor.swing.iframe;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


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
import com.github.lindenb.rdfeditor.swing.InstanceCreator;
import com.github.lindenb.rdfeditor.swing.table.InstanceListTableModel;
import com.hp.hpl.jena.rdf.model.Resource;

/** list all instances for the given class */
@SuppressWarnings("serial")
public class InstanceListInternalFrame
	extends AbstractInternalFrame
	{
	private InstanceListTableModel instanceListTableModel;
	private JTable instanceTable;
	private Resource ontClass;
	public InstanceListInternalFrame(
			final RDFEditorFrame owner,
			final Resource rsrcClass
			)
		{
		super(owner);
		this.ontClass=rsrcClass;
		setClosable(true);
		
		this.setTitle(this.ontClass.getURI());
		
		this.instanceListTableModel=new InstanceListTableModel(owner, rsrcClass);
		this.instanceTable=new JTable(this.instanceListTableModel);
		JScrollPane scroll=new JScrollPane(this.instanceTable);
		JPanel pane=new JPanel(new BorderLayout(5,5));
		pane.add(scroll,BorderLayout.CENTER);
		this.setContentPane(pane);
		
		
		this.instanceTable.addMouseListener(new MouseAdapter()
			{
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()>1) showSelectedInstance();
				}
			});
		
		AbstractAction action=new AbstractAction("View Instance")
				{
				@Override
				public void actionPerformed(ActionEvent evt)
					{
					showSelectedInstance();
					}
				};
		action.setEnabled(false);		
		getActionMap().put("view.instance", action);
		
		action=new AbstractAction("New Instance of "+getRDFSchema().shortForm(getOntClass().getURI()))
			{
			@Override
			public void actionPerformed(ActionEvent evt)
				{
				createInstanceDialog();
				}
			};
		action.setEnabled(true);		
		getActionMap().put("new.instance", action);
		
		
				
		JPanel bottom=new JPanel(new FlowLayout(FlowLayout.TRAILING));
		bottom.add(new JButton(getActionMap().get("view.instance")));
		bottom.add(new JButton(getActionMap().get("new.instance")));
		pane.add(bottom,BorderLayout.SOUTH);
		
		

		
		
		this.instanceTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.instanceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
			{
			
			@Override
			public void valueChanged(ListSelectionEvent evt)
				{
				getActionMap().get("view.instance").setEnabled(instanceTable.getSelectedRowCount()!=0);
				}
			});
		

		}
	
	private void showSelectedInstance()
		{
		int row=instanceTable.getSelectedRow();
		if(row==-1) return;
		Resource x= instanceListTableModel.getElementAt(row);
		for(JInternalFrame f:getDesktopPane().getAllFrames())
			{
			if(!(f instanceof InstanceViewInternalFrame)) continue;
			if(InstanceViewInternalFrame.class.cast(f).getSubject().equals(x))
				{
				f.moveToFront();
				return;
				}
			}
		InstanceViewInternalFrame ivif=new InstanceViewInternalFrame(getRDFEditorFrame(), ontClass, x);
		getDesktopPane().add(ivif);
		ivif.setVisible(true);	
		}
	
	private void createInstanceDialog()
		{		
		InstanceCreator dlg=InstanceCreator.create(this,getRDFEditorFrame(),getOntClass());
		if(dlg==null) return;
		dlg.setVisible(true);
		fireModelChanged();
		}

	
	public Resource getOntClass()
		{
		return ontClass;
		}
	
	}
