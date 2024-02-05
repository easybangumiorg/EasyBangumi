/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: ArgumentData.java
*
*	Revision;
*
*	02/24/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.xml;

public class ArgumentData extends NodeData
{
	public ArgumentData() 
	{
	}

	////////////////////////////////////////////////
	// value
	////////////////////////////////////////////////

	private String value = "";
	
	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

}

