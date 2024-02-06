/******************************************************************
*
*	MediaPlayer for CyberLink
*
*	Copyright (C) Satoshi Konno 2005
*
*	File : MediaPlayer.java
*
*	09/26/05
*		- first revision.
*	02/05/08
*		- Added getContentDirectory(dev, objectId).
*		- Added browse().
*
******************************************************************/

package org.cybergarage.upnp.std.av.controller;

import org.cybergarage.util.*;
import org.cybergarage.xml.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.UPnP;
import org.cybergarage.upnp.std.av.player.action.BrowseAction;
import org.cybergarage.upnp.std.av.player.action.BrowseResult;
import org.cybergarage.upnp.std.av.server.*;
import org.cybergarage.upnp.std.av.server.object.*;
import org.cybergarage.upnp.std.av.server.object.item.*;
import org.cybergarage.upnp.std.av.server.object.container.*;
import org.cybergarage.upnp.std.av.renderer.*;

public class MediaController extends ControlPoint
{
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	
	public MediaController()
	{
	}

	////////////////////////////////////////////////
	// DeviceList
	////////////////////////////////////////////////

	private DeviceList getDeviceList(String deviceType)
	{
		DeviceList devList = new DeviceList();
		
		DeviceList allDevList = getDeviceList();
		int allDevCnt = allDevList.size();
		for (int n=0; n<allDevCnt; n++) {
			Device dev = allDevList.getDevice(n);
			if (dev.isDeviceType(deviceType) == false)
				continue;
			devList.add(dev);
		}
		return devList;
	}
	
	public DeviceList getServerDeviceList()
	{
		return getDeviceList(MediaServer.DEVICE_TYPE);
	}

	public DeviceList getRendererDeviceList()
	{
		return getDeviceList(MediaRenderer.DEVICE_TYPE);
	}
	
	////////////////////////////////////////////////
	// get*Device
	////////////////////////////////////////////////
	
	public Device getServerDevice(String name)
	{
		Device foundDev = getDevice(name);
		if (foundDev.isDeviceType(MediaServer.DEVICE_TYPE))
			return foundDev;
		return null;
	}

	public Device getRendererDevice(String name)
	{
		Device foundDev = getDevice(name);
		if (foundDev.isDeviceType(MediaRenderer.DEVICE_TYPE))
			return foundDev;
		return null;
	}
	
	////////////////////////////////////////////////
	// browse
	////////////////////////////////////////////////
	
	public Node browse(
			Device dev,
			String objectID,
			String browseFlag,
			String filter,
			int startIndex,
			int requestedCount,
			String sortCaiteria)
	{
		System.out.println("browse " + objectID + ", " + browseFlag + ", " + startIndex + ", " + requestedCount);
		
		if (dev == null)
			return null;
		
		Service conDir = dev.getService(ContentDirectory.SERVICE_TYPE);
		if (conDir == null)
			return null;
		Action action = conDir.getAction(ContentDirectory.BROWSE);
		if (action == null)
			return null;

		BrowseAction browseAction = new BrowseAction(action);
		browseAction.setObjectID(objectID);
		browseAction.setBrowseFlag(browseFlag);
		browseAction.setStartingIndex(startIndex);
		browseAction.setRequestedCount(requestedCount);
		browseAction.setFilter(filter);
		browseAction.setSortCriteria(sortCaiteria);
		if (browseAction.postControlAction() == false)
			return null;

		/*
		 * ContentDirectory:1 Service Template Version 1.01
		 * 2.7.4.2. Argument Descriptions
		 *  RequestedCount ui4 Requested number of entries under the object specified by ObjectID. 
		 *  RequestedCount =0 indicates request all entries.
		 * Added to set the RequestedCount parameter using the NumberReturned result when the specified parameter is zero and
		 * the NumberReturned parameter is less than the TotalMatches parameter for XMBC.
		*/
		if (requestedCount == 0) {
			int numberReturned = browseAction.getNumberReturned();
			int totalMatches = browseAction.getTotalMatches();
			if (numberReturned == 0) {
				if (0 < totalMatches) {
					browseAction.setRequestedCount(totalMatches);
					if (browseAction.postControlAction() == false)
						return null;
				}
				else {
					browseAction.setRequestedCount(9999);
					if (browseAction.postControlAction() == false)
						return null;
				}
			}
		}
		
		Argument resultArg = browseAction.getArgument(BrowseAction.RESULT);
		if (resultArg == null)
			return null;

		String resultStr = resultArg.getValue();
		if (resultStr == null)
			return null;

		Node node = null;

		Parser xmlParser = UPnP.getXMLParser();

		try {
			node = xmlParser.parse(resultStr);
		}
		catch (ParserException pe) {
			Debug.warning(pe);
			return null;
		};
		
		return node;
	}

