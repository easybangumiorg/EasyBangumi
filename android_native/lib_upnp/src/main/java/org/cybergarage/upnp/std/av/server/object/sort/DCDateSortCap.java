/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: DCDateSortCap.java
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
import org.cybergarage.upnp.std.av.server.object.item.*;

public class DCDateSortCap implements SortCap 
{
	public DCDateSortCap() 
	{
	}
	
	public String getType() 
	{
		return DC.DATE;
	}

	public int compare(ContentNode conNode1, ContentNode conNode2)
	{
		if (conNode1 == null || conNode2 == null)
			return 0;
		if (!(conNode1 instanceof ItemNode) || !(conNode2 instanceof ItemNode))
			return 0;
		ItemNode itemNode1 = (ItemNode)conNode1;
		ItemNode itemNode2 = (ItemNode)conNode2;
		long itemTime1 = itemNode1.getDateTime();
		long itemTime2 = itemNode2.getDateTime();
		if (itemTime1 == itemTime2)
			return 0;
		return (itemTime1 < itemTime2) ? -1 : 1;
	}
}

