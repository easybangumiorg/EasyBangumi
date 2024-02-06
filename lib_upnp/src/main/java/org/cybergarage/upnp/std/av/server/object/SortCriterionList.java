/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: SortCriterionList.java
*
*	Revision:
*
*	04/06/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object;

import java.util.*;

public class SortCriterionList extends Vector 
{
	public SortCriterionList() 
	{
	}
	
	public String getSortCriterion(int n)
	{
		return (String)get(n);
	}
}

