/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
**
*	File: ID3FrameList.java
*
*	Revision;
*
*	12/04/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.format;

import java.util.*;

public class ID3FrameList extends Vector 
{
	public ID3FrameList() 
	{
	}
	
	public ID3Frame at(int n)
	{
		return (ID3Frame)get(n);
	}

	public ID3Frame getFrame(int n)
	{
		return (ID3Frame)get(n);
	}

	public ID3Frame getFrame(String name) 
	{
		if (name == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			ID3Frame frame = getFrame(n);
			if (name.compareTo(frame.getID()) == 0)
				return frame;
		}
		return null;
	}

	public byte[] getFrameData(String name) 
	{
		ID3Frame frame = getFrame(name);
		if (frame == null)
			return new byte[0];
		return frame.getData();
	}

	public String getFrameStringData(String name) 
	{
		ID3Frame frame = getFrame(name);
		if (frame == null)
			return "";
		return frame.getStringData();
	}
}

