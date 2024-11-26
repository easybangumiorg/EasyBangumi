/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File : ConnectionInfo.java
*
*	Revision:
*
*	06/19/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server;

public class ConnectionInfo
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	
	public final static String INPUT = "Input";
	public final static String OUTPUT = "Output";
	public final static String OK = "OK";
	public final static String UNKNOWN = "Unknown";
	
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	
	public ConnectionInfo(int id)
	{
		setID(id);
		setRcsID(-1);
		setAVTransportID(-1);
		setProtocolInfo("");
		setPeerConnectionManager("");
		setPeerConnectionID(-1);
		setDirection(OUTPUT);
		setStatus(UNKNOWN);
	}
		
	////////////////////////////////////////////////
	// ID
	////////////////////////////////////////////////
	
	private int id;

	public void setID(int value)
	{
		id = value;
	}
	
	public int getID()
	{
		return id;
	}

	////////////////////////////////////////////////
	// RcsID
	////////////////////////////////////////////////
	
	private int rcsId;

	public void setRcsID(int value)
	{
		rcsId = value;
	}
	
	public int getRcsID()
	{
		return rcsId;
	}

	////////////////////////////////////////////////
	// AVTransportID
	////////////////////////////////////////////////
	
	private int transId;

	public void setAVTransportID(int value)
	{
		transId = value;
	}
	
	public int getAVTransportID()
	{
		return transId;
	}
	 
	////////////////////////////////////////////////
	// ProtocolInfo
	////////////////////////////////////////////////
	
	private String protocolInfo;

	public void setProtocolInfo(String value)
	{
		protocolInfo = value;
	}
	
	public String getProtocolInfo()
	{
		return protocolInfo;
	}
	
	////////////////////////////////////////////////
	// PeerConnectionManager
	////////////////////////////////////////////////
	
	private String peerConnectionManager;

	public void setPeerConnectionManager(String value)
	{
		peerConnectionManager = value;
	}
	
	public String getPeerConnectionManager()
	{
		return peerConnectionManager;
	}
	 
	////////////////////////////////////////////////
	// PeerConnectionID 
	////////////////////////////////////////////////
	
	private int peerConnectionID;

	public void setPeerConnectionID(int value)
	{
		peerConnectionID = value;
	}
	
	public int getPeerConnectionID()
	{
		return peerConnectionID;
	}

	////////////////////////////////////////////////
	// Direction
	////////////////////////////////////////////////
	
	private String direction;

	public void setDirection(String value)
	{
		direction = value;
	}
	
	public String getDirection()
	{
		return direction;
	}

	////////////////////////////////////////////////
	// Status
	////////////////////////////////////////////////
	
	private String status;

	public void setStatus(String value)
	{
		status = value;
	}
	
	public String getStatus()
	{
		return status;
	}
}

