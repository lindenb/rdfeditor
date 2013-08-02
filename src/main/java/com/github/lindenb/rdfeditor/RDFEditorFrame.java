package com.github.lindenb.rdfeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.github.lindenb.rdfeditor.rdf.SchemaAndModel;
import com.github.lindenb.rdfeditor.swing.iframe.AbstractInternalFrame;
import com.github.lindenb.rdfeditor.swing.iframe.OntClassInternalFrame;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;




@SuppressWarnings("serial")
public class RDFEditorFrame
	extends JFrame
	implements SchemaAndModel
	{
	private static final Logger LOG = Logger.getLogger("com.github.lindenb");
	private Model schema;
	private Model model;
	private JDesktopPane desktopPane;
	private File saveAsFile;
	private boolean rdfStoreDirtyFlag=false;
	private ActionMap actionMap=new ActionMap();
	private SwingAppender swingAppender=null;
	private JTextField logField=null;
	
	private class SwingAppender
		extends AppenderSkeleton
		{
		
		@Override
		protected void append(LoggingEvent evt)
			{
			if(evt==null || evt.getRenderedMessage()==null) return;
			logField.setText(evt.getRenderedMessage());
			logField.setCaretPosition(0);
			}
		@Override
		public void close() {
			
			}
		
		@Override
		public boolean requiresLayout() {
			return false;
			}
		}
	
	
	
	private RDFEditorFrame(File saveAsF,final Model model,final Model schema)
		{
		super("RDFEditorFrame");
		this.model=model;
		this.schema=schema;
		this.saveAsFile=saveAsF;
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowClosing(WindowEvent arg0)
				{
				doMenuQuit();
				}
			});
		addWindowListener(new WindowAdapter()
			{
			@Override
			public void windowOpened(WindowEvent arg0) {
				LOG.info("Creating OntClassInternalFrame");
				OntClassInternalFrame f=new OntClassInternalFrame(RDFEditorFrame.this);
				desktopPane.add(f);
				f.setVisible(true);
				removeWindowListener(this);
				}	
			});
		JMenuBar bar=new JMenuBar();
		setJMenuBar(bar);
		AbstractAction action=new AbstractAction("Save As...") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doMenuSaveAs();
				}
			};
		this.actionMap.put("file.saveas", action);
		
		action=new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doMenuSave(saveAsFile);
				}
			};
		this.actionMap.put("file.save", action);
		
		action=new AbstractAction("Quit") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doMenuQuit();
				}
			};
		this.actionMap.put("file.quit", action);
			
		JMenu menu=new JMenu("File");
		bar.add(menu);
		menu.add(this.actionMap.get("file.saveas"));
		menu.add(this.actionMap.get("file.save"));
		menu.add(new JSeparator());
		menu.add(this.actionMap.get("file.quit"));
		
		JPanel mainPane=new JPanel(new BorderLayout(5,5));
		mainPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		setContentPane(mainPane);
		this.desktopPane=new JDesktopPane();
		mainPane.add(this.desktopPane,BorderLayout.CENTER);
		
		JToolBar toolBar=new JToolBar();
		mainPane.add(toolBar,BorderLayout.NORTH);
		toolBar.add(this.actionMap.get("file.saveas"));
		toolBar.add(this.actionMap.get("file.save"));
		
		this.logField=new JTextField(100);
		this.logField.setEditable(false);
		mainPane.add(logField,BorderLayout.SOUTH);
		
		this.swingAppender=new SwingAppender();
		}
	
	
	public boolean doMenuSaveAs()
		{
		JFileChooser fc;
		if(this.saveAsFile!=null && this.saveAsFile.exists() && this.saveAsFile.isFile())
			{
			fc=new JFileChooser(this.saveAsFile.getParentFile());
			}
		else
			{
			fc=new JFileChooser();
			}
		if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return false;
		File f2=fc.getSelectedFile();
		if(f2.exists() && JOptionPane.showConfirmDialog(this, "File Exists Overwrite ?","Overwrite",JOptionPane.WARNING_MESSAGE,JOptionPane.OK_CANCEL_OPTION,null)!=JOptionPane.OK_OPTION)
			{
			return false;
			}
		return doMenuSave(f2);
		}
	
	public boolean doMenuSave(File f)
		{
		if(f==null) return doMenuSaveAs();
		FileWriter fw=null;
		try
			{
			fw=new FileWriter(f);
			this.model.write(fw, "en",f.toURI().toString());
			fw.flush();
			fw.close();
			this.saveAsFile=f;
			}
		catch(IOException err)
			{
			JOptionPane.showMessageDialog(this, ""+err.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			return false;
			}
		finally
			{
			
			}
		
		return true;
		}
	public void doMenuQuit()
		{	
		if(this.rdfStoreDirtyFlag)
			{
			doMenuSaveAs();
			}
		this.setVisible(false);
		this.dispose();
		if(swingAppender!=null) LOG.removeAppender(swingAppender);
		swingAppender=null;
		}
	
	
	@Override
	/** get the associated RDF datastore model */
	public Model getRDFDataStore()
		{
		return this.model;
		}
	/** get the associated RDFS schema */
	@Override
	public Model getRDFSchema()
		{
		return this.schema;
		}
	
	@Override
	/** tell the internal frames the model has changed, set the dirty flag to ON */
	public void fireModelChanged()
		{
		LOG.debug("fireModelChanged");
		for(JInternalFrame jif:this.desktopPane.getAllFrames())
			{
			if(!(jif instanceof AbstractInternalFrame)) continue;
			AbstractInternalFrame.class.cast(jif).reloadModel();
			}
		this.rdfStoreDirtyFlag=true;
		}
	
	public static void main(String[] args)
		throws Exception
		{
		LOG.setLevel(Level.INFO);
		
		args=new String[]{"-s",
				"file:///home/lindenb/src/yardfapp/schema.rdf",
				"/home/lindenb/src/yardfapp/data.rdf"
				};
		String schemaURI=null;
		int optind=0;
		while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				return;
				}
			else if(args[optind].equals("-s") && optind+1 < args.length)
				{
				schemaURI=args[++optind];
				}
			else if(args[optind].equals("--"))
				{
				optind++;
				break;
				}
			else if(args[optind].startsWith("-"))
				{
				System.err.println("Unnown option: "+args[optind]);
				return;
				}
			else
				{
				break;
				}
			++optind;
			}
		if(schemaURI==null)
			{
			System.err.println("Schema URI missing");
			return;
			}
		File rdfStoreFile=null;
		if(optind==args.length)
			{
			rdfStoreFile=null;
			}
		else if(optind+1==args.length)
			{
			rdfStoreFile=new File(args[optind]);
			}
		else
			{
			System.err.println("Illegal number of arguments");
			return;
			}
		
		Model schema=ModelFactory.createDefaultModel();
		schema.read(schemaURI);
		
			
		
		Model model=ModelFactory.createDefaultModel();
		if(rdfStoreFile!=null)
			{
			FileReader fr=new FileReader(rdfStoreFile);
			model.read(fr, rdfStoreFile.toURI().toString());
			fr.close();
			}
		
		
		final RDFEditorFrame frame=new RDFEditorFrame(rdfStoreFile,model,schema);
	
		Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(50,50,screen.width-100,screen.height-100);
		try
			{
			SwingUtilities.invokeAndWait(new Runnable()
				{
				@Override
				public void run()
					{
					frame.setVisible(true);
					}
				});
			}
		catch(Exception err)
			{
			err.printStackTrace();
			}
		}

}
