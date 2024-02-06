/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : ItemNode.java
*
*	Revision:
*
*	10/22/03
*		- first revision.
*	01/28/04
*		- Added file and timestamp parameters.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.item;

import org.cybergarage.xml.*;
import org.cybergarage.upnp.std.av.server.object.*;

public class ResourceNode extends ContentNode
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	
	public final static String 
	NAME = "res";

	public final static String PROTOCOL_INFO = "protocolInfo";
	
	public final static String SIZE = "size";
	public final static String IMPORT_URI = "importUri";
	public final static String COLOR_DEPTH = "colorDepth";
	public final static String RESOLUTION = "resolution";
	
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public ResourceNode()
	{
	}
	
	////////////////////////////////////////////////
	// isResourceNode
	//////////////////////////////////////////////	//

	public final static boolean isResourceNode(Node node)
	{
		String name = node.getName();
		if (name == null)
			return false;
		return name.equals(NAME);
	}
	
	////////////////////////////////////////////////
	// set
	////////////////////////////////////////////////

	public boolean set(Node node)
	{
		setValue(node.getValue());
		
		// Attribute -> Attribute;
		int nAttr = node.getNAttributes();
		for (int n=0; n<nAttr; n++) {
			Attribute attr = node.getAttribute(n);
			setAttribute(attr.getName(), attr.getValue());
		}
		
		return true;
	}
	
	////////////////////////////////////////////////
	//	Property
	////////////////////////////////////////////////
	
	public String getURL()
	{
		return getValue();
	}

	////////////////////////////////////////////////
	//	ProtocolInfo
	////////////////////////////////////////////////
	
	public String getProtocolInfo()
	{
		return getAttributeValue("protocolInfo");
	}

	public String getProtocolInfoAtIndex(int anIndex)
	{
		String protocolInfo = getProtocolInfo();
		if (protocolInfo == null)
			return "";
		String protocols[] = protocolInfo.split(":");
		if (protocols == null || protocols.length <= anIndex)
			return "";
		return protocols[anIndex];
	}

	public String getProtocol()
	{
		return getProtocolInfoAtIndex(0);
	}

	public String getNetwork()
	{
		return getProtocolInfoAtIndex(1);
	}

	public String getContentFormat()
	{
		return getProtocolInfoAtIndex(2);
	}

	public String getAdditionalInfo()
	{
		return getProtocolInfoAtIndex(3);
	}

	public String getAdditionalInfoForKey(String aKey)
	{
		if (aKey == null)
			return "";
		String fullAddInfo = getAdditionalInfo();
		if (fullAddInfo == null)
			return "";
		String addInfos[] = fullAddInfo.split(";");
		if (addInfos == null || addInfos.length <= 0)
			return "";
		for (int n=0; n<addInfos.length; n++) {
			String addInfo = addInfos[n];
			if (addInfo.startsWith(aKey) == false)
				continue;
			String tokens[] = addInfo.split("=");
			if (tokens == null || tokens.length < 2)
				return "";
			return tokens[1];
		}
		return "";
	}
	
	////////////////////////////////////////////////
	//	Property
	////////////////////////////////////////////////
	
	public String getDlnaOrgPn()
	{
		return getAdditionalInfoForKey("DLNA.ORG_PN");
	}

	public String getDlnaOrgOp()
	{
		return getAdditionalInfoForKey("DLNA.ORG_OP");
	}

	public String getDlnaOrgFlags()
	{
		return getAdditionalInfoForKey("DLNA.ORG_FLAGS");
	}

	public boolean isThumbnail()
	{
		String dlnaOrgPn = getDlnaOrgPn();
		if (dlnaOrgPn == null)
			return false;
		if (dlnaOrgPn.endsWith("_TN"))
			return true;
		return false;
	}

	public boolean isSmallImage()
	{
		String dlnaOrgPn = getDlnaOrgPn();
		if (dlnaOrgPn == null)
			return false;
		if (dlnaOrgPn.endsWith("_SM"))
			return true;
		return false;
	}

	public boolean isMediumImage()
	{
		String dlnaOrgPn = getDlnaOrgPn();
		if (dlnaOrgPn == null)
			return false;
		if (dlnaOrgPn.endsWith("_MED"))
			return true;
		return false;
	}

	public boolean isLargeImage()
	{
		String dlnaOrgPn = getDlnaOrgPn();
		if (dlnaOrgPn == null)
			return false;
		if (dlnaOrgPn.endsWith("_LRG"))
			return true;
		return false;
	}

	public boolean isImage()
	{
		String mimeType = getContentFormat();
		if (mimeType == null)
			return false;
		return mimeType.startsWith("image");
	}

	public boolean isMovie()
	{
		String mimeType = getContentFormat();
		if (mimeType == null)
			return false;
		if (mimeType.startsWith("movie"))
			return true;
		return mimeType.startsWith("video");
	}

	public boolean isVideo()
	{
		return isMovie();
	}

	public boolean isAudio()
	{
		String mimeType = getContentFormat();
		if (mimeType == null)
			return false;
		return mimeType.startsWith("audio");
	}
}

