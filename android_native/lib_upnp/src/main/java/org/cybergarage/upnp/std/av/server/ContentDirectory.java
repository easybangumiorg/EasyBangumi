/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File : ContentDirectory
*
*	Revision:
*
*	10/22/03
*		- first revision.
*	03/02/04
*		- Fixed a bug when update() is executed because the root node's
*		  ContentDirectory is null. 
*	03/12/04
*		- Thanks for Robert Johansson <robert.johansson@kreatel.se>
*		- I ran into the problem that the system can not send big files. 
*		  It uses FileUtil.Load to load files. If the file is too big 
*		  we get an OutOfMemory exception.
*	04/05/04
*		- Added getFormatMimeTypes(), Deleted getFormat() and getNFormats() for C++ porting.
*	04/27/04
*		- Changed getContentExportURL() usint the string ID.
*	06/20/04
*		- Changed contentExportRequestRecieved() to set the ConnectionInfo.
*	08/07/04
*		- Implemented for GetSearchCapabilities request.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server;

import java.io.*;
import java.util.*;

import org.cybergarage.util.*;
import org.cybergarage.http.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.control.*;
import org.cybergarage.upnp.std.av.server.action.*;
import org.cybergarage.upnp.std.av.server.object.*;
import org.cybergarage.upnp.std.av.server.object.container.*;
import org.cybergarage.upnp.std.av.server.object.item.*;
import org.cybergarage.upnp.std.av.server.object.sort.*;
import org.cybergarage.upnp.std.av.server.object.search.*;

