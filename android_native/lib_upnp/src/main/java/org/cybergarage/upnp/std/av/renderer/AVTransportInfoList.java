/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: ConnectionInfoList.java
*
*	Revision;
*
*	02/22/08
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.renderer;

import java.util.*;

public class AVTransportInfoList extends Vector 
{
	////////////////////////////////////////////////
	// Constrictor
	////////////////////////////////////////////////
	
	public AVTransportInfoList() 
	{
	}
	
	////////////////////////////////////////////////
	// getConnectionInfo
	////////////////////////////////////////////////
	
	public AVTransportInfo getAVTransportInfo(int n)
	{
		return (AVTransportInfo)get(n);
	}
}

