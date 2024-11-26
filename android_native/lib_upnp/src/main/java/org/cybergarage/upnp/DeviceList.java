/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: DeviceList.java
*
*	Revision;
*
*	12/04/02
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp;

import java.util.Vector;

public class DeviceList extends Vector 
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public final static String ELEM_NAME = "deviceList";

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public DeviceList() 
	{
	}
	
	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////
	
	public Device getDevice(int n)
	{
		return (Device)get(n);
	}
}

