/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: ParameterList.java
*
*	Revision;
*
*	02/01/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.http;

import java.util.Vector;

public class ParameterList extends Vector 
{
	public ParameterList() 
	{
	}
	
	public Parameter at(int n)
	{
		return (Parameter)get(n);
	}

	public Parameter getParameter(int n)
	{
		return (Parameter)get(n);
	}
	
	public Parameter getParameter(String name) 
	{
		if (name == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			Parameter param = at(n);
			if (name.compareTo(param.getName()) == 0)
				return param;
		}
		return null;
	}

	public String getValue(String name) 
	{
		Parameter param = getParameter(name);
		if (param == null)
			return "";
		return param.getValue();
	}
}

