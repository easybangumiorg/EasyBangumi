/******************************************************************
*
*	CyberLink for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: SSDPPacket.java
*
*	Revision;
*
*	11/18/02
*		- first revision.
*	05/13/03
*		- Added getLocalAddress().
*	11/01/04
*		- Theo Beisch <theo.beisch@gmx.de>
*		- Fixed isRootDevice() to check the ST header.
*	11/19/04
*		- Theo Beisch <theo.beisch@gmx.de>
*		- Changed getRemoteAddress() to return the adresss instead of the host name.
*
******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.*;

import org.cybergarage.http.*;

import org.cybergarage.upnp.device.*;

public class SSDPPacket 
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public SSDPPacket(byte[] buf, int length)
	{
		dgmPacket = new DatagramPacket(buf, length);
	}

	////////////////////////////////////////////////
	//	DatagramPacket
	////////////////////////////////////////////////
	
	private DatagramPacket dgmPacket = null;

	public DatagramPacket getDatagramPacket()
	{
		return dgmPacket;
	}

	////////////////////////////////////////////////
	//	addr
	////////////////////////////////////////////////
	
	private String localAddr = "";
	
	public void setLocalAddress(String addr)
	{
		localAddr = addr;
	}
	
	public String getLocalAddress()
	{
		return localAddr;
	}

	
	////////////////////////////////////////////////
	//	Time
	////////////////////////////////////////////////

	private long timeStamp;
	
	public void setTimeStamp(long value)
	{
		timeStamp = value;
	}
		
	public long getTimeStamp()
	{
		return timeStamp;
	}

	////////////////////////////////////////////////
	//	Remote host
	////////////////////////////////////////////////

	public InetAddress getRemoteInetAddress()
	{
		return getDatagramPacket().getAddress();
	}
	
	public String getRemoteAddress()
	{
		// Thanks for Theo Beisch (11/09/04)
		return getDatagramPacket().getAddress().getHostAddress();
	}

	public int getRemotePort()
	{
		return getDatagramPacket().getPort();
	}
	
	////////////////////////////////////////////////
	//	Access Methods
	////////////////////////////////////////////////

	public byte[] packetBytes = null;
	
	public byte[] getData()
	{
		if (packetBytes != null)
			return packetBytes;
		
		DatagramPacket packet = getDatagramPacket();
		int packetLen = packet.getLength();
		String packetData = new String(packet.getData(), 0, packetLen);
		packetBytes = packetData.getBytes();
		
		return packetBytes;
	}

	////////////////////////////////////////////////
	//	Access Methods
	////////////////////////////////////////////////

	public String getHost()
	{
		return HTTPHeader.getValue(getData(), HTTP.HOST);
	}

	public String getCacheControl()
	{
		return HTTPHeader.getValue(getData(), HTTP.CACHE_CONTROL);
	}
	
	public String getLocation()
	{
		return HTTPHeader.getValue(getData(), HTTP.LOCATION);
	}

	public String getMAN()
	{
		return HTTPHeader.getValue(getData(), HTTP.MAN);
	}

	public String getST()
	{
		return HTTPHeader.getValue(getData(), HTTP.ST);
	}

	public String getNT()
	{
		return HTTPHeader.getValue(getData(), HTTP.NT);
	}

	public String getNTS()
	{
		return HTTPHeader.getValue(getData(), HTTP.NTS);
	}

	public String getServer()
	{
		return HTTPHeader.getValue(getData(), HTTP.SERVER);
	}

	public String getUSN()
	{
		return HTTPHeader.getValue(getData(), HTTP.USN);
	}

	public int getMX()
	{
		return HTTPHeader.getIntegerValue(getData(), HTTP.MX);
	}

	////////////////////////////////////////////////
	//	Access Methods
	////////////////////////////////////////////////

	public InetAddress getHostInetAddress()
	{
		String addrStr = "127.0.0.1";
		String host = getHost();
		int canmaIdx = host.lastIndexOf(":");
		if (0 <= canmaIdx) {
			addrStr = host.substring(0, canmaIdx);
			if (addrStr.charAt(0) == '[')
				addrStr = addrStr.substring(1, addrStr.length());
			if (addrStr.charAt(addrStr.length()-1) == ']')
				addrStr = addrStr.substring(0, addrStr.length()-1);
		}
		InetSocketAddress isockaddr = new InetSocketAddress(addrStr, 0);
		return isockaddr.getAddress();
	}
	
	////////////////////////////////////////////////
	//	Access Methods (Extension)
	////////////////////////////////////////////////
	
	public boolean isRootDevice()
	{
		if (NT.isRootDevice(getNT()) == true)
			return true;
		// Thanks for Theo Beisch (11/01/04)
		if (ST.isRootDevice(getST()) == true)
			return true;
		return USN.isRootDevice(getUSN());
	}

	public boolean isDiscover()
	{
		return MAN.isDiscover(getMAN());
	}
	
	public boolean isAlive()
	{
		return NTS.isAlive(getNTS());
	}

	public boolean isByeBye()
	{
		return NTS.isByeBye(getNTS());
	}

	public int getLeaseTime()
	{
		return SSDP.getLeaseTime(getCacheControl());
	}

	////////////////////////////////////////////////
	//	toString
	////////////////////////////////////////////////

	public String toString()
	{
		return new String(getData());
	}
}

