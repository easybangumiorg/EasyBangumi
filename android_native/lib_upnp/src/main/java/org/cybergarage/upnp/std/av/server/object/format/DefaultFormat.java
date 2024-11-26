/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File : DefaultPlugIn.java
*
*	Revision:
*
*	02/12/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.format;

import java.io.*;

import org.cybergarage.xml.*;
import org.cybergarage.upnp.std.av.server.object.*;

public class DefaultFormat implements Format, FormatObject
{
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public DefaultFormat()
	{
	}
	
	////////////////////////////////////////////////
	// Abstract Methods
	////////////////////////////////////////////////
	
	public boolean equals(File file)
	{
		return true;
	}
	
	public FormatObject createObject(File file)
	{
		return new DefaultFormat();
	}
	
	public String getMimeType()
	{
		return "*/*";
	}

	public String getMediaClass()
	{
		return "object.item";
	}
	
	public AttributeList getAttributeList()
	{
		return new AttributeList();
	}
	
	public String getTitle()
	{
		return "";
	}
	
	public String getCreator()
	{
		return "";
	}
}

