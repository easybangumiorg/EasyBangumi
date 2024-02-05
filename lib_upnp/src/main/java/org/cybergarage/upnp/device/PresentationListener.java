/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: DeviceNotifyListener.java
*
*	Revision;
*
*	11/18/02
*		- first revision.
*	
******************************************************************/

package org.cybergarage.upnp.device;

import org.cybergarage.http.HTTPRequest;

public interface PresentationListener
{
	public void httpRequestRecieved(HTTPRequest httpReq);
}
