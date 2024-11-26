/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File: DirectoryList.java
*
*	Revision;
*
*	11/11/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server;

import java.util.*;

public class DirectoryList extends Vector 
{
	////////////////////////////////////////////////
	// Constrictor
	////////////////////////////////////////////////
	
	public DirectoryList() 
	{
	}
	
	////////////////////////////////////////////////
	// getDirectory
	////////////////////////////////////////////////
	
	public Directory getDirectory(int n)
	{
		return (Directory)get(n);
	}

	public Directory getDirectory(String name)
	{
		int dirCnt = size();
		for (int n=0; n<dirCnt; n++) {
			Directory dir = getDirectory(n);
			String dirName = dir.getFriendlyName();
			if (dirName == null)
				continue;
			if (dirName.equals(name) == true)
				return dir;
		}
		return null;
	}

	////////////////////////////////////////////////
	// update
	////////////////////////////////////////////////
	
	public void update()
	{
		int dirCnt = size();
		for (int n=0; n<dirCnt; n++) {
			Directory dir = getDirectory(n);
			dir.updateContentList();
		}
	}
	
}

