/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: TitleSearchCap.java
*
*	Revision;
*
*	08/21/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.search;

import org.cybergarage.upnp.std.av.server.object.*;

public class TitleSearchCap implements SearchCap 
{
	public TitleSearchCap() 
	{
	}
	
	public String getPropertyName() 
	{
		return SearchCriteria.TITLE;
	}

	public boolean compare(SearchCriteria searchCri, ContentNode conNode)
	{
		String searchCriTitle = searchCri.getValue();
		String conTitle = conNode.getTitle();
		if (searchCriTitle == null|| conTitle == null)
				return false;
		int cmpRet = conTitle.compareTo(searchCriTitle);
		if (cmpRet == 0 && (searchCri.isEQ() || searchCri.isLE() || searchCri.isGE()))
				return true;
		else if (cmpRet < 0 && searchCri.isLT())
			return true;
		else if (0 < cmpRet && searchCri.isGT())
			return true;
		int idxRet = conTitle.indexOf(searchCriTitle);
		if (0 <= idxRet && searchCri.isContains())
			return true;
		else if (searchCri.isDoesNotContain())
			return true;
		return false;
	}
}

