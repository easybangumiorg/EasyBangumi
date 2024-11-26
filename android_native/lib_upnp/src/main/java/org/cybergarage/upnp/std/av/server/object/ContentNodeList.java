/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File: ContentNodeList.java
*
*	Revision;
*
*	10/30/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object;

import java.util.*;

public class ContentNodeList extends Vector
{
	public ContentNodeList() 
	{
	}
	
	public ContentNode getContentNode(int n)
	{
		return (ContentNode)get(n);
	}

	public ContentNode getContentNode(String name) 
	{
		if (name == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			ContentNode node = getContentNode(n);
			if (name.compareTo(node.getName()) == 0)
				return node;
		}
		return null;
	}
}

