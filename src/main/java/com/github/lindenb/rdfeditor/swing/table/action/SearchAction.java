package com.github.lindenb.rdfeditor.swing.table.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public class SearchAction extends AbstractAction
	{
	public static String LAST_QUERY=null;
	private JTable table;
	private boolean searchAgain;
	public SearchAction(JTable table,String label,boolean searchAgain)
		{
		super(label);
		this.table=table;
		this.searchAgain=searchAgain;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				searchAgain?KeyEvent.VK_G:KeyEvent.VK_F, ActionEvent.CTRL_MASK)
				);
		}

	public SearchAction(JTable table,boolean searchAgain) {
		this(table,(searchAgain?"Re-Search":"Search..."),searchAgain);
		}

	@Override
	public void actionPerformed(ActionEvent ae)
		{
		if(table.getRowCount()==0) return;
		if(!searchAgain || LAST_QUERY==null)
			{
			String s=JOptionPane.showInputDialog(this.table,"Search");
			if(s==null) return;
			LAST_QUERY=s;
			}
		String lowerQ=LAST_QUERY.toLowerCase();
		int start=table.getSelectedRow();
		if(start==-1) start=0;
		int curr=start+1;
		while(curr!=start)
			{
			if(curr>=table.getRowCount()) curr=0;
			for(int c=0;c < table.getColumnCount();++c)
				{
				Object v=table.getValueAt(curr, c);
				if(v==null) continue;
				if(v.toString().toLowerCase().contains(lowerQ))
					{
					table.getSelectionModel().setSelectionInterval(curr, curr);
					table.scrollRectToVisible(table.getCellRect(curr, c, true));
					return;
					}
				}
			++curr;
			}
		}

}
