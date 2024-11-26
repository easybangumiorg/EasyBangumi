/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: MAN.java
*
*	Revision;
*
*	12/30/02
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.device;

public class MAN 
{
	public final static String DISCOVER = "ssdp:discover";
	
	public final static boolean isDiscover(String value)
	{
		if (value == null)
			return false;
		if (value.equals(MAN.DISCOVER) == true)
			return true;
		return value.equals("\"" + MAN.DISCOVER + "\"");
	}
}

