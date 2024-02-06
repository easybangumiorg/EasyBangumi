/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: DCTitleSortCap.java
*
*	Revision;
*
*	02/03/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.sort;

import org.cybergarage.upnp.std.av.server.DC;
import org.cybergarage.upnp.std.av.server.object.*;

public class DCTitleSortCap implements SortCap 
{
	public DCTitleSortCap() 
	{
	}
	
	public String getType() 
	{
		return DC.TITLE;
	}

	public int compare(ContentNode conNode1, ContentNode conNode2)
	{
		if (conNode1 == null || conNode2 == null)
			return 0;
		String title1 = conNode1.getTitle();
		String title2 = conNode2.getTitle();
		if (title1 == null || title2 == null)
			return 0;
		return title1.compareTo(title2);
	}
}