public class ContentDirectory extends ThreadCore implements ActionListener, QueryListener
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////

	public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:ContentDirectory:1";
	
	// Browse Action	
	public final static String TRANSFERIDS = "TransferIDs";
	public final static String SEARCHCAPABILITIES = "SearchCapabilities";
	public final static String SORTCAPABILITIES = "SortCapabilities";
	public final static String SYSTEMUPDATEID = "SystemUpdateID";
	public final static String CONTAINERUPDATEIDS = "ContainerUpdateIDs";
	public final static String GETSEARCHCAPABILITIES = "GetSearchCapabilities";
	public final static String SEARCHCAPS = "SearchCaps";
	public final static String GETSORTCAPABILITIES = "GetSortCapabilities";
	public final static String SORTCAPS = "SortCaps";
	public final static String GETSYSTEMUPDATEID = "GetSystemUpdateID";
	public final static String ID = "Id";
	public final static String BROWSE = "Browse";
	public final static String OBJECTID = "ObjectID";
	public final static String BROWSEFLAG = "BrowseFlag";
	public final static String FILTER = "Filter";
	public final static String STARTINGINDEX = "StartingIndex";
	public final static String REQUESTEDCOUNT = "RequestedCount";
	public final static String SORTCRITERIA = "SortCriteria";
	public final static String RESULT = "Result";
	public final static String NUMBERRETURNED = "NumberReturned";
	public final static String TOTALMATCHES = "TotalMatches";
	public final static String UPDATEID = "UpdateID";
	public final static String SEARCH = "Search";
	public final static String CONTAINERID = "ContainerID";
	public final static String SEARCHCRITERIA = "SearchCriteria";
	public final static String CREATEOBJECT = "CreateObject";
	public final static String ELEMENTS = "Elements";
	public final static String DESTROYOBJECT = "DestroyObject";
	public final static String UPDATEOBJECT = "UpdateObject";
	public final static String CURRENTTAGVALUE = "CurrentTagValue";
	public final static String NEWTAGVALUE = "NewTagValue";
	public final static String IMPORTRESOURCE = "ImportResource";
	public final static String SOURCEURI = "SourceURI";
	public final static String DESTINATIONURI = "DestinationURI";
	public final static String TRANSFERID = "TransferID";
	public final static String EXPORTRESOURCE = "ExportResource";
	public final static String STOPTRANSFERRESOURCE = "StopTransferResource";
	public final static String GETTRANSFERPROGRESS = "GetTransferProgress";
	public final static String TRANSFERSTATUS = "TransferStatus";
	public final static String TRANSFERLENGTH = "TransferLength";
	public final static String TRANSFERTOTAL = "TransferTotal";
	public final static String DELETERESOURCE = "DeleteResource";
	public final static String RESOURCEURI = "ResourceURI";
	public final static String CREATEREFERENCE = "CreateReference";
	public final static String NEWID = "NewID";
	
	public final static String BROWSEMETADATA = "BrowseMetadata";
	public final static String BROWSEDIRECTCHILDREN = "BrowseDirectChildren";
	public final static String COMPLETED = "COMPLETED";
	public final static String ERROR = "ERROR";
	public final static String IN_PROGRESS = "IN_PROGRESS";
	public final static String STOPPED = "STOPPED";
	
	public final static String CONTENT_EXPORT_URI = "/ExportContent";
	public final static String CONTENT_IMPORT_URI = "/ImportContent";
	public final static String CONTENT_ID = "id";
	
	public final static String SCPD = 
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
		"<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\">\n" + 
		"   <specVersion>\n" + 
		"      <major>1</major>\n" + 
		"      <minor>0</minor>\n" + 
		"   </specVersion>\n" + 
		"   <actionList>\n" + 
		"      <action>\n" + 
		"         <name>ExportResource</name>\n" + 
		"         <argumentList>\n" + 
		"            <argument>\n" + 
		"               <name>SourceURI</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_URI</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"            <argument>\n" + 
		"               <name>DestinationURI</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_URI</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"            <argument>\n" + 
		"               <name>TransferID</name>\n" + 
		"               <direction>out</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_TransferID</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"         </argumentList>\n" + 
		"      </action>\n" + 
		"      <action>\n" + 
		"         <name>StopTransferResource</name>\n" + 
		"         <argumentList>\n" + 
		"            <argument>\n" + 
		"               <name>TransferID</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_TransferID</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"         </argumentList>\n" + 
		"      </action>\n" + 
		"      <action>\n" + 
		"         <name>DestroyObject</name>\n" + 
		"         <argumentList>\n" + 
		"            <argument>\n" + 
		"               <name>ObjectID</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"         </argumentList>\n" + 
		"      </action>\n" + 
		"      <action>\n" + 
		"         <name>DeleteResource</name>\n" + 
		"         <argumentList>\n" + 
		"            <argument>\n" + 
		"               <name>ResourceURI</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_URI</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"         </argumentList>\n" + 
		"      </action>\n" + 
		"      <action>\n" + 
		"         <name>UpdateObject</name>\n" + 
		"         <argumentList>\n" + 
		"            <argument>\n" + 
		"               <name>ObjectID</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"            <argument>\n" + 
		"               <name>CurrentTagValue</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_TagValueList</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"            <argument>\n" + 
		"               <name>NewTagValue</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_TagValueList</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"         </argumentList>\n" + 
		"      </action>\n" + 
		"      <action>\n" + 
		"         <name>Browse</name>\n" + 
		"         <argumentList>\n" + 
		"            <argument>\n" + 
		"               <name>ObjectID</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"            <argument>\n" + 
		"               <name>BrowseFlag</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_BrowseFlag</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"            <argument>\n" + 
		"               <name>Filter</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_Filter</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"            <argument>\n" + 
		"               <name>StartingIndex</name>\n" + 
		"               <direction>in</direction>\n" + 
		"               <relatedStateVariable>A_ARG_TYPE_Index</relatedStateVariable>\n" + 
		"            </argument>\n" + 
		"            <argument>\n" + 
		"               <name>RequestedCount</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>SortCriteria</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_SortCriteria</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>Result</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Result</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>NumberReturned</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>TotalMatches</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>UpdateID</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_UpdateID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"         </argumentList>\n" +
		"      </action>\n" +
		"      <action>\n" +
		"         <name>GetTransferProgress</name>\n" +
		"         <argumentList>\n" +
		"            <argument>\n" +
		"               <name>TransferID</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_TransferID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>TransferStatus</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_TransferStatus</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>TransferLength</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_TransferLength</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>TransferTotal</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_TransferTotal</relatedStateVariable>\n" +
		"            </argument>\n" +
		"         </argumentList>\n" +
		"      </action>\n" +
		"      <action>\n" +
		"         <name>GetSearchCapabilities</name>\n" +
		"         <argumentList>\n" +
		"            <argument>\n" +
		"               <name>SearchCaps</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>SearchCapabilities</relatedStateVariable>\n" +
		"            </argument>\n" +
		"         </argumentList>\n" +
		"      </action>\n" +
		"      <action>\n" +
		"         <name>CreateObject</name>\n" +
		"         <argumentList>\n" +
		"            <argument>\n" +
		"               <name>ContainerID</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>Elements</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Result</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>ObjectID</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>Result</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Result</relatedStateVariable>\n" +
		"            </argument>\n" +
		"         </argumentList>\n" +
		"      </action>\n" +
		"      <action>\n" +
		"         <name>Search</name>\n" +
		"         <argumentList>\n" +
		"            <argument>\n" +
		"               <name>ContainerID</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>SearchCriteria</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_SearchCriteria</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>Filter</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Filter</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>StartingIndex</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Index</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>RequestedCount</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>SortCriteria</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_SortCriteria</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>Result</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Result</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>NumberReturned</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>TotalMatches</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>UpdateID</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_UpdateID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"         </argumentList>\n" +
		"      </action>\n" +
		"      <action>\n" +
		"         <name>GetSortCapabilities</name>\n" +
		"         <argumentList>\n" +
		"            <argument>\n" +
		"               <name>SortCaps</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>SortCapabilities</relatedStateVariable>\n" +
		"            </argument>\n" +
		"         </argumentList>\n" +
		"      </action>\n" +
		"      <action>\n" +
		"         <name>ImportResource</name>\n" +
		"         <argumentList>\n" +
		"            <argument>\n" +
		"               <name>SourceURI</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_URI</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>DestinationURI</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_URI</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>TransferID</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_TransferID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"         </argumentList>\n" +
		"      </action>\n" +
		"      <action>\n" +
		"         <name>CreateReference</name>\n" +
		"         <argumentList>\n" +
		"            <argument>\n" +
		"               <name>ContainerID</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>ObjectID</name>\n" +
		"               <direction>in</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"            <argument>\n" +
		"               <name>NewID</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"         </argumentList>\n" +
		"      </action>\n" +
		"      <action>\n" +
		"         <name>GetSystemUpdateID</name>\n" +
		"         <argumentList>\n" +
		"            <argument>\n" +
		"              <name>Id</name>\n" +
		"               <direction>out</direction>\n" +
		"               <relatedStateVariable>SystemUpdateID</relatedStateVariable>\n" +
		"            </argument>\n" +
		"         </argumentList>\n" +
		"      </action>\n" +
		"   </actionList>\n" +
		"   <serviceStateTable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_SortCriteria</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_TransferLength</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"yes\">\n" +
		"         <name>TransferIDs</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_UpdateID</name>\n" +
		"         <dataType>ui4</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_SearchCriteria</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_Filter</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"yes\">\n" +
		"         <name>ContainerUpdateIDs</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_Result</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_Index</name>\n" +
		"         <dataType>ui4</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_TransferID</name>\n" +
		"         <dataType>ui4</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_TagValueList</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_URI</name>\n" +
		"         <dataType>uri</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_ObjectID</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>SortCapabilities</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>SearchCapabilities</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_Count</name>\n" +
		"         <dataType>ui4</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_BrowseFlag</name>\n" +
		"         <dataType>string</dataType>\n" +
		"         <allowedValueList>\n" +
		"            <allowedValue>BrowseMetadata</allowedValue>\n" +
		"            <allowedValue>BrowseDirectChildren</allowedValue>\n" +
		"         </allowedValueList>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"yes\">\n" +
		"         <name>SystemUpdateID</name>\n" +
		"         <dataType>ui4</dataType>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_TransferStatus</name>\n" +
		"         <dataType>string</dataType>\n" +
		"         <allowedValueList>\n" +
		"            <allowedValue>COMPLETED</allowedValue>\n" +
		"            <allowedValue>ERROR</allowedValue>\n" +
		"            <allowedValue>IN_PROGRESS</allowedValue>\n" +
		"            <allowedValue>STOPPED</allowedValue>\n" +
		"         </allowedValueList>\n" +
		"      </stateVariable>\n" +
		"      <stateVariable sendEvents=\"no\">\n" +
		"         <name>A_ARG_TYPE_TransferTotal</name>\n" +
		"         <dataType>string</dataType>\n" +
		"      </stateVariable>\n" +
		"   </serviceStateTable>\n" +
		"</scpd>";
	
	////////////////////////////////////////////////
	// Constructor 
	////////////////////////////////////////////////
	
	public ContentDirectory(MediaServer mserver)
	{
		setMediaServer(mserver);
		
		systemUpdateID = 0;
		maxContentID = 0;
		
		setSystemUpdateInterval(DEFAULT_SYSTEMUPDATEID_INTERVAL);
		setContentUpdateInterval(DEFAULT_CONTENTUPDATE_INTERVAL);
		
		initRootNode();
		initSortCaps();
		initSearchCaps();
	}
	
	////////////////////////////////////////////////
	// Media Server
	////////////////////////////////////////////////

	private MediaServer mediaServer;
	
	private void setMediaServer(MediaServer mserver)
	{
		mediaServer = mserver;	
	}
	
	public MediaServer getMediaServer()
	{
		return mediaServer;	
	}
	
	////////////////////////////////////////////////
	// Mutex
	////////////////////////////////////////////////
	
	private Mutex mutex = new Mutex();
	
	public void lock()
	{
		mutex.lock();
	}
	
	public void unlock()
	{
		mutex.unlock();
	}
	
	////////////////////////////////////////////////
	// ContentID
	////////////////////////////////////////////////
	
	private int systemUpdateID;
	
	public synchronized void updateSystemUpdateID()
	{
		systemUpdateID++;
	}

	public synchronized int getSystemUpdateID()
	{
		return systemUpdateID;
	}
	
	////////////////////////////////////////////////
	// ContentID
	////////////////////////////////////////////////
	
	private int maxContentID;
	
	private synchronized int getNextContentID()
	{
		maxContentID++;
		return maxContentID;
	}

	public int getNextItemID()
	{
		return getNextContentID();
	}
	
	public int getNextContainerID()
	{
		return getNextContentID();
	}
	
	////////////////////////////////////////////////
	// Root Node
	////////////////////////////////////////////////
	
	private RootNode rootNode;
	
	private void initRootNode()
	{
		rootNode = new RootNode();
		rootNode.setContentDirectory(this);
	}
	
	public RootNode getRootNode()
	{
		return rootNode;
	}

	////////////////////////////////////////////////
	// Container/Item Node
	////////////////////////////////////////////////
	
	private ContainerNode createContainerNode()
	{
		ContainerNode node = new ContainerNode();
		return node;
	}
	
	////////////////////////////////////////////////
	// Format
	////////////////////////////////////////////////
	
	private FormatList formatList = new FormatList();
	
	public boolean addPlugIn(Format format)
	{
		formatList.add(format);
		return true;
	}

	public Format getFormat(File file)
	{
		return formatList.getFormat(file);
	}

	public Format getFormat(int n)
	{
		return formatList.getFormat(n);
	}
	
	public int getNFormats()
	{
		return formatList.size();
	}
	
	/*
	public String []getFormatMimeTypes()
	{
		int formatCnt = formatList.size();
		String mimeType[] = new String[formatCnt];
		for (int n=0; n<formatCnt; n++) {
			Format format = formatList.getFormat(n);
			mimeType[n] = format.getMimeType();
		}
		return mimeType;
	}
	*/
	
	////////////////////////////////////////////////
	// SortCap
	////////////////////////////////////////////////
	
	private SortCapList sortCapList = new SortCapList();
	
	public boolean addSortCap(SortCap sortCap)
	{
		sortCapList.add(sortCap);
		return true;
	}

	public int getNSortCaps()
	{
		return sortCapList.size();
	}
	
	public SortCap getSortCap(int n)
	{
		return sortCapList.getSortCap(n);
	}
	
	public SortCap getSortCap(String type)
	{
		return sortCapList.getSortCap(type);
	}

	private void initSortCaps()
	{
		addSortCap(new UPnPClassSortCap());
		addSortCap(new DCTitleSortCap());
		addSortCap(new DCDateSortCap());
	}
	
	private String getSortCapabilities()
	{
		String sortCapsStr = "";
		int nSortCaps = getNSortCaps();
		for (int n=0; n<nSortCaps; n++) {
			SortCap sortCap = getSortCap(n);
			String type = sortCap.getType();
			if (0 < n)
				sortCapsStr += ",";
			sortCapsStr += type;
		}
		return sortCapsStr;
	}
	
	////////////////////////////////////////////////
	// SearchCap
	////////////////////////////////////////////////
	
	private SearchCapList searchCapList = new SearchCapList();
	
	public boolean addSearchCap(SearchCap searchCap)
	{
		searchCapList.add(searchCap);
		return true;
	}

	public SearchCapList getSearchCapList()
	{
		return searchCapList;
	}
	
	public int getNSearchCaps()
	{
		return searchCapList.size();
	}
	
	public SearchCap getSearchCap(int n)
	{
		return searchCapList.getSearchCap(n);
	}
	
	public SearchCap getSearchCap(String type)
	{
		return searchCapList.getSearchCap(type);
	}

	private void initSearchCaps()
	{
		addSearchCap(new IdSearchCap());
		addSearchCap(new TitleSearchCap());
	}
	
	private String getSearchCapabilities()
	{
		String searchCapsStr = "";
		int nSearchCaps = getNSearchCaps();
		for (int n=0; n<nSearchCaps; n++) {
			SearchCap searchCap = getSearchCap(n);
			String type = searchCap.getPropertyName();
			if (0 < n)
				searchCapsStr += ",";
			searchCapsStr += type;
		}
		return searchCapsStr;
	}
	
	////////////////////////////////////////////////
	// Directory
	////////////////////////////////////////////////
	
	private DirectoryList dirList = new DirectoryList();
	
	private DirectoryList getDirectoryList()
	{
		return dirList;
	}
	
	public boolean addDirectory(Directory dir)
	{
		dir.setContentDirectory(this);
		dir.setID(getNextContainerID());
		dir.updateContentList();
		dirList.add(dir);
		rootNode.addContentNode(dir);
		
		//Update SysteUpdateID
		updateSystemUpdateID();
		
		return true;
	}

	public boolean removeDirectory(String name)
	{	
		Directory dirNode = dirList.getDirectory(name);
		if (dirNode == null)
			return false;
		dirList.remove(dirNode);
		rootNode.removeNode(dirNode);

		//Update SysteUpdateID
		updateSystemUpdateID();
		
		return true;
	}

	public boolean removeAllDirectories()
	{	
		dirList.removeAllElements();
		return true;
	}
	
	public int getNDirectories()
	{
		return dirList.size();
	}
	
	public Directory getDirectory(int n)
	{
		return dirList.getDirectory(n);
	}

	////////////////////////////////////////////////
	// findContentNodeBy*
	////////////////////////////////////////////////
	
	public ContentNode findContentNodeByID(String id)
	{
		return getRootNode().findContentNodeByID(id);		
	}

	////////////////////////////////////////////////
	// ActionListener
	////////////////////////////////////////////////

	public boolean actionControlReceived(Action action)
	{
		//action.print();
		
		String actionName = action.getName();
		
		if (actionName.equals(BROWSE) == true) {
			BrowseAction browseAct = new BrowseAction(action);
			return browseActionReceived(browseAct);
		}
		
		if (actionName.equals(SEARCH) == true) {
			SearchAction searchAct = new SearchAction(action);
			return searchActionReceived(searchAct);
		}
		
		//@id,@parentID,dc:title,dc:date,upnp:class,res@protocolInfo
		if (actionName.equals(GETSEARCHCAPABILITIES) == true) {
			Argument searchCapsArg = action.getArgument(SEARCHCAPS);
			String searchCapsStr = getSearchCapabilities();
			searchCapsArg.setValue(searchCapsStr);
			return true;
		}

		//dc:title,dc:date,upnp:class
		if (actionName.equals(GETSORTCAPABILITIES) == true) {
			Argument sortCapsArg = action.getArgument(SORTCAPS);
			String sortCapsStr = getSortCapabilities();
			sortCapsArg.setValue(sortCapsStr);
			return true;
		}
		
		if (actionName.equals(GETSYSTEMUPDATEID) == true) {
			Argument idArg = action.getArgument(ID);
			idArg.setValue(getSystemUpdateID());
			return true;
		}
		
		return false;		
	}

	////////////////////////////////////////////////
	// Browse
	////////////////////////////////////////////////

	private boolean browseActionReceived(BrowseAction action)
	{
		if (action.isMetadata() == true)
			return browseMetadataActionReceived(action);
		if (action.isDirectChildren() == true)
			return browseDirectChildrenActionReceived(action);
		return false;		
	}

	////////////////////////////////////////////////
	// Browse (MetaData)
	////////////////////////////////////////////////
	
	private boolean browseMetadataActionReceived(BrowseAction action)
	{
		String objID = action.getObjectID();
		ContentNode node = findContentNodeByID(objID);
		if (node == null)
			return false;

		DIDLLite didlLite = new DIDLLite();
		didlLite.setContentNode(node);
		String result = didlLite.toString();
		
		action.setArgumentValue(BrowseAction.RESULT, result);
		action.setArgumentValue(BrowseAction.NUMBER_RETURNED, 1);
		action.setArgumentValue(BrowseAction.TOTAL_MACHES, 1);
		action.setArgumentValue(BrowseAction.UPDATE_ID, getSystemUpdateID());
		
		if (Debug.isOn())
			action.print();
		
		return true;
	}
		
	////////////////////////////////////////////////
	// Browse (DirectChildren/Sort)
	////////////////////////////////////////////////

	private void sortContentNodeList(ContentNode conNode[], SortCap sortCap, boolean ascSeq)
	{	
		// Selection Sort
		int nConNode = conNode.length;
		for (int i=0; i<(nConNode-1); i++) {
			int selIdx = i;
			for (int j=(i+1); j<nConNode; j++) {
				int cmpRet = sortCap.compare(conNode[selIdx], conNode[j]);
				if (ascSeq == true && cmpRet < 0)
					selIdx = j;
				if (ascSeq == false && 0 < cmpRet)
					selIdx = j;
			}
			ContentNode conTmp = conNode[i];
			conNode[i] = conNode[selIdx];
			conNode[selIdx] = conTmp;
		}
	}
	
	private SortCriterionList getSortCriteriaArray(String sortCriteria)
	{
		SortCriterionList sortCriList = new SortCriterionList();
		StringTokenizer st = new StringTokenizer(sortCriteria, ", ");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			sortCriList.add(token);
		}
		return sortCriList;
	}
	
	private ContentNodeList sortContentNodeList(ContentNodeList contentNodeList, String sortCriteria)
	{	
		if (sortCriteria == null || sortCriteria.length() <= 0)
			return contentNodeList;
			
		int nChildNodes = contentNodeList.size();
		ContentNode conNode[] = new ContentNode[nChildNodes];
		for (int n=0; n<nChildNodes; n++)
			conNode[n] = contentNodeList.getContentNode(n);
		
		SortCriterionList sortCritList = getSortCriteriaArray(sortCriteria);
		int nSortCrit = sortCritList.size();
		for (int n=0; n<nSortCrit; n++) {
			String sortStr = sortCritList.getSortCriterion(n);
			Debug.message("[" + n + "] = " + sortStr);
			boolean ascSeq = true;
			char firstSortChar = sortStr.charAt(0);
			if (firstSortChar == '-')
				ascSeq = false;
			if (firstSortChar == '+' || firstSortChar == '-')
				sortStr = sortStr.substring(1);
			SortCap sortCap = getSortCap(sortStr);
			if (sortCap == null)
				continue;
			Debug.message("  ascSeq = " + ascSeq);
			Debug.message("  sortCap = " + sortCap.getType());
			sortContentNodeList(conNode, sortCap, ascSeq);
		}
		
		ContentNodeList sortedContentNodeList = new ContentNodeList();
		for (int n=0; n<nChildNodes; n++)
			sortedContentNodeList.add(conNode[n]);
		return sortedContentNodeList;
	}
	
	////////////////////////////////////////////////
	// Browse (DirectChildren)
	////////////////////////////////////////////////
	
	private boolean browseDirectChildrenActionReceived(BrowseAction action)
	{
		String objID = action.getObjectID();
		ContentNode node = findContentNodeByID(objID);
		if (node == null || node.isContainerNode() == false)
			return false;
	
		ContainerNode containerNode = (ContainerNode)node;
		ContentNodeList contentNodeList = new ContentNodeList();
		int nChildNodes = containerNode.getNContentNodes();
		for (int n=0; n<nChildNodes; n++) {
			ContentNode cnode = containerNode.getContentNode(n);
			contentNodeList.add(cnode);
		}

		// Sort Content Node Lists
		String sortCriteria = action.getSortCriteria();
		ContentNodeList sortedContentNodeList = sortContentNodeList(contentNodeList, sortCriteria);
			
		int startingIndex = action.getStartingIndex();
		if (startingIndex <= 0)
			startingIndex = 0;
		int requestedCount = action.getRequestedCount();
		if (requestedCount == 0)
			requestedCount = nChildNodes;
			
		DIDLLite didlLite = new DIDLLite();
		int numberReturned = 0;
		for (int n=startingIndex; (n<nChildNodes && numberReturned<requestedCount); n++) {
			ContentNode cnode = sortedContentNodeList.getContentNode(n);
			didlLite.addContentNode(cnode);
			cnode.setParentID(objID);
			numberReturned++;
		}
		
		String result = didlLite.toString();		
		action.setResult(result);
		action.setNumberReturned(numberReturned);
		action.setTotalMaches(nChildNodes);
		action.setUpdateID(getSystemUpdateID());
		
		return true;
	}

	////////////////////////////////////////////////
	// Search
	////////////////////////////////////////////////

	private SearchCriteriaList getSearchCriteriaList(String searchStr)
	{
		SearchCriteriaList searchList = new SearchCriteriaList();

		if (searchStr == null)
			return searchList;
		if (searchStr.compareTo("*") == 0)
			return searchList;

		StringTokenizer searchCriTokenizer = new StringTokenizer(searchStr, SearchCriteria.WCHARS);
			while (searchCriTokenizer.hasMoreTokens() == true) {
			String prop = searchCriTokenizer.nextToken();
			if (searchCriTokenizer.hasMoreTokens() == false)
				break;
			String binOp = searchCriTokenizer.nextToken();
			if (searchCriTokenizer.hasMoreTokens() == false)
				break;
			String value = searchCriTokenizer.nextToken();
			value = StringUtil.trim(value, "\"");
			String logOp = "";
			if (searchCriTokenizer.hasMoreTokens() == true)
				logOp = searchCriTokenizer.nextToken();
			SearchCriteria searchCri = new SearchCriteria();
			searchCri.setProperty(prop);
			searchCri.setOperation(binOp);
			searchCri.setValue(value);
			searchCri.setLogic(logOp);
			searchList.add(searchCri);
		}

		return searchList;
	}
	
	private int getSearchContentList(ContainerNode node, SearchCriteriaList searchCriList, SearchCapList searchCapList, ContentNodeList contentNodeList)
	{
		if (searchCriList.compare(node, searchCapList) == true)
			contentNodeList.add(node);

		int nChildNodes = node.getNContentNodes();
		for (int n=0; n<nChildNodes; n++) {
			ContentNode cnode = node.getContentNode(n);
			if (cnode.isContainerNode())
				getSearchContentList((ContainerNode)cnode, searchCriList, searchCapList, contentNodeList);
		}
		return contentNodeList.size();
	}

	private boolean searchActionReceived(SearchAction action)
	{
		String contaierID = action.getContainerID();
		ContentNode node = findContentNodeByID(contaierID);
		if (node == null || node.isContainerNode() == false)
			return false;

		ContainerNode containerNode = (ContainerNode)node;
		String searchCriteria = action.getSearchCriteria();
		SearchCriteriaList searchCriList = getSearchCriteriaList(searchCriteria);
		SearchCapList searchCapList = getSearchCapList();

		int n;
		ContentNodeList contentNodeList = new ContentNodeList();
		int nChildNodes = containerNode.getNContentNodes();
		for (n=0; n<nChildNodes; n++) {
			ContentNode cnode = containerNode.getContentNode(n);
			if (cnode.isContainerNode())
				getSearchContentList((ContainerNode)cnode, searchCriList, searchCapList, contentNodeList);
		}
		nChildNodes = contentNodeList.size();

		// Sort Content Node Lists
		String sortCriteria = action.getSortCriteria();
		ContentNodeList sortedContentNodeList = sortContentNodeList(contentNodeList, sortCriteria);

		int startingIndex = action.getStartingIndex();
		if (startingIndex <= 0)
			startingIndex = 0;
		int requestedCount = action.getRequestedCount();
		if (requestedCount == 0)
			requestedCount = nChildNodes;

		DIDLLite didlLite = new DIDLLite();
		int numberReturned = 0;
		for (n=startingIndex; (n<nChildNodes && numberReturned<requestedCount); n++) {
			ContentNode cnode = sortedContentNodeList.getContentNode(n);
			didlLite.addContentNode(cnode);
			numberReturned++;
		}

		String result = didlLite.toString();
		action.setResult(result);
		action.setNumberReturned(numberReturned);
		action.setTotalMaches(nChildNodes);
		action.setUpdateID(getSystemUpdateID());

		return true;
	}
	
	////////////////////////////////////////////////
	// QueryListener
	////////////////////////////////////////////////

	public boolean queryControlReceived(StateVariable stateVar)
	{
		return false;
	}
	
	////////////////////////////////////////////////
	//	HTTP Server	
	////////////////////////////////////////////////

	public void contentExportRequestRecieved(HTTPRequest httpReq)
	{
		String uri = httpReq.getURI();
		if (uri.startsWith(CONTENT_EXPORT_URI) == false) {
			httpReq.returnBadRequest();
			return;
		}

		ParameterList paramList = httpReq.getParameterList();
		for (int n=0; n<paramList.size(); n++) {
			Parameter param = paramList.getParameter(n);
			Debug.message("[" + param.getName() + "] = " + param.getValue());
		}
		
		////////////////////////////////////////
		// Getting item ID
		////////////////////////////////////////
		
		String id = paramList.getValue(CONTENT_ID);
		
		////////////////////////////////////////
		// Has Item Node ?
		////////////////////////////////////////
		
		ContentNode node = findContentNodeByID(id);
		if (node == null) {
			httpReq.returnBadRequest();
			return;
		}
		if (!(node instanceof ItemNode)) {
			httpReq.returnBadRequest();
			return;
		}
			
		////////////////////////////////////////
		// Return item content
		////////////////////////////////////////
				

		ItemNode itemNode = (ItemNode)node;

		long contentLen = itemNode.getContentLength();
		String contentType = itemNode.getMimeType();
		InputStream contentIn = itemNode.getContentInputStream();		
		
		if (contentLen <= 0 || contentType.length() <= 0 || contentIn == null) {
			httpReq.returnBadRequest();
			return;
		}

		MediaServer mserver = getMediaServer();
		ConnectionManager conMan = mserver.getConnectionManager();
		int conID = conMan.getNextConnectionID();
		ConnectionInfo conInfo = new ConnectionInfo(conID);
		conInfo.setProtocolInfo(contentType);
		conInfo.setDirection(ConnectionInfo.OUTPUT);
		conInfo.setStatus(ConnectionInfo.OK);
		conMan.addConnectionInfo(conInfo);
		
		// Thanks for Robert Johansson <robert.johansson@kreatel.se>
		HTTPResponse httpRes = new HTTPResponse();
		httpRes.setContentType(contentType);
		httpRes.setStatusCode(HTTPStatus.OK);
		httpRes.setContentLength(contentLen);
		httpRes.setContentInputStream(contentIn);

		httpReq.post(httpRes);

		try {
			contentIn.close();
		}
		catch (Exception e) {}
		
		conMan.removeConnectionInfo(conID);
	}
	
	////////////////////////////////////////////////
	// Content URL
	////////////////////////////////////////////////

	public String getInterfaceAddress()
	{
		return getInterfaceAddress();
	}			

	public int getHTTPPort()
	{
		return getHTTPPort();
	}			
	
	public String getContentExportURL(String id)
	{
		return "http://" + getInterfaceAddress() + ":" + getHTTPPort() + CONTENT_EXPORT_URI + "?" + CONTENT_ID + "=" + id;
	}			

	public String getContentImportURL(String id)
	{
		return "http://" + getInterfaceAddress() + ":" + getHTTPPort() + CONTENT_IMPORT_URI + "?" + CONTENT_ID + "=" + id;
	}

	////////////////////////////////////////////////
	// run
	////////////////////////////////////////////////

	private final static int DEFAULT_SYSTEMUPDATEID_INTERVAL = 2000;
	private final static int DEFAULT_CONTENTUPDATE_INTERVAL = 60000;
	
	private long systemUpdateIDInterval;
	private long contentUpdateInterval;
	
	public void setSystemUpdateInterval(long itime)
	{
		systemUpdateIDInterval = itime;
	}
	
	public long getSystemUpdateIDInterval()
	{
		return systemUpdateIDInterval;
	}
	
	public void setContentUpdateInterval(long itime)
	{
		contentUpdateInterval = itime;
	}
	
	public long getContentUpdateInterval()
	{
		return contentUpdateInterval;
	}
	
	public void run()
	{
		MediaServer mserver = getMediaServer();
		StateVariable varSystemUpdateID = mserver.getStateVariable(SYSTEMUPDATEID);
		
		int lastSystemUpdateID = 0;
		long lastContentUpdateTime = System.currentTimeMillis();
		
		while (isRunnable() == true) {
			try {
				Thread.sleep(getSystemUpdateIDInterval());
			} catch (InterruptedException e) {}

			// Update SystemUpdateID
			int currSystemUpdateID = getSystemUpdateID();
			if (lastSystemUpdateID != currSystemUpdateID) {
				varSystemUpdateID.setValue(currSystemUpdateID);
				lastSystemUpdateID = currSystemUpdateID;
			}

			// Update Content Directory			
			long currTime = System.currentTimeMillis();
			if (getContentUpdateInterval() < (currTime - lastContentUpdateTime)) {
				getDirectoryList().update();
				lastContentUpdateTime = currTime;
			}
		}
	}
}

