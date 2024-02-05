/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: Advertiser.java
*
*	Revision;
*
*	12/24/03
*		- first revision.
*	06/18/04
*		- Changed to advertise every 25%-50% of the periodic notification cycle for NMPR;
*	
******************************************************************/

package org.cybergarage.upnp.device;

import org.cybergarage.util.*;
import org.cybergarage.upnp.*;

public class Advertiser extends ThreadCore
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public Advertiser(Device dev)
	{
		setDevice(dev);
	}
	
	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private Device device;

	public void setDevice(Device dev)
	{
		device = dev;
	}
	
	public Device getDevice()
	{
		return device;
	}

	////////////////////////////////////////////////
	//	Thread
	////////////////////////////////////////////////
	
	public void run() 
	{
		Device dev = getDevice();
		long leaseTime = dev.getLeaseTime();
		long notifyInterval;
		while (isRunnable() == true) {
			notifyInterval = (leaseTime/4) + (long)((float)leaseTime * (Math.random() * 0.25f));
			notifyInterval *= 1000;
			try {
				Thread.sleep(notifyInterval);
			} catch (InterruptedException e) {}
			dev.announce();
		}
	}
}
