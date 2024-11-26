/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: SSDP.java
*
*	Revision;
*
*	11/18/02
*		- first revision.
*	05/13/03
*		- Added constants for IPv6.
*	08/03/05
*		- Thanks for Stefano Lenzi <kismet-sl at users.sourceforge.net>
*		  and Mikael <mhakman at users.sourceforge.net>
*		- Fixed getLeaseTime() to parse normally when the value includes extra strings such as white space.
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import org.cybergarage.util.Debug;

/**
 * 
 * This class defines constant value related to SSDP.<br>
 * All the values defined here are complaint to the UPnP Standard 
 * 
 * @author Satoshi "skonno" Konno
 * @author Stefano "Kismet" Lenzi
 * @version 1.0
 *
 */
public class SSDP
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////

	/**
	 * Default destination port for SSDP multicast messages
	 */
	public static final int PORT = 1900;
	
	/**
	 * Default IPv4 multicast address for SSDP messages
	 */
	public static final String ADDRESS = "239.255.255.250";

	public static final String IPV6_LINK_LOCAL_ADDRESS = "FF02::C";
	public static final String IPV6_SUBNET_ADDRESS = "FF03::C";
	public static final String IPV6_ADMINISTRATIVE_ADDRESS = "FF04::C";
	public static final String IPV6_SITE_LOCAL_ADDRESS = "FF05::C";
	public static final String IPV6_GLOBAL_ADDRESS = "FF0E::C";
	
	private static String IPV6_ADDRESS;

	public static final void setIPv6Address(String addr)
	{
		IPV6_ADDRESS = addr;
	}

	public static final String getIPv6Address()
	{
		return IPV6_ADDRESS;
	}
	
	public static final int DEFAULT_MSEARCH_MX = 3;

	public static final int RECV_MESSAGE_BUFSIZE = 1024;

	////////////////////////////////////////////////
	//	Initialize
	////////////////////////////////////////////////

	static 
	{
		setIPv6Address(IPV6_LINK_LOCAL_ADDRESS);
	}
	
	////////////////////////////////////////////////
	//	LeaseTime
	////////////////////////////////////////////////

	public final static int getLeaseTime(String cacheCont){
		/*
		 * Search for max-age keyword instead of equals sign Found value of
		 * max-age ends at next comma or end of string
		 */ 
		int mx = 0;  
		int maxAgeIdx = cacheCont.indexOf("max-age");  
		if (maxAgeIdx >= 0) {  
			int endIdx = cacheCont.indexOf(',',maxAgeIdx);  
			if (endIdx < 0)  
				endIdx = cacheCont.length();  
			try {  
				maxAgeIdx = cacheCont.indexOf("=",maxAgeIdx); 
				String mxStr = cacheCont.substring(maxAgeIdx+1,endIdx).trim();  
				mx = Integer.parseInt(mxStr);  
			} catch (Exception e) {  
				Debug.warning (e);  
			} 
		}  
		return mx; 
	}
}

