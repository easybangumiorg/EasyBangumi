/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: SSDPNotifySocket.java
*
*	Revision;
*
*	11/20/02
*		- first revision.
*	05/13/03
*		- Added support for IPv6.
*	02/20/04
*		- Inma Marin Lopez <inma@dif.um.es>
*		- Added a multicast filter using the SSDP pakcet.
*	04/20/05
*		- Mikael Hakman <mhakman@dkab.net>
*		- Handle receive() returning null.
*		- Added close() in stop().
*	08/23/07
*		- Thanks for Kazuyuki Shudo
* 		- Changed run() to catch IOException of HTTPMUSocket::receive().
*	01/31/08
*		- Changed start() not to abort when the interface infomation is null on Android m3-rc37a.
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.*;
import java.io.IOException;

import org.cybergarage.net.*;
import org.cybergarage.util.*;
import org.cybergarage.http.*;
import org.cybergarage.upnp.*;

/**
 * 
 * This class identifies a SSDP socket only for <b>notifing packet</b>.<br>
 * 
 * @author Satoshi "skonno" Konno
 * @author Stefano "Kismet" Lenzi
 * @version 1.8
 *
 */
public class SSDPNotifySocket extends HTTPMUSocket implements Runnable
{
	private boolean useIPv6Address;
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public SSDPNotifySocket(String bindAddr)
	{
		String addr = SSDP.ADDRESS;
		useIPv6Address = false;
		if (HostInterface.isIPv6Address(bindAddr) == true) {
			addr = SSDP.getIPv6Address();
			useIPv6Address = true;
		}
		open(addr, SSDP.PORT, bindAddr);
		setControlPoint(null);
	}

	////////////////////////////////////////////////
	//	ControlPoint	
	////////////////////////////////////////////////

	private ControlPoint controlPoint = null;
	
	public void setControlPoint(ControlPoint ctrlp)
	{
		this.controlPoint = ctrlp;
	}

	public ControlPoint getControlPoint()
	{
		return controlPoint;
	}

	/**
	 * This method send a {@link SSDPNotifyRequest} over {@link SSDPNotifySocket}
	 * 
	 * @param req the {@link SSDPNotifyRequest} to send
	 * @return true if and only if the trasmission succeced<br>
	 * 	Because it rely on UDP doesn't mean that it's also recieved
	 */
	public boolean post(SSDPNotifyRequest req)
	{
		String ssdpAddr = SSDP.ADDRESS;
		if (useIPv6Address == true)
			ssdpAddr = SSDP.getIPv6Address();
		req.setHost(ssdpAddr, SSDP.PORT);
		return post((HTTPRequest)req);
	}

	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////

	private Thread deviceNotifyThread = null;
		
	public void run()
	{
		Thread thisThread = Thread.currentThread();
		
		ControlPoint ctrlPoint = getControlPoint();
		
		while (deviceNotifyThread == thisThread) {
			Thread.yield();

			// Thanks for Kazuyuki Shudo (08/23/07)
			SSDPPacket packet = null;
			try {
				packet = receive();
			}
			catch (IOException e) { 
				break;
			}
			
			// Thanks for Mikael Hakman (04/20/05)
			if (packet == null)
				continue;
			
			// Thanks for Inma (02/20/04)
			InetAddress maddr = getMulticastInetAddress();
			InetAddress pmaddr = packet.getHostInetAddress();
			if (maddr.equals(pmaddr) == false) {
				Debug.warning("Invalidate Multicast Received from IP " + maddr + " on " + pmaddr);
				continue;
			}
			//TODO Must be performed on a different Thread in order to prevent UDP packet losses.
			if (ctrlPoint != null)
				ctrlPoint.notifyReceived(packet); 
		}
	}
	
	public void start(){
		StringBuffer name = new StringBuffer("Cyber.SSDPNotifySocket/");
		String localAddr = this.getLocalAddress();
		// localAddr is null on Android m3-rc37a (01/30/08)
		if (localAddr != null && 0 < localAddr.length()) {
			name.append(this.getLocalAddress()).append(':');
			name.append(this.getLocalPort()).append(" -> ");
			name.append(this.getMulticastAddress()).append(':');
			name.append(this.getMulticastPort());
		}
		deviceNotifyThread = new Thread(this,name.toString());
		deviceNotifyThread.start();
	}
	
	public void stop()
	{
		// Thanks for Mikael Hakman (04/20/05)
		close();
		
		deviceNotifyThread = null;
	}
}

