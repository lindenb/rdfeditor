package com.github.lindenb.rdfeditor.swing.table;

import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;


@SuppressWarnings("serial")
public abstract class AbstractGenericTableModel<T> extends AbstractTableModel
	{
	protected static final Logger LOG = Logger.getLogger("com.github.lindenb");
	private List<T> rows=new Vector<T>();
	
	public AbstractGenericTableModel()
		{
		}
	
	public AbstractGenericTableModel(List<T> rows)
		{
		this.rows=new Vector<T>(rows);
		}
	
	@Override
	public abstract Class<?> getColumnClass(int col);
	
	@Override
	public abstract String getColumnName(int col);
	
	public List<T> getRows()
		{
		return rows;
		}
	
	public void setRows(List<T> rows)
		{
		this.rows = new Vector<T>(rows);
		fireTableDataChanged();
		}
	
	public void setElementAt(int row,T o)
		{
		this.rows.set(row, o);
		this.fireTableRowsUpdated(row, row);
		}
	
	public T getElementAt(int i)
		{
		return this.getRows().get(i);
		}
	
	public abstract Object getValueOf(final T o, int col);
	
	@Override
	public final Object getValueAt(int row, int col)
		{
		T rsrc=getElementAt(row);
		return rsrc==null?null:getValueOf(rsrc,col);
		}
	
	@Override
	public int getRowCount()
		{
		return rows.size();
		}
	@Override
	public boolean isCellEditable(int arg0, int arg1)
		{
		return false;
		}
	public void clear()
		{
		this.rows.clear();
		fireTableDataChanged();
		}		
	}
