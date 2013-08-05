package com.github.lindenb.rdfeditor.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class AskDialog
	extends JDialog
	{
	public static final int OK_OPTION=JOptionPane.OK_OPTION;
	public static final int CANCEL_OPTION=JOptionPane.CANCEL_OPTION;
	private static final Logger LOG = Logger.getLogger("com.github.lindenb");
	private int returnValue=JOptionPane.CANCEL_OPTION;
	protected AbstractAction okAction=null;	
	protected JPanel bottomPane;
	protected JPanel contentPane;
	
	
	public AskDialog(Component owner)
		{
		super(
				owner==null?null:SwingUtilities.getWindowAncestor( owner),
				ModalityType.APPLICATION_MODAL
				);
		
		setUndecorated(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.contentPane = new JPanel(new BorderLayout(5,5));
		this.contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setContentPane(this.contentPane);
		
		this.bottomPane =new JPanel(new FlowLayout(FlowLayout.TRAILING));
		this.contentPane.add(this.bottomPane,BorderLayout.SOUTH);
		this.bottomPane.add(new JButton(okAction=new AbstractAction("OK")
			{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doOKAction();
				}
			}));
		this.okAction.setEnabled(false);
		
		this.bottomPane.add(new JButton(new AbstractAction("Cancel")
			{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCancelAction();
				}
			}));

		
		}
	
	public void doOKAction()
		{
		LOG.info("OK ACTION");
		returnValue=AskDialog.OK_OPTION;
		this.setVisible(false);
		this.dispose();
		}
	
	public void doCancelAction()
		{
		LOG.info("CANCEL ACTION");
		returnValue=AskDialog.CANCEL_OPTION;
		this.setVisible(false);
		this.dispose();
		}
	
	
	public int getReturnStatus()
		{
		return returnValue;
		}

	}
