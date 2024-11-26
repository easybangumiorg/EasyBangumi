/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: FormatList
*
*	Revision;
*
*	01/12/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object;

import java.io.*;
import java.util.*;

public class FormatList extends Vector 
{
	public FormatList() 
	{
	}
	
	public Format getFormat(int n)
	{
		return (Format)get(n);
	}

	public Format getFormat(String type) 
	{
		if (type == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			Format format = getFormat(n);
			if (type.compareTo(format.getMimeType()) == 0)
				return format;
		}
		return null;
	}

	public Format getFormat(File file) 
	{
		if (file == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			Format format = getFormat(n);
			if (format.equals(file) == false)
				continue;
			return format;
		}
		return null;
	}
}

