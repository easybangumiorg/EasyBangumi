/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: IconList.java
*
*	Revision;
*
*	12/04/02
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp;

import java.util.Vector;

public class IconList extends Vector 
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public final static String ELEM_NAME = "iconList";

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public IconList() 
	{
	}
	
	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////
	
	public Icon getIcon(int n)
	{
		return (Icon)get(n);
	}
}

