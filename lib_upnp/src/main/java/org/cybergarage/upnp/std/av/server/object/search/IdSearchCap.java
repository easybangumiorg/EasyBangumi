/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: IdSearchCap.java
*
*	Revision;
*
*	08/16/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.search;

import org.cybergarage.upnp.std.av.server.object.*;

public class IdSearchCap implements SearchCap 
{
	public IdSearchCap() 
	{
	}
	
	public String getPropertyName() 
	{
		return SearchCriteria.ID;
	}

	public boolean compare(SearchCriteria searchCri, ContentNode conNode)
	{
		String searchCriID = searchCri.getValue();
		String conID = conNode.getID();
		if (searchCriID == null|| conID == null)
				return false;
		if (searchCri.isEQ() == true)
			return (searchCriID.compareTo(conID) == 0) ? true : false;
		return false;
		
	}
}

