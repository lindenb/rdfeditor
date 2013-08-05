package com.github.lindenb.rdfeditor.swing.text;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


@SuppressWarnings("serial")
public class RDFDataTypeTextComponent
	extends JPanel
	{
	/** property will be fired as boolean if text component match the RDF datatype */
	public static final String RDFDATATYPE_VALID_PROPERTY="rdf.datatype.valid.property";
	private JTextComponent tComp;
	private RDFDatatype rdfDatatype;
	private boolean old_is_valid=false;
	
	public RDFDataTypeTextComponent()
		{
		this(XSDDatatype.XSDstring);
		}
	
	public RDFDataTypeTextComponent(RDFDatatype rdfDatatype)
		{
		this(rdfDatatype,new JTextField(20));
		}
	
	public RDFDataTypeTextComponent(RDFDatatype rdfDatatype,JTextComponent tComp)
		{
		super(new BorderLayout());
		this.tComp=tComp;
		this.rdfDatatype=(rdfDatatype==null?XSDDatatype.XSDstring:rdfDatatype);
		this.add(tComp,BorderLayout.CENTER);
		this.tComp.getDocument().addDocumentListener(new DocumentListener()
			{
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				validateTextInput();
				}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				validateTextInput();
				};
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				validateTextInput();
				}
			});
		validateTextInput();
		}
	public RDFDatatype getRdfDatatype()
		{
		return rdfDatatype;
		}
	
	protected JTextComponent getTextComponent()
		{
		return tComp;
		}
	
	public String getText()
		{
		return getTextComponent().getText();	
		}
	public void setText(String text)
		{
		getTextComponent().setText(text);
		}	
	
	/** Test whether the given string is a legal lexical form of this datatype. */
	public boolean isValidLiteral()
		{
		return getRdfDatatype().isValid(getText());
		}
	
	/** return a literal with the correct datatype or null if the textcomponent cannot be cast to the datatype */
	public Literal getLiteral()
		{
		return isValidLiteral()?ResourceFactory.createTypedLiteral(getText(), getRdfDatatype()):null;
		}
	
	protected void validateTextInput()
		{
		boolean old_valid=this.old_is_valid;
		boolean is_valid=isValidLiteral();
		this.old_is_valid=is_valid;
		
		this.setBorder(BorderFactory.createLineBorder(is_valid?Color.BLACK:Color.RED, 1));
		this.getTextComponent().setToolTipText(is_valid?"":"Enter a valid "+getRdfDatatype().getURI());

		if(old_valid!=is_valid)
			{
			firePropertyChange(RDFDATATYPE_VALID_PROPERTY, old_valid, is_valid);
			}
	
		
		}
	
	}
