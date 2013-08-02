package com.github.lindenb.rdfeditor.rdf;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class JenaUtils
	{
	
	public static RDFNode one(Resource rsrc,Property prop)
		{
		StmtIterator iter=null;
		try
			{
			iter=rsrc.listProperties(prop);
			while(iter.hasNext()) return iter.next().getObject();
			return null;
			}
		finally
			{
			if(iter!=null) iter.close();
			}
		}
	
	public static int count(Resource rsrc,Property prop)
		{
		int n=0;
		StmtIterator iter=null;
		try
			{
			iter=rsrc.listProperties(prop);
			while(iter.hasNext()) { iter.next();++n;}
			return n;
			}
		finally
			{
			if(iter!=null) iter.close();
			}
		}
	/* http://stackoverflow.com/questions/2683202 */
	public static BigDecimal convertToBigDecimal(RDFNode node)
		{
		if(node==null || !node.isLiteral()) return null;
		Object o=node.asLiteral().getValue();
		if(o==null) return null;
		try
			{
			return new BigDecimal(o.toString());
			}
		catch(Throwable err)
			{
			return null;
			}
		}

	public static BigInteger convertToBigInteger(RDFNode node)
		{
		BigDecimal bd=convertToBigDecimal(node);
		return bd==null?null:bd.toBigInteger();
		}

	}
