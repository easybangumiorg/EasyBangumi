/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: SortCapList.java
*
*	Revision;
*
*	02/03/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object;

import java.util.*;

public class SortCapList extends Vector 
{
	public SortCapList() 
	{
	}
	
	public SortCap getSortCap(int n)
	{
		return (SortCap)get(n);
	}

	public SortCap getSortCap(String type) 
	{
		if (type == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			SortCap scap = getSortCap(n);
			if (type.compareTo(scap.getType()) == 0)
				return scap;
		}
		return null;
	}
}

