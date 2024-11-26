/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : MediaRender.java
*
*	02/22/08
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.renderer;

import java.io.*;

import org.cybergarage.net.*;
import org.cybergarage.util.*;
import org.cybergarage.http.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.device.*;

public class MediaRenderer extends Device
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	
	public final static String DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaRenderer:1";
	
	public final static int DEFAULT_HTTP_PORT = 39520;
	
	public final static String DESCRIPTION = 
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n" +
		"   <specVersion>\n" +
		"      <major>1</major>\n" +
		"      <minor>0</minor>\n" +
		"   </specVersion>\n" +
		"   <device>\n" +
		"      <deviceType>urn:schemas-upnp-org:device:MediaRender:1</deviceType>\n" +
		"      <friendlyName>Cyber Garage Media Render</friendlyName>\n" +
		"      <manufacturer>Cyber Garage</manufacturer>\n" +
		"      <manufacturerURL>http://www.cybergarage.org</manufacturerURL>\n" +
		"      <modelDescription>Provides content through UPnP ContentDirectory service</modelDescription>\n" +
		"      <modelName>Cyber Garage Media Render</modelName>\n" +
		"      <modelNumber>1.0</modelNumber>\n" +
		"      <modelURL>http://www.cybergarage.org</modelURL>\n" +
		"      <UDN>uuid:362d9414-31a0-48b6-b684-2b4bd38391d0</UDN>\n" +
		"      <serviceList>\n" +
		"         <service>\n" +
		"            <serviceType>urn:schemas-upnp-org:service:RenderingControl:1</serviceType>\n" +
		"            <serviceId>RenderingControl</serviceId>\n" +
		"         </service>\n" +
		"         <service>\n" +
		"            <serviceType>urn:schemas-upnp-org:service:ConnectionManager:1</serviceType>\n" +
		"            <serviceId>ConnectionManager</serviceId>\n" +
		"         </service>\n" +
		"         <service>\n" +
		"            <serviceType>urn:schemas-upnp-org:service:AVTransport:1</serviceType>\n" +
		"            <serviceId>AVTransport</serviceId>\n" +
		"         </service>\n" +
		"      </serviceList>\n" +
		"   </device>\n" +
		"</root>";
	
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	
	private final static String DESCRIPTION_FILE_NAME = "description/description.xml";
	
	public MediaRenderer(String descriptionFileName) throws InvalidDescriptionException
	{
		super(new File(descriptionFileName));
		initialize();
	}

	public MediaRenderer()
	{
		super();
		try {
			initialize(DESCRIPTION, RenderingControl.SCPD, ConnectionManager.SCPD, AVTransport.SCPD);
		}
		catch (InvalidDescriptionException ide) {}
	}

	public MediaRenderer(String description, String renderCtrlSCPD, String conMgrSCPD, String avTransSCPD) throws InvalidDescriptionException
	{
		super();
		initialize(description, renderCtrlSCPD, conMgrSCPD, avTransSCPD);
	}
	
	private void initialize(String description, String renderCtrlSCPD, String conMgrSCPD, String avTransSCPD) throws InvalidDescriptionException
	{
		loadDescription(description);
		
		Service renCtrlService = getService(RenderingControl.SERVICE_TYPE);
		renCtrlService.loadSCPD(renderCtrlSCPD);
		
		Service conManService = getService(ConnectionManager.SERVICE_TYPE);
		conManService.loadSCPD(conMgrSCPD);
		
		Service avTransService = getService(AVTransport.SERVICE_TYPE);
		avTransService.loadSCPD(avTransSCPD);
		
		initialize();
	}
	
	private void initialize()
	{
		// Netwroking initialization		
		UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
		String firstIf = HostInterface.getHostAddress(0);
		setInterfaceAddress(firstIf);
		setHTTPPort(DEFAULT_HTTP_PORT);
		
		renCon = new RenderingControl(this);
		conMan = new ConnectionManager(this);
		avTrans = new AVTransport(this);

		setActionListener(null);
	}
	
	protected void finalize()
	{
		stop();		
	}
	
	////////////////////////////////////////////////
	// Memeber
	////////////////////////////////////////////////
	
	private ConnectionManager conMan;
	private RenderingControl renCon;
	private AVTransport avTrans;
	
	public ConnectionManager getConnectionManager()
	{
		return conMan;
	}

	public RenderingControl getRenderingControl()
	{
		return renCon;
	}	
	
	public AVTransport getAVTransport()
	{
		return avTrans;
	}	
	
	////////////////////////////////////////////////
	// HostAddress
	////////////////////////////////////////////////

	public void setInterfaceAddress(String ifaddr)
	{
		HostInterface.setInterface(ifaddr);
	}			
	
	public String getInterfaceAddress()
	{
		return HostInterface.getInterface();
	}			

	////////////////////////////////////////////////
	// HttpRequestListner (Overridded)
	////////////////////////////////////////////////
	
	public void httpRequestRecieved(HTTPRequest httpReq)
	{
		String uri = httpReq.getURI();
		Debug.message("uri = " + uri);

		/*
		if (uri.startsWith(ContentDirectory.CONTENT_EXPORT_URI) == true) {
			getContentDirectory().contentExportRequestRecieved(httpReq);
			return;
		}
		*/
		
		super.httpRequestRecieved(httpReq);
	}
	
	////////////////////////////////////////////////
	// Action Listener
	////////////////////////////////////////////////

	private ActionListener actionListener;
	
	public void setActionListener(ActionListener listener)
	{
		actionListener = listener; 
	}

	public ActionListener getActionListener()
	{
		return actionListener;
	}
	
	////////////////////////////////////////////////
	// start/stop (Overided)
	////////////////////////////////////////////////
	
	public boolean start()
	{
		super.start();
		return true;
	}
	
	public boolean stop()
	{
		super.stop();
		return true;
	}
	
	////////////////////////////////////////////////
	// update
	////////////////////////////////////////////////

	public void update()
	{
	}			

}

