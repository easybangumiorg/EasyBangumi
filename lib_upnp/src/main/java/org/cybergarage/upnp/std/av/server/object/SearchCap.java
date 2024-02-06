/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: SearchCap.java
*
*	Revision:
*
*	08/07/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object;

public interface SearchCap
{
	public abstract String getPropertyName();
	public boolean compare(SearchCriteria searchCri, ContentNode conNode);
}



