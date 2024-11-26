/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: SSDPResponse.java
*
*	Revision;
*
*	01/14/03
*		- first revision.
*	01/23/04
*		- Oliver Newell
*		- Overided HTTPResponse::getHeader() for Intel UPnP control points.
*	03/16/04
*		- Thanks for Darrell Young
*		- Fixed to set v1.1 to the HTTP version.
*	10/20/04 
*		- Brent Hills <bhills@openshores.com>
*		- Added setMYNAME() and getMYNAME().
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.io.InputStream;

import org.cybergarage.http.*;

public class SSDPResponse extends HTTPResponse
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public SSDPResponse()
	{
		setVersion(HTTP.VERSION_11);
	}

	public SSDPResponse(InputStream in)
	{
		super(in);
	}
	
	////////////////////////////////////////////////
	//	ST (SearchTarget)
	////////////////////////////////////////////////

	public void setST(String value)
	{
		setHeader(HTTP.ST, value);
	}

	public String getST()
	{
		return getHeaderValue(HTTP.ST);
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
	//	MYNAME
	////////////////////////////////////////////////

	public void setMYNAME(String value)
	{
		setHeader(HTTP.MYNAME, value);
	}

	public String getMYNAME()
	{
		return getHeaderValue(HTTP.MYNAME);
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
	
	////////////////////////////////////////////////
	//	getHeader (Override)
	////////////////////////////////////////////////
	
	public String getHeader()
	{
		StringBuffer str = new StringBuffer();
	
		str.append(getStatusLineString());
		str.append(getHeaderString());
		str.append(HTTP.CRLF); // for Intel UPnP control points.
		
		return str.toString();
	}

}
