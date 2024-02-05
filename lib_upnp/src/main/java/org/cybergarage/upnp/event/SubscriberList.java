/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: SubscriberList.java
*
*	Revision;
*
*	01/31/03
*		- first revision.
*	06/18/03
*		- Fixed to catch ArrayIndexOutOfBounds.
*
******************************************************************/

package org.cybergarage.upnp.event;

import java.util.*;

public class SubscriberList extends Vector 
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public SubscriberList() 
	{
	}
	
	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////
	
	public Subscriber getSubscriber(int n)
	{
		Object obj = null;
		try {
			obj = get(n);
		}
		catch (Exception e) {}
		return (Subscriber)obj;
	}
}

