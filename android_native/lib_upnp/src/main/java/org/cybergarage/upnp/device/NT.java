/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: NT.java
*
*	Revision;
*
*	12/09/02
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.device;

public class NT 
{
	public final static String ROOTDEVICE = "upnp:rootdevice";
	public final static String EVENT = "upnp:event";
	
	public final static boolean isRootDevice(String ntValue)
	{
		if (ntValue == null)
			return false;
		return ntValue.startsWith(ROOTDEVICE);
	}
}

