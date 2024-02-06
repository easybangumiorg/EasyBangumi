/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File : JPEGPlugIn.java
*
*	Revision:
*
*	01/25/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.format;

import java.io.*;

import org.cybergarage.upnp.std.av.server.object.*;

public class JPEGFormat extends ImageIOFormat
{
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public JPEGFormat()
	{
	}
	
	public JPEGFormat(File file)
	{
		super(file);
	}

	////////////////////////////////////////////////
	// Abstract Methods
	////////////////////////////////////////////////
	
	public boolean equals(File file)
	{
		byte headerID[] = Header.getID(file, 2);
		int header1 = (int)headerID[0] & 0xff;
		int header2 = (int)headerID[1] & 0xff;
		if (header1 == 0xff && header2 == 0xd8)
			return true;
		return false;
	}
	
	public FormatObject createObject(File file)
	{
		return new JPEGFormat(file);
	}
	
	public String getMimeType()
	{
		return "image/jpeg";
	}

}

