/******************************************************************
*
*	CyberXML for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: AttributeList.java
*
*	Revision;
*
*	11/27/02
*		- first revision.
*
******************************************************************/

package org.cybergarage.xml;

import java.util.Vector;

public class AttributeList extends Vector 
{
	public AttributeList() 
	{
	}
	
	public Attribute getAttribute(int n)
	{
		return (Attribute)get(n);
	}
	
	public Attribute getAttribute(String name) 
	{
		if (name == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			Attribute elem = getAttribute(n);
			if (name.compareTo(elem.getName()) == 0)
				return elem;
		}
		return null;
	}
}

