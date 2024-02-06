/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : ID3Frame
*
*	Revision:
*
*	12/03/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.format;

public class ID3Frame
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	
	public final static String TIT1 = "TIT1";
	public final static String TIT2 = "TIT2";
	public final static String TIT3 = "TIT3";

	public final static String TPE1 = "TPE1";
	public final static String TPE2 = "TPE2";
	public final static String TPE3 = "TPE3";
	public final static String TPE4 = "TPE4";
	
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public ID3Frame()
	{
		setID("");
		setFlag(0);
		setSize(0);
	}
	
	////////////////////////////////////////////////
	// ID
	////////////////////////////////////////////////
	
	private String id;

	public void setID(String val) {
		id = val;
	}

	public String getID() {
		return id;
	}

	////////////////////////////////////////////////
	// Size
	////////////////////////////////////////////////
	
	private int flag;
	
	public void setFlag(int val) {
		flag = val;
	}

	public int getFlag() {
		return flag;
	}

	////////////////////////////////////////////////
	// Size
	////////////////////////////////////////////////
	
	private int size;
	
	public void setSize(int val) {
		size = val;
	}

	public int getSize() {
		return size;
	}

	////////////////////////////////////////////////
	// Data
	////////////////////////////////////////////////
	
	private byte data[];
	
	public void setData(byte val[]) 
	{
		data = val;
	}

	public byte[] getData() 
	{
		return data;
	}

	public String getStringData() 
	{
		return new String(data, 1, getSize()-1);
	}
}

