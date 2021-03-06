package com.github.lindenb.rdfeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
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
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.github.lindenb.rdfeditor.rdf.JavaDataType;
import com.github.lindenb.rdfeditor.rdf.SchemaAndModel;
import com.github.lindenb.rdfeditor.swing.SelectFileComponent;
import com.github.lindenb.rdfeditor.swing.dialog.AskDialog;
import com.github.lindenb.rdfeditor.swing.iframe.AbstractInternalFrame;
import com.github.lindenb.rdfeditor.swing.iframe.OntClassInternalFrame;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

@SuppressWarnings("serial")
class StartupDialog
	extends AskDialog
	{
	SelectFileComponent selectSchema;
	SelectFileComponent selectDataStore;
	JCheckBox createSchema;
	public StartupDialog(Component c)
		{
		super(c);
		setTitle("Startup");
		setUndecorated(false);
		JPanel pane=new JPanel(new GridLayout(0, 1, 5, 5));
		this.selectSchema =new SelectFileComponent();
		this.selectSchema.setBorder(BorderFactory.createTitledBorder("Schema"));
		this.selectDataStore =new SelectFileComponent();
		this.selectDataStore.setBorder(BorderFactory.createTitledBorder("DataStore"));
		pane.add(this.selectSchema);
		pane.add(this.selectDataStore);
		
		pane.add(this.createSchema=new JCheckBox(".. or create a new Schema"));
		
		PropertyChangeListener change=new PropertyChangeListener()
			{

			@Override
			public void propertyChange(PropertyChangeEvent evt)
				{
				doDialogValidation();
				}
			};
			FileFilter ff=new FileFilter()
				{
				@Override
				public String getDescription()
					{
					return "RDF/XML files";
					}
				
				@Override
				public boolean accept(File f)
					{
					return f.isDirectory() || (f.isFile() && f.canRead() &&
							(f.getName().toLowerCase().endsWith(".xml") || f.getName().toLowerCase().endsWith(".rdf")));
					}
				};
		this.selectSchema.setFileFilter(ff);
		this.selectDataStore.setFileFilter(ff);
		this.selectDataStore.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.selectDataStore.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		this.selectSchema.addPropertyChangeListener(SelectFileComponent.FILE_CHANGED_PROPERTY,change);
		this.selectDataStore.addPropertyChangeListener(SelectFileComponent.FILE_CHANGED_PROPERTY,change);
		this.createSchema.addActionListener(new ActionListener()
				{
				@Override
				public void actionPerformed(ActionEvent e)
					{
					doDialogValidation();
					selectDataStore.setEnabled(!createSchema.isSelected());
					selectSchema.setEnabled(!createSchema.isSelected());
					}
			});
		Preferences prefs=Preferences.userNodeForPackage(RDFEditorFrame.class);
		String v=prefs.get("schema.file", null);
		if(v!=null)
			{
			try
				{
				File f=new File(v);
				if(f.exists()) selectSchema.setFile(f);
				}
			catch (Exception e)
				{
				}
			}
		v=prefs.get("datastore.file", null);
		if(v!=null)
			{
			try
				{
				File f=new File(v);
				if(f.exists()) selectDataStore.setFile(f);
				}
			catch (Exception e)
				{
				}
			}
		getContentPane().add(pane,BorderLayout.CENTER);
		}
	
	@Override
	public void doOKAction()
		{
		if(!this.createSchema.isSelected())
			{
			Preferences prefs=Preferences.userNodeForPackage(RDFEditorFrame.class);
			
			File f=selectSchema.getFile();
			if(f!=null)
				{
				prefs.put("schema.file", f.getPath());
				}
			f=selectDataStore.getFile();
			if(f!=null)
				{
				prefs.put("datastore.file", f.getPath());
				}
			try	{
				prefs.flush();
				}
			catch(Exception err)
				{
				
				}
			}
		
		super.doOKAction();
		}
	
	private void doDialogValidation()
		{
		super.okAction.setEnabled(
				(
				this.selectSchema.getFile()!=null
				//&& this.selectDataStore.getFile()!=null 
				) ||
				
				createSchema.isSelected()
				);
		}
	}

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
		public void close()
			{
			
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
		
		
		action=new AbstractAction("Export as ONE RDF") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doMenuExportAsOneRDF();
				}
			};
		this.actionMap.put("file.export.one.rdf", action);
		
		
		
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
		menu.add(this.actionMap.get("file.export.one.rdf"));
		
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
	
	
	private File promptFileToSave(File defaultFile,FileFilter filter)
		{
		JFileChooser fc;
		if(defaultFile!=null && defaultFile.exists() && defaultFile.isFile())
			{
			fc=new JFileChooser(defaultFile.getParentFile());
			fc.setSelectedFile(defaultFile);
			}
		else if(defaultFile!=null && defaultFile.exists() && defaultFile.isDirectory())
			{
			fc=new JFileChooser(defaultFile);
			}
		else if(defaultFile!=null && defaultFile.getParentFile()!=null && defaultFile.getParentFile().exists())
			{
			fc=new JFileChooser(defaultFile.getParentFile());
			}
		else
			{
			fc=new JFileChooser();
			}
		if(filter!=null) fc.setFileFilter(filter);
		if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return null;
		File f2=fc.getSelectedFile();
		if(f2.exists() && JOptionPane.showConfirmDialog(
				this,
				"File Exists Overwrite ?",
				"Overwrite",
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				null)!=JOptionPane.OK_OPTION)
			{
			return null;
			}
		return f2;
		}
	
	public boolean doMenuSaveAs()
		{
		File f2=promptFileToSave(this.saveAsFile,null);
		if(f2==null) return false;
		return doMenuSave(f2);
		}
	
	public boolean doMenuExportAsOneRDF()
		{
		File f2=promptFileToSave(this.saveAsFile,null);
		if(f2==null) return false;
		Model unified=ModelFactory.createUnion(this.model, this.schema);
		boolean b=saveModelToFile(unified,f2);
		unified.close();
		return b;
		}

	private static boolean saveModelToFile(Model model,File f)
		{
		FileWriter fw=null;
		try
			{
			fw=new FileWriter(f);
			RDFWriter rdfw= model.getWriter("RDF/XML");
			rdfw.setProperty("showDoctypeDeclaration", true);
			rdfw.setProperty("showXmlDeclaration", true);
			rdfw.setErrorHandler(new RDFErrorHandler()
					{
					
					@Override
					public void warning(Exception t) {
						LOG.warn("warning", t);
					}
					
					@Override
					public void fatalError(Exception t) {
						LOG.fatal("fatal", t);
						
					}
					
					@Override
					public void error(Exception t) {
						LOG.error("error", t);
						
					}
				});
			rdfw.write(model, fw,f.toURI().toString());
			fw.flush();
			fw.close();
			}
		catch(Exception err)
			{
			LOG.error("error", err);
			return false;
			}
		return true;
		}
	
	public boolean doMenuSave(File f)
		{
		if(f==null) return doMenuSaveAs();
		
		if(!saveModelToFile(this.model, f)) return false;
			
		this.saveAsFile=f;
		this.rdfStoreDirtyFlag=false;
		return true;
		}
	
	/** invoked when the window is closing */
	private void doMenuQuit()
		{	
		if(this.rdfStoreDirtyFlag)
			{
			if(JOptionPane.showConfirmDialog(this,
					"Model was not saved. Do you wish to save it before Closing ?",
					"Save model ?",
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.YES_NO_OPTION, null)!=JOptionPane.NO_OPTION)
				{
				doMenuSave(this.saveAsFile);
				}
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
	
	private static Model createDefaultSchema()
		{
		Model m=ModelFactory.createDefaultModel();
		m.setNsPrefix("rdf",RDF.getURI());
		m.setNsPrefix("rdfs",RDFS.getURI());
		m.setNsPrefix("owl",OWL.getURI());
		m.setNsPrefix("dc",DC.getURI());
		m.setNsPrefix("xsd",XSD.getURI());
		m.setNsPrefix("dcterms",DCTerms.getURI());
		
		//class is rdfs:Class
		Resource r1=m.createResource(RDFS.Class.getURI());
		m.add(r1,RDF.type,RDFS.Class);
		m.add(r1, RDFS.label, "Class");
		m.add(r1, RDFS.comment, "a rdfs:Class");
		
		//class is rdfs:Label
		Resource r2=m.createResource(RDF.Property.getURI());
		m.add(r2,RDF.type,RDFS.Class);
		m.add(r2, RDFS.label, "Property");
		m.add(r2, RDFS.comment, "a rdf:Property");
		
		//property rdfs:label
		Resource r=m.createResource(RDFS.label.getURI());
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "label");
		m.add(r, RDFS.comment, "a rdfs:label");
		m.add(r, RDFS.domain,RDFS.Class);
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,XSD.xstring);
		m.add(r, OWL.cardinality,m.createTypedLiteral(1));
		
		//property rdfs:comment
		r=m.createResource(RDFS.comment.getURI());
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "comment");
		m.add(r, RDFS.comment, "a rdfs:comment");
		m.add(r, RDFS.domain,RDFS.Class);
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,XSD.xstring);
		m.add(r, OWL.minCardinality,m.createTypedLiteral(0));
		m.add(r, OWL.maxCardinality,m.createTypedLiteral(1));
		
		//property owl:cardinality
		r=m.createResource(OWL.minCardinality.getURI());
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "minCardinality");
		m.add(r, RDFS.comment, "min cardinality");
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,XSD.nonNegativeInteger);
		m.add(r, OWL.minCardinality,m.createTypedLiteral(0));
		m.add(r, OWL.maxCardinality,m.createTypedLiteral(1));
		
		r=m.createResource(OWL.maxCardinality.getURI());
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "maxCardinality");
		m.add(r, RDFS.comment, "max cardinality");
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,XSD.nonNegativeInteger);
		m.add(r, OWL.minCardinality,m.createTypedLiteral(0));
		m.add(r, OWL.maxCardinality,m.createTypedLiteral(1));
	
		r=m.createResource(OWL.cardinality.getURI());
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "cardinality");
		m.add(r, RDFS.comment, "cardinality");
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,XSD.nonNegativeInteger);
		m.add(r, OWL.minCardinality,m.createTypedLiteral(0));
		m.add(r, OWL.maxCardinality,m.createTypedLiteral(1));

		r=m.createResource(XSD.getURI()+"pattern");
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "pattern");
		m.add(r, RDFS.comment, "regular expression pattern");
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,XSD.xstring);
		m.add(r, OWL.minCardinality,m.createTypedLiteral(0));
		m.add(r, OWL.maxCardinality,m.createTypedLiteral(1));

		r=m.createResource(XSD.getURI()+"minLength");
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "minLength");
		m.add(r, RDFS.comment, "min-length");
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,XSD.nonNegativeInteger);
		m.add(r, OWL.minCardinality,m.createTypedLiteral(0));
		m.add(r, OWL.maxCardinality,m.createTypedLiteral(1));
		
		r=m.createResource(XSD.getURI()+"maxLength");
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "maxLength");
		m.add(r, RDFS.comment, "max-length");
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,XSD.nonNegativeInteger);
		m.add(r, OWL.minCardinality,m.createTypedLiteral(0));
		m.add(r, OWL.maxCardinality,m.createTypedLiteral(1));
		
		r=RDFS.range;
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "range");
		m.add(r, RDFS.comment, "rdfs:range");
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,RDFS.Resource);
		m.add(r, OWL.cardinality,m.createTypedLiteral(1));

		
		r=RDFS.domain;
		m.add(r,RDF.type,RDF.Property);
		m.add(r, RDFS.label, "domain");
		m.add(r, RDFS.comment, "rdfs:domain");
		m.add(r, RDFS.domain,RDF.Property);
		m.add(r, RDFS.range,RDFS.Class);
		m.add(r, OWL.cardinality,m.createTypedLiteral(1));

		
		return m;
		}
	
	public static void main(String[] args)
		throws Exception
		{
		LOG.setLevel(Level.INFO);
		
		JavaDataType.loadJavaTypes(TypeMapper.getInstance());
		Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
		
		File schemaFile=null;
		File rdfStoreFile=null;
		
		if(args.length==0)
			{
			LOG.info("invoking GUI dialog startup");
			StartupDialog startup=new StartupDialog(null);
			startup.pack();
			Dimension d=startup.getPreferredSize();
			startup.setBounds((screen.width-d.width)/2, (screen.height-d.height)/2, d.width, d.height);

			startup.setVisible(true);
			if(startup.getReturnStatus()==StartupDialog.CANCEL_OPTION) return;
			schemaFile=startup.selectSchema.getFile();
			}
		else
			{
			LOG.info("parsing cmd line arguments.");
			int optind=0;
			while(optind<args.length)
				{
				if(args[optind].equals("-h"))
					{
					return;
					}
				else if(args[optind].equals("-s") && optind+1 < args.length)
					{
					schemaFile=new File(args[++optind]);
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
			}
		
		Model schema=null;
		
		if(schemaFile!=null)
			{
			LOG.info("loading schema: "+schemaFile);
			schema=ModelFactory.createDefaultModel();
			FileReader r=new FileReader(schemaFile);
			schema.read(r,schemaFile.toURI().toString());
			r.close();
			}
		else
			{
			LOG.info("using the default schema");
			schema=RDFEditorFrame.createDefaultSchema();
			}
			
		
		Model model=ModelFactory.createDefaultModel();
		if(rdfStoreFile!=null && schemaFile!=null)
			{
			LOG.info("loading dataStore: "+schemaFile);
			FileReader fr=new FileReader(rdfStoreFile);
			model.read(fr, rdfStoreFile.toURI().toString());
			fr.close();
			}
		else
			{
			model.setNsPrefixes(schema.getNsPrefixMap());
			}
		
		
		final RDFEditorFrame frame=new RDFEditorFrame(rdfStoreFile,model,schema);
		if(schemaFile==null)
			{
			frame.addWindowListener(new WindowAdapter()
				{
				@Override
					public void windowOpened(WindowEvent arg0) {
					
					AbstractInternalFrame iframe=new AbstractInternalFrame(frame)
						{
						
						};
					JTextPane txtPane=new JTextPane();
					txtPane.setContentType("text/html");
					txtPane.setText(
							"<html><body>"+
							"<h1>Default Schema</h1>"+
							"You're using the default schema."+
							"</body></html>"
							);
					txtPane.setEditable(false);
					iframe.setClosable(true);
					iframe.setContentPane(txtPane);
					frame.desktopPane.add(iframe);
					iframe.setVisible(true);
					}
				});
			}
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