	////////////////////////////////////////////////
	// browse*
	////////////////////////////////////////////////
	
	public Node browseMetaData(
			Device dev,
			String objectId,
			String filter,
			int startIndex,
			int requestedCount,
			String sortCaiteria)
	{
		return browse(dev, objectId, BrowseAction.BROWSE_METADATA, filter, startIndex, requestedCount, sortCaiteria);
	}

	public Node browseMetaData(
			Device dev,
			String objectId)
	{
		return browseMetaData(dev, objectId, "*", 0, 0, "");
	}
	
	public Node browseDirectChildren(
		Device dev,
		String objectID,
		String filter,
		int startIndex,
		int requestedCount,
		String sortCaiteria)
	{
		return browse(dev, objectID, BrowseAction.BROWSE_DIRECT_CHILDREN, filter, startIndex, requestedCount, sortCaiteria);
	}

	public Node browseDirectChildren(
			Device dev,
			String objectId)
	{
		return browseDirectChildren(dev, objectId, "*", 0, 0, "");
	}
	
	////////////////////////////////////////////////
	// Content
	////////////////////////////////////////////////
	
	public ContainerNode browse(Device dev)
	{
		return browse(dev, "0");
	}
	
	public ContainerNode browse(Device dev, String objectId)
	{
		return browse(dev, objectId, false);
	}
	
	public ContainerNode browse(Device dev, String objectId, boolean hasBrowseChildNodes, boolean hasRootNodeMetadata)
	{		
		ContainerNode contentRootNode = new RootNode();

		if (hasRootNodeMetadata) {
			Node rootNode = browseMetaData(dev, objectId, "*", 0, 0, "");
			if (rootNode != null)
				contentRootNode.set(rootNode);
		}
		
		browse(contentRootNode, dev, objectId, hasRootNodeMetadata);

		return contentRootNode;
	}
	
	public ContainerNode browse(Device dev, String objectId, boolean hasBrowseChildNodes)
	{
		return browse(dev, objectId, hasBrowseChildNodes, false);
	}

	private int browse(ContainerNode parentNode, Device dev, String objectID, boolean hasBrowseChildNodes)
	{
		if (objectID == null)
			return 0;

		Node resultNode = browseDirectChildren(dev, objectID, "*", 0, 0, "");
		if (resultNode == null)
			return 0;
		
		BrowseResult browseResult = new BrowseResult(resultNode);
		int nResultNode = 0;
		int nContents = browseResult.getNContentNodes();
		for (int n=0; n<nContents; n++) {
			Node xmlNode = browseResult.getContentNode(n);
			ContentNode contentNode = null;
			if (ContainerNode.isContainerNode(xmlNode) == true) {
				contentNode = new ContainerNode();
			}
			else if (ItemNode.isItemNode(xmlNode) == true)
				contentNode = new ItemNode();
			if (contentNode == null)
				continue;
			contentNode.set(xmlNode);
			parentNode.addContentNode(contentNode);
			contentNode.setParentID(objectID);
			nResultNode++;
			// Add Child Nodes
			if (hasBrowseChildNodes) {
				if (contentNode.isContainerNode() == true) {
					ContainerNode containerNode = (ContainerNode)contentNode;
					int childCnt = containerNode.getChildCount();
					if (0 < childCnt) {
						String objid = containerNode.getID();
						browse(containerNode, dev, objid, true);
					}
				}
			}
		}

		return nResultNode;
	}
	
	////////////////////////////////////////////////
	// Content
	////////////////////////////////////////////////
	
	public ContentNode getContentDirectory(Device dev)
	{
		return getContentDirectory(dev, "0");
	}

	public ContentNode getContentDirectory(Device dev, String objectId)
	{
		return browse(dev, objectId, true);
	}
	
