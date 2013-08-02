package com.github.lindenb.rdfeditor.swing.table.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.github.lindenb.rdfeditor.swing.list.PropertiesListModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

@SuppressWarnings(value={"unchecked","serial","rawtypes"})
public class PropertyChooserCellEditor  
	extends AbstractCellEditor implements TableCellEditor
	{
	private PropertiesListModel propertiesListModel;
	private Model schema;
	private JList jlist;
	private JButton jbutton;
	private boolean userCanceled=true;
	
	public PropertyChooserCellEditor(Model schema,Resource ontClass)
		{
		this.schema=schema;
		this.propertiesListModel=new PropertiesListModel(schema, ontClass);
		this.jlist=new JList(propertiesListModel);
		this.jbutton=new JButton();
		this.jbutton.setBorderPainted(false);
		this.jbutton.addActionListener(new ActionListener()
				{
				@Override
				public void actionPerformed(ActionEvent evt)
					{
					userCanceled=false;
					JPanel pane=new JPanel(new BorderLayout(5,5));
					pane.add(new JScrollPane(jlist),BorderLayout.CENTER);
					
					JOptionPane jop=new JOptionPane(pane,JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null);
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
	
	public Model getSchema()
		{
		return this.schema;
		}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
	boolean isSelected, int row, int column)
		{
		jlist.setSelectedValue(value,true);
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
		return jlist.getSelectedValue();
	}

	}
