/******************************************************************
*
*	CyberXML for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: Attribute.java
*
*	Revision;
*
*	11/27/02
*		- first revision.
*
******************************************************************/

package org.cybergarage.xml;

public class Attribute 
{
	private String name = new String(); 
	private String value = new String(); 

	public Attribute() 
	{
	}

	public Attribute(String name, String value) 
	{
		this();
		setName(name);
		setValue(value);
	}

	public Attribute(Attribute otherAttr) 
	{
		this();
		set(otherAttr);
	}
	
	////////////////////////////////////////////////
	//	name
	////////////////////////////////////////////////

	public void setName(String name) 
	{
		this.name = name;
	}

	public String getName() 
	{
		return name;
	}

	////////////////////////////////////////////////
	//	value
	////////////////////////////////////////////////

	public void setValue(String value) 
	{
		this.value = value;
	}

	public String getValue() 
	{
		return value;
	}

	////////////////////////////////////////////////
	//	set
	////////////////////////////////////////////////

	public void set(Attribute otherAttr) 
	{
		setName(otherAttr.getName());
		setValue(otherAttr.getValue());
	}
}

