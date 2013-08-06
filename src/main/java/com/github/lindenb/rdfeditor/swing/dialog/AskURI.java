package com.github.lindenb.rdfeditor.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

@SuppressWarnings("serial")
public class AskURI extends AskDialog
	{
	private Model rdfDataStore;
	private JTextField textField;
	public AskURI(Component owner,
			Model rdfDataStore,
			Resource ontClass
			)
		{
		super(owner);
		this.rdfDataStore=rdfDataStore;
		String defaultUri="";
		int i=0;
		do
			{
			String localName=ontClass.getLocalName();
			if(localName==null) localName="class";
			defaultUri=String.format("urn:%s:%05d",localName,++i);
			} while(this.rdfDataStore.containsResource(ResourceFactory.createResource(defaultUri)));
		
		this.textField=new JTextField(defaultUri,20);
		getContentPane().add(this.textField,BorderLayout.CENTER);
		this.textField.getDocument().addDocumentListener(new DocumentListener()
			{
			@Override
			public void removeUpdate(DocumentEvent e) {	
				validateURI();
				}
			
			@Override
			public void insertUpdate(DocumentEvent e) {	
				validateURI();
				}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				validateURI();
				}
			});
		this.textField.addActionListener(new ActionListener()
			{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(isValidURI()) doOKAction();
				}
			});
		validateURI();
		}
	
	public Resource getResource()
		{
		if(this.textField.getText().trim().isEmpty()) return null;
		String uriStr=this.textField.getText().trim();
		URI uri=null;
		try {
			uri=new URI(uriStr);
			if(!uri.isAbsolute())
				{
				return null;
				}
			if(!uri.isOpaque())
				{
				return null;
				}
			if(this.rdfDataStore.containsResource(ResourceFactory.createResource(uriStr)))
				{
				return null;
				}
			return ResourceFactory.createResource(uriStr);
			}
		catch (Exception e)
			{
			return null;
			}
		}	
	
	private boolean isValidURI()
		{
		return getResource()!=null;
		}
	
	private void validateURI()
		{
		super.okAction.setEnabled(isValidURI());
		}
	
}
