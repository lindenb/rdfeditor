package com.github.lindenb.rdfeditor.rdf;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFWriter;

public class GexfWriter implements RDFWriter {
	private RDFErrorHandler handler;
	private XMLStreamWriter w;
	@Override
	public RDFErrorHandler setErrorHandler(RDFErrorHandler handler)
		{
		RDFErrorHandler prev=handler;
		this.handler=handler;
		return prev;
		}

	@Override
	public Object setProperty(String arg0, Object arg1) {
		return null;
		}

	@Override
	public void write(Model model, Writer w, String base)
		{
		XMLOutputFactory xmlfactory= XMLOutputFactory.newInstance();
		try
			{
			this.w= xmlfactory.createXMLStreamWriter(w);
			this.w.writeStartDocument();
			write(model);
			this.w.writeEndDocument();
			this.w.flush();
			this.w=null;
			}
		catch(XMLStreamException err)
			{
			if(handler!=null) handler.error(err);
			}
		
		
		}

	@Override
	public void write(Model model, OutputStream out, String base)
		{
		XMLOutputFactory xmlfactory= XMLOutputFactory.newInstance();
		try
			{
			this.w= xmlfactory.createXMLStreamWriter(out,"UTF-8");
			this.w.writeStartDocument();
			write(model);
			this.w.writeEndDocument();
			this.w.flush();
			this.w=null;
			}
		catch(XMLStreamException err)
			{
			if(handler!=null) handler.error(err);
			}
		}

	private void write(Model m) throws XMLStreamException
		{
		w.writeStartElement("gexf");
		w.writeAttribute("xmlns", "http://www.gexf.net/1.2draft");
		w.writeAttribute("version", "1.2");
		
		
		/* meta */
		w.writeStartElement("meta");
			w.writeStartElement("creator");
			  w.writeCharacters(GexfWriter.class.getCanonicalName());
			w.writeEndElement();
			w.writeStartElement("description");
			  w.writeCharacters("RDF Graph");
			w.writeEndElement();
		w.writeEndElement();
		
		/* graph */
		w.writeStartElement("graph");
		w.writeAttribute("mode", "static");
		w.writeAttribute("defaultedgetype", "directed");
		
		
		
		/* attributes */
		w.writeStartElement("attributes");
		w.writeAttribute("class","node");
		w.writeAttribute("mode","static");

		w.writeEndElement();//attributes
		
		/* nodes */
		w.writeStartElement("nodes");
		
	
		/*
		for(Target t:this.m.list.values())
			{
			w.writeStartElement("node");
			w.writeAttribute("id", String.valueOf(t.node()));
			w.writeAttribute("label", t.name);    			
			w.writeEndElement();
			}*/
			

		w.writeEndElement();//nodes
		
		/* edges */
		
		w.writeStartElement("edges");
		/*
		 * int relid=0;
	    for(Target t:this.name2target.values())
	            {
	           for(Target c:t.children)
                {
    			w.writeEmptyElement("edge");
    			w.writeAttribute("id", "E"+(++relid));
    			w.writeAttribute("type","directed");
    			w.writeAttribute("source",c.node());
    			w.writeAttribute("target",t.node());
                }
			}*/

		w.writeEndElement();//edges

		w.writeEndElement();//graph
		
		w.writeEndElement();//gexf
		}
	
}
