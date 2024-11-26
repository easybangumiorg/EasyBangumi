/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: SSDPRequest.java
*
*	Revision;
*
*	01/14/03
*		- first revision.
*	03/16/04
*		- Thanks for Darrell Young
*		- Fixed to set v1.1 to the HTTP version.;
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.io.InputStream;

import org.cybergarage.http.*;

public class SSDPRequest extends HTTPRequest
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public SSDPRequest()
	{
		setVersion(HTTP.VERSION_11);
	}

	public SSDPRequest(InputStream in)
	{
		super(in);
	}
	
	////////////////////////////////////////////////
	//	NT
	////////////////////////////////////////////////

	public void setNT(String value)
	{
		setHeader(HTTP.NT, value);
	}

	public String getNT()
	{
		return getHeaderValue(HTTP.NT);
	}

	////////////////////////////////////////////////
	//	NTS
	////////////////////////////////////////////////

	public void setNTS(String value)
	{
		setHeader(HTTP.NTS, value);
	}

	public String getNTS()
	{
		return getHeaderValue(HTTP.NTS);
	}

	////////////////////////////////////////////////
	//	Location
	////////////////////////////////////////////////

	public void setLocation(String value)
	{
		setHeader(HTTP.LOCATION, value);
	}

	public String getLocation()
	{
		return getHeaderValue(HTTP.LOCATION);
	}

	////////////////////////////////////////////////
	//	USN
	////////////////////////////////////////////////

	public void setUSN(String value)
	{
		setHeader(HTTP.USN, value);
	}

	public String getUSN()
	{
		return getHeaderValue(HTTP.USN);
	}

	////////////////////////////////////////////////
	//	CacheControl
	////////////////////////////////////////////////

	public void setLeaseTime(int len)
	{
		setHeader(HTTP.CACHE_CONTROL, "max-age=" + Integer.toString(len));
	}

	public int getLeaseTime()
	{
		String cacheCtrl = getHeaderValue(HTTP.CACHE_CONTROL);
		return SSDP.getLeaseTime(cacheCtrl);
	}

	////////////////////////////////////////////////
	//	BootId
	////////////////////////////////////////////////

	public void setBootId(int bootId)
	{
		setHeader(HTTP.BOOTID_UPNP_ORG, bootId);
	}

	public int getBootId()
	{
		return getIntegerHeaderValue(HTTP.BOOTID_UPNP_ORG);
	}
}
