/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
**
*	File: ItemNodeList.java
*
*	Revision;
*
*	11/11/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.item;

import java.util.*;

public class ItemNodeList extends Vector 
{
	public ItemNodeList() 
	{
	}
	
	public ItemNode getItemNode(int n)
	{
		return (ItemNode)get(n);
	}
}



