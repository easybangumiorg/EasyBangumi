/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : Directory
*
*	Revision:
*
*	11/11/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server;

import org.cybergarage.upnp.std.av.server.object.container.*;

public abstract class Directory extends ContainerNode
{
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	
	public Directory(ContentDirectory cdir, String name)
	{
		setContentDirectory(cdir);
		setFriendlyName(name);
	}

	public Directory(String name)
	{
		this(null, name);
	}
	
	////////////////////////////////////////////////
	// Name
	////////////////////////////////////////////////

	public void setFriendlyName(String name)
	{
		setTitle(name);
	}
	
	public String getFriendlyName()
	{
		return getTitle();
	}
	

	////////////////////////////////////////////////
	// update
	////////////////////////////////////////////////
	
	public abstract boolean update();
	
	public void updateContentList()
	{
		if (update() == true) {
			int nContents = getNContentNodes();
			setChildCount(nContents);
			getContentDirectory().updateSystemUpdateID();
		}
	}
}

