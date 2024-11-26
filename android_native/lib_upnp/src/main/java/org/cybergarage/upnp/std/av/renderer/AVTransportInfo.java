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
*	02/22/08
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.renderer;

public class AVTransportInfo
{
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	
	public AVTransportInfo()
	{
		setInstanceID(0);
		setURI("");
		setURIMetaData("");
	}
		
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////

	private int instanceID;

	public int getInstanceID() {
		return instanceID;
	}
	
	public void setInstanceID(int instanceID) {
		this.instanceID = instanceID;
	}

	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////

	private String uri;

	public String getURI() {
		return uri;
	}
	
	public void setURI(String uri) {
		this.uri = uri;
	}

	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	
	private String uriMetaData;

	public String getURIMetaData() {
		return uriMetaData;
	}
	
	public void setURIMetaData(String uriMetaData) {
		this.uriMetaData = uriMetaData;
	}
	
}

