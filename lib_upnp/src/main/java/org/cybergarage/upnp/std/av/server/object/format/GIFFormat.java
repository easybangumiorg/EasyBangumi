/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File : GIFPlugIn.java
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

public class GIFFormat extends ImageIOFormat
{
	////////////////////////////////////////////////
	// Member
	////////////////////////////////////////////////

	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public GIFFormat()
	{
	}
	
	public GIFFormat(File file)
	{
		super(file);
	}

	////////////////////////////////////////////////
	// Abstract Methods
	////////////////////////////////////////////////
	
	public boolean equals(File file)
	{
		String headerID = Header.getIDString(file, 3);
		if (headerID.startsWith("GIF") == true)
			return true;		
		return false;
	}
	
	public FormatObject createObject(File file)
	{
		return new GIFFormat(file);
	}
	
	public String getMimeType()
	{
		return "image/gif";
	}

}

