package com.github.lindenb.rdfeditor.swing.iframe;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.apache.log4j.Logger;

import com.github.lindenb.rdfeditor.RDFEditorFrame;
import com.github.lindenb.rdfeditor.rdf.SchemaAndModel;
import com.hp.hpl.jena.rdf.model.Model;

/** abstract internal frame for the main JFrame */
public class AbstractInternalFrame
	extends JInternalFrame
	implements SchemaAndModel
	{
	protected static final Logger LOG = Logger.getLogger("com.github.lindenb");

	private static final long serialVersionUID = 1L;
	
	private RDFEditorFrame ownerFrame;
	/** associated schema */
	
	/**
	 * Constructor with schema and model
	 * @param schema
	 * @param rdfModel
	 */
	protected AbstractInternalFrame(RDFEditorFrame frame)
		{
		this.ownerFrame=frame;
		setClosable(false);
		setResizable(true);
		setIconifiable(true);
		setMaximizable(true);
		this.addInternalFrameListener(new InternalFrameAdapter()
			{
			@Override
			public void internalFrameOpened(InternalFrameEvent evt)
				{
				int w=getDesktopPane().getWidth();
				int h=getDesktopPane().getHeight();
				setBounds(
						(int)(Math.random()*w*0.2),
						(int)(Math.random()*h*0.2),
						(int)(w*0.8),
						(int)(h*0.8)
						);
				removeInternalFrameListener(this);
				}
			});
		}
	
	public RDFEditorFrame getRDFEditorFrame()
		{
		return ownerFrame;
		}
	
	@Override
	public Model getRDFDataStore() {
		return getRDFEditorFrame().getRDFDataStore();
		}
	
	@Override
	public Model getRDFSchema() {
		return getRDFEditorFrame().getRDFSchema();
		}
	
	public void reloadModel()
		{
		
		}
	@Override
	public void fireModelChanged()
		{
		getRDFEditorFrame().fireModelChanged();
		}		
	}
