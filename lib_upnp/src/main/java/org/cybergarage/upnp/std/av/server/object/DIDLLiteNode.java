/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : ContentNode
*
*	Revision:
*
*	10/30/03
*		- first revision.
*	10/26/04 
*		- Brent Hills <bhills@openshores.com>
*		- Changed DIDLLiteNode is a subclass of Node instead of ContentNode
*		  because the node has the parentID attributes.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object;

import org.cybergarage.xml.Node;

public class DIDLLiteNode extends Node // Thanks for Brent Hills (10/28/04)
{
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public DIDLLiteNode()
	{
		setName(DIDLLite.NAME);
		setAttribute(DIDLLite.XMLNS, DIDLLite.XMLNS_URL);
		setAttribute(DIDLLite.XMLNS_DC, DIDLLite.XMLNS_DC_URL);
		setAttribute(DIDLLite.XMLNS_UPNP, DIDLLite.XMLNS_UPNP_URL);
	}

	////////////////////////////////////////////////
	//	Child node
	////////////////////////////////////////////////

	public void addContentNode(ContentNode node) 
	{
		addNode(node);
	}

	public boolean removeContentNode(ContentNode node) 
	{
		return removeNode(node);
	}
}

