package com.github.lindenb.rdfeditor.rdf;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

public class JavaDataType extends BaseDatatype{
	private static final Logger LOG = Logger.getLogger("com.github.lindenb");

	public static final String URI_PREFIX="urn:java:";
	
	private Class<?> clazz;
	private Constructor<?> ctor;

	
	public JavaDataType(Class<?> clazz)
		{
		super(URI_PREFIX+clazz.getName());
		this.clazz=clazz;
		try {
			this.ctor=clazz.getConstructor(String.class);
			}
		catch (Exception e)
			{
			throw new RuntimeException("Cannot find a string constructor for "+clazz);
			}
		}
	
	@Override
	public Object parse(String lexicalForm) throws DatatypeFormatException {
		try {
			return this.ctor.newInstance(lexicalForm);
		} catch (Exception e) {
			throw new DatatypeFormatException("Cannot cast "+lexicalForm+" to "+getJavaClass());
			}
		
		
		}
	
	@Override
	public Class<?> getJavaClass() {
		return this.clazz;
		}
	@Override
	public boolean isEqual(LiteralLabel value1, LiteralLabel value2)
		{
	     return value1.getDatatype() == value2.getDatatype()
	             && value1.getValue().equals(value2.getValue())
	             ;
		}
	
	
    /**
     * Add all of the XSD pre-defined simple types to the given
     * type mapper registry.
     */
    public static void loadJavaTypes(TypeMapper tm)
    	{
    	LOG.info("register java data types");
        tm.registerDatatype(javaBigDecimal);
        tm.registerDatatype(javaBigInteger);
        tm.registerDatatype(javaBoolean);
        tm.registerDatatype(javaDouble);
        tm.registerDatatype(javaFloat);
        tm.registerDatatype(javaInt);
        tm.registerDatatype(javaByte);
        tm.registerDatatype(javaLong);
        tm.registerDatatype(javaShort);
        tm.registerDatatype(javaString);
        tm.registerDatatype(javaUrl);
        tm.registerDatatype(javaUri);
    }
	
	
    public static final JavaDataType javaString = new JavaDataType(String.class);
    
    public static final JavaDataType javaFloat = new JavaDataType(Float.class);

    public static final JavaDataType javaDouble = new JavaDataType(Double.class);

    public static final JavaDataType javaInt = new JavaDataType(Integer.class);

    public static final JavaDataType javaLong = new JavaDataType(Long.class);

    public static final JavaDataType javaShort = new JavaDataType(Short.class);

    public static final JavaDataType javaByte = new JavaDataType(Byte.class);


    public static final JavaDataType javaBigDecimal = new JavaDataType(BigDecimal.class);

    public static final JavaDataType javaBigInteger = new JavaDataType(BigInteger.class);

    public static final JavaDataType javaBoolean = new JavaDataType(Boolean.class);

    public static final JavaDataType javaUrl = new JavaDataType(URL.class);
    
    public static final JavaDataType javaUri = new JavaDataType(URI.class);

	}
