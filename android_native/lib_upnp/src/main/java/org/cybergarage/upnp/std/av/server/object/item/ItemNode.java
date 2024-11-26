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

import java.io.InputStream;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.cybergarage.util.*;
import org.cybergarage.xml.*;
import org.cybergarage.upnp.std.av.server.*;
import org.cybergarage.upnp.std.av.server.object.*;
import org.cybergarage.upnp.std.av.server.object.container.ContainerNode;

public class ItemNode extends ContentNode
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	
	public final static String NAME = "item";

	public final static String RES = "res";
	
	public final static String PROTOCOL_INFO = "protocolInfo";
	
	public final static String SIZE = "size";
	public final static String IMPORT_URI = "importUri";
	public final static String COLOR_DEPTH = "colorDepth";
	public final static String RESOLUTION = "resolution";
	
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public ItemNode()
	{
		setID(-1);
		setName(NAME);
		setStorageMedium(UNKNOWN);
		setWriteStatus(UNKNOWN);
	}

	////////////////////////////////////////////////
	// isItemNode
	//////////////////////////////////////////////	//

	public final static boolean isItemNode(Node node)
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
		// Child Node -> Property;
		int nNode = node.getNNodes();
		for (int n=0; n<nNode; n++) {
			Node cnode = node.getNode(n);
			if (ContainerNode.isContainerNode(cnode) == true)
				continue;
			if (ItemNode.isItemNode(cnode) == true)
				continue;
			if (ResourceNode.isResourceNode(cnode) == true) {
				ResourceNode resNode = new ResourceNode();
				resNode.set(cnode);
				addResourceNode(resNode);
				continue;
			}
			setProperty(cnode.getName(), cnode.getValue());
		}

		// Attribute -> Attribute;
		int nAttr = node.getNAttributes();
		for (int n=0; n<nAttr; n++) {
			Attribute attr = node.getAttribute(n);
			setAttribute(attr.getName(), attr.getValue());
		}
		
		return true;
	}
	
	////////////////////////////////////////////////
	//	Child node
	////////////////////////////////////////////////

	public void addContentNode(ContentNode node) 
	{
		addNode(node);
		node.setParentID(getID());
		node.setContentDirectory(getContentDirectory());
	}

	public boolean removeContentNode(ContentNode node) 
	{
		return removeNode(node);
	}

	////////////////////////////////////////////////
	// dc:creator
	////////////////////////////////////////////////

	private final static String DATE_FORMAT = "yyyy-MM-dd";
	
	public void setDate(String value)
	{
		setProperty(DC.DATE, value);
	}
	
	public String getDate()
	{
		return getPropertyValue(DC.DATE);
	}

	public void setDate(long dateTime)
	{	
		try {
			Date date = new Date(dateTime);
			//DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
			//DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			DateFormat df = new SimpleDateFormat(DATE_FORMAT);
			String dateStr= df.format(date);
			setDate(dateStr);
		}
		catch (Exception e) {
			Debug.warning(e);
		}
	}
	
	public long getDateTime()
	{
		String dateStr = getDate();
		if (dateStr == null || dateStr.length() < 10)
			return 0;
		DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		try {
			Date date = df.parse(dateStr);
			return date.getTime();
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	////////////////////////////////////////////////
	// dc:creator
	////////////////////////////////////////////////
	
	public void setCreator(String name)
	{
		setProperty(DC.CREATOR, name);
	}
	
	public String getCreator()
	{
		return getPropertyValue(DC.CREATOR);
	}
	
	////////////////////////////////////////////////
	// upnp:storageMedium
	////////////////////////////////////////////////
	
	public void setStorageMedium(String value)
	{
		setProperty(UPnP.STORAGE_MEDIUM, value);
	}
	
	public String getStorageMedium()
	{
		return getPropertyValue(UPnP.STORAGE_MEDIUM);
	}

	////////////////////////////////////////////////
	// upnp:storageUsed
	////////////////////////////////////////////////
	
	public void setStorageUsed(long value)
	{
		setProperty(UPnP.STORAGE_USED, value);
	}
	
	public long getStorageUsed()
	{
		return getPropertyLongValue(UPnP.STORAGE_USED);
	}

	////////////////////////////////////////////////
	// upnp:storageUsed
	////////////////////////////////////////////////
	
	public void setAlbumArtURI(String value)
	{
		setProperty(UPnP.ALBUMART_URI, value);
	}
	
	public String getAlbumArtURI()
	{
		return getPropertyValue(UPnP.ALBUMART_URI);
	}
	
	////////////////////////////////////////////////
	// isXXXClass
	////////////////////////////////////////////////

	public boolean isMovieClass()
	{
		if (isUPnPClassStartWith("object.item.movie") || isUPnPClassStartWith("object.item.video"))
			return true;
		return false;
	}

	public boolean isVideoClass()
	{
		return isMovieClass();
	}

	public boolean isAudioClass()
	{
		if (isUPnPClassStartWith("object.item.audio") || isUPnPClassStartWith("object.item.music"))
			return true;
		return false;
	}

	public boolean isImageClass()
	{
		if (isUPnPClassStartWith("object.item.image") || isUPnPClassStartWith("object.item.photo"))
			return true;
		return false;
	}
	
	////////////////////////////////////////////////
	// Res
	////////////////////////////////////////////////
	
	private ResourceNodeList resourceNodeList = new ResourceNodeList();
	
	public void addResourceNode(ResourceNode resNode)
	{
		resourceNodeList.add(resNode);
	}
	
	public ResourceNodeList getResourceNodeList()
	{
		return resourceNodeList;
	}
	
	public int getNResourceNodeLists()
	{
		return resourceNodeList.size();
	}
	
	public ResourceNode getResourceNode(int n)
	{
		return resourceNodeList.getResourceNode(n);
	}
	
	public ResourceNode getFirstResource()
	{
		int nProperties = getNResourceNodeLists();
		for (int n=0; n<nProperties; n++) {
			ResourceNode resNode = getResourceNode(n);
			if (resNode.isThumbnail() == false)
				return resNode;
		} 
		return null;
	}
	
	public ResourceNode getThumbnailResource()
	{
		int nProperties = getNResourceNodeLists();
		for (int n=0; n<nProperties; n++) {
			ResourceNode resNode = getResourceNode(n);
			if (resNode.isThumbnail())
				return resNode;
		} 
		return null;
	}
	
	public ResourceNode getSmallImageResource()
	{
		int nProperties = getNResourceNodeLists();
		for (int n=0; n<nProperties; n++) {
			ResourceNode resNode = getResourceNode(n);
			if (resNode.isSmallImage())
				return resNode;
		} 
		return null;
	}
	
	////////////////////////////////////////////////
	// Res (Deprecated)
	////////////////////////////////////////////////
	
	public void setResource(String url, String protocolInfo, AttributeList attrList)
	{
		setProperty(DIDLLite.RES, url);
		
		setPropertyAttribure(DIDLLite.RES, DIDLLite.RES_PROTOCOLINFO, protocolInfo);
		int attrCnt = attrList.size();
		for (int n=0; n<attrCnt; n++) {		
			Attribute attr = attrList.getAttribute(n);
			String name = attr.getName();
			String value = attr.getValue();
			setPropertyAttribure(DIDLLite.RES, name, value);			
		}
	}

	public void setResource(String url, String protocolInfo)
	{
		setResource(url, protocolInfo, new AttributeList());
	}

	public String getResource()
	{
		return getPropertyValue(DIDLLite.RES);
	}
	
	public String getProtocolInfo()
	{
		return getPropertyAttribureValue(DIDLLite.RES, DIDLLite.RES_PROTOCOLINFO);
	}
	
	
	////////////////////////////////////////////////
	// Abstract methods
	////////////////////////////////////////////////
	
	public long getContentLength()
	{
		return 0;
	}

	public InputStream getContentInputStream()
	{
		return null;
	}

	public String getMimeType()
	{
		return "*/*";
	}
	
}

