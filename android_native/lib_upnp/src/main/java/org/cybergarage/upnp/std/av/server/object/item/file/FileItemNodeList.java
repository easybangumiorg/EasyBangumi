/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
**
*	File: FileItemNodeList.java
*
*	Revision;
*
*	02/12/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.item.file;

import java.io.*;
import java.util.*;

public class FileItemNodeList extends Vector 
{
	public FileItemNodeList() 
	{
	}
	
	public FileItemNode getFileItemNode(int n)
	{
		return (FileItemNode)get(n);
	}
	
	public FileItemNode getFileItemNode(File file)
	{
		int itemNodeCnt = size();
		for (int n=0; n<itemNodeCnt; n++) {
			FileItemNode itemNode = getFileItemNode(n);
			File itemNodeFile = itemNode.getFile();
			if (itemNodeFile == null)
				continue;
			if (itemNode.equals(file) == true)
				return itemNode;
		}
		return null;
	}
	
}

