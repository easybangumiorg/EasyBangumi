/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : ContentRoot
*
*	Revision:
*
*	10/28/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.container;

public class RootNode extends ContainerNode
{
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public RootNode()
	{
		setID(0);
		setParentID(-1);
		setTitle("Root");
	}

}

