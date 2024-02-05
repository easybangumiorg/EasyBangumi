/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: SSDPSearchResponse.java
*
*	Revision;
*
*	01/14/03
*		- first revision.
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import org.cybergarage.http.*;

import org.cybergarage.upnp.*;

public class SSDPSearchResponse extends SSDPResponse
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public SSDPSearchResponse()
	{
		setStatusCode(HTTPStatus.OK);
		setCacheControl(Device.DEFAULT_LEASE_TIME);
		setHeader(HTTP.SERVER, UPnP.getServerName());
		setHeader(HTTP.EXT, "");
	}
}
