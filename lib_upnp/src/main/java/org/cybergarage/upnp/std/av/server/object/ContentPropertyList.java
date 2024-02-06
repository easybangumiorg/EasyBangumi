/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File: ContentPropertyList.java
*
*	Revision;
*
*	10/29/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object;

import java.util.*;

public class ContentPropertyList extends Vector 
{
	public ContentPropertyList() 
	{
	}
	
	public ContentProperty getContentProperty(int n)
	{
		return (ContentProperty)get(n);
	}

	public ContentProperty getContentProperty(String name) 
	{
		if (name == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			ContentProperty elem = getContentProperty(n);
			if (name.compareTo(elem.getName()) == 0)
				return elem;
		}
		return null;
	}
}

