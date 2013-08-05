package com.github.lindenb.rdfeditor.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.filechooser.FileFilter;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class SelectFileComponent extends JPanel
	{
	private FileFilter fileFilter;
	private File selFile;
	private JTextField tf;
	private int fileSelectionMode=JFileChooser.FILES_AND_DIRECTORIES;
	private AbstractAction selectAction;
	public static final String FILE_CHANGED_PROPERTY="com.github.lindenb.rdfeditor.swing.SelectFileComponent.File";
	
	
	public SelectFileComponent()
		{
		super(new BorderLayout());
		this.tf=new JTextField(20);
		this.tf.setEditable(false);
		this.add(tf,BorderLayout.CENTER);
		this.add(new JButton(selectAction=new AbstractAction("Select...")
			{
			@Override
			public void actionPerformed(ActionEvent arg0)
				{
				selectFile();
				}
			}),BorderLayout.EAST);
		}
	
	public int getFileSelectionMode()
		{
		return fileSelectionMode;
		}
	
	public void setFileSelectionMode(int fileSelectionMode)
		{
		this.fileSelectionMode = fileSelectionMode;
		}
	
	
	private void selectFile()
		{
		JFileChooser fc=null;
		if(this.selFile==null || !this.selFile.exists())
			{
			fc=new JFileChooser();
			}
		else if(this.selFile.isDirectory())
			{
			fc=new JFileChooser(this.selFile);
			}
		else
			{
			fc=new JFileChooser(this.selFile.getParentFile());
			}
		if(this.selFile!=null && selFile.isFile())
			{
			fc.setSelectedFile(selFile);
			}
		fc.setFileSelectionMode(getFileSelectionMode());
		FileFilter fileFilter=getFileFilter();
		if(fileFilter!=null) fc.setFileFilter(fileFilter);
		if(fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		File f=fc.getSelectedFile();
		if(!f.exists()) return;
		setFile(f);
		}
	public FileFilter getFileFilter()
		{
		return this.fileFilter;
		}
	
	public void setFileFilter(FileFilter ff)
		{
		this.fileFilter=ff;
		}
	
	public void setFile(File f)
		{
		File oldFile=this.selFile;
		if(f!=null) 
			{
			tf.setText(f.toString());
			tf.setCaretPosition(0);
			tf.setToolTipText(f.toString());
			}
		else
			{
			tf.setText("");
			tf.setToolTipText("");
			}
		this.selFile=f;
		this.firePropertyChange(FILE_CHANGED_PROPERTY, oldFile, this.selFile);
		}
	
	public File getFile()
		{
		return this.selFile;
		}
	@Override
	public void setEnabled(boolean enabled)
		{
		super.setEnabled(enabled);
		tf.setEnabled(enabled);
		selectAction.setEnabled(enabled);
		}
	}
