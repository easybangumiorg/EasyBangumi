/******************************************************************
*
*	MediaPlayer for CyberLink
*
*	Copyright (C) Satoshi Konno 2005
*
*	File : BrowseAction.java
*
*	09/26/05
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.player.action;

import org.cybergarage.xml.*;

public class BrowseResult
{
	////////////////////////////////////////////////
	// Member
	////////////////////////////////////////////////

	private Node resultNode;

	////////////////////////////////////////////////
	// Constrictor
	////////////////////////////////////////////////
	
	public BrowseResult(Node node)
	{
		setResultNode(node);
	}

	////////////////////////////////////////////////
	// Request
	////////////////////////////////////////////////

	public void setResultNode(Node node)
	{
		resultNode = node;
	}

	public Node getResultNode()
	{
		return resultNode;
	}

	////////////////////////////////////////////////
	// ContentNode
	////////////////////////////////////////////////

	public int getNContentNodes()
	{
		return resultNode.getNNodes();
	}

	public Node getContentNode(int n)
	{
		return resultNode.getNode(n);
	}
}