	////////////////////////////////////////////////
	// main
	////////////////////////////////////////////////

	public void printContentNode(ContentNode node, int indentLevel)
	{
			int n;
			
			for (n=0; n<indentLevel; n++)
				System.out.print("  ");
			System.out.print(node.getTitle());
			if (node.isItemNode() == true) {
				ItemNode itemNode = (ItemNode)node;
				String res = itemNode.getResource();
				String protocolInfo = itemNode.getProtocolInfo();
				System.out.print(" (" + res + ")");
			}
			System.out.println("");
			
			if (node.isContainerNode()) {
				ContainerNode containerNode = (ContainerNode)node;
				int nContentNodes = containerNode.getNContentNodes();
				for (n=0; n<nContentNodes; n++) {
					ContentNode cnode = containerNode.getContentNode(n);
					printContentNode(cnode, indentLevel+1);
				}
			}
	}

	public void printContentDirectory(Device dev)
	{
			ContentNode node = getContentDirectory(dev);
			if (node == null)
				return;
			//node->print();
			printContentNode(node, 1);
	}
	
	public void printMediaServers()
	{
			DeviceList devList = getDeviceList();
			int devCnt = devList.size();
			int mediaServerCnt = 0;
			for (int n=0; n<devCnt; n++) {
				Device dev = devList.getDevice(n);
				if (dev.isDeviceType(MediaServer.DEVICE_TYPE) == false)
					continue;
				System.out.println("[" + n +  "] " + dev.getFriendlyName() + ", " + dev.getLeaseTime() + ", " + dev.getElapsedTime());
				printContentDirectory(dev);
				mediaServerCnt++;
			}		
			if (mediaServerCnt == 0) {
				System.out.println("MediaServer is not found");
			}
	}
	
	////////////////////////////////////////////////
	// play
	////////////////////////////////////////////////
	
	public boolean setAVTransportURI(
			Device dev,
			ItemNode itemNode)
	{
		if (dev == null)
			return false;
		
		ResourceNode resNode = itemNode.getFirstResource();
		if (resNode == null)
			return false;
		String resURL = resNode.getURL();
		if (resURL == null || resURL.length() <= 0)
			return false;
		
		Service avTransService = dev.getService(AVTransport.SERVICE_TYPE);
		if (avTransService == null)
			return false;
		
		Action action = avTransService.getAction(AVTransport.SETAVTRANSPORTURI);
		if (action == null)
			return false;
		
		action.setArgumentValue(AVTransport.INSTANCEID, "0");
		action.setArgumentValue(AVTransport.CURRENTURI, resURL);
		action.setArgumentValue(AVTransport.CURRENTURIMETADATA, "");
		
		return action.postControlAction();
	}
	
	public boolean play(Device dev)
	{
		if (dev == null)
			return false;
		
		Service avTransService = dev.getService(AVTransport.SERVICE_TYPE);
		if (avTransService == null)
			return false;
		
		Action action = avTransService.getAction(AVTransport.PLAY);
		if (action == null)
			return false;
		
		action.setArgumentValue(AVTransport.INSTANCEID, "0");
		action.setArgumentValue(AVTransport.SPEED, "1");
		
		return action.postControlAction();
	}

	public boolean stop(Device dev)
	{
		if (dev == null)
			return false;
		
		Service avTransService = dev.getService(AVTransport.SERVICE_TYPE);
		if (avTransService == null)
			return false;
		
		Action action = avTransService.getAction(AVTransport.STOP);
		if (action == null)
			return false;
		
		action.setArgumentValue(AVTransport.INSTANCEID, "0");
		
		return action.postControlAction();
	}
	
	public boolean play(
			Device dev,
			ItemNode itemNode)
	{
		stop(dev);
		if (setAVTransportURI(dev, itemNode) == false)
			return false;
		return play(dev);
	}
	
	////////////////////////////////////////////////
	// main
	////////////////////////////////////////////////

	public static void main(String args[]) 
	{
		MediaController mplayer = new MediaController();
		
		mplayer.start();
		
		while(true) {
			mplayer.printMediaServers();
			try {
				Thread.sleep(1000*20);
			}
			catch (Exception e) {};
		}
		
		//mplayer.stop();
	}
}

