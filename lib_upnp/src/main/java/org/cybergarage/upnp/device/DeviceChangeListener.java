/******************************************************************
*
*	CyberLink for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: DeviceChangeListener.java
*
*	Revision;
*
*	09/12/04
*		- Oliver Newell <newell@media-rush.com>
*		- Added this class to allow ControlPoint applications to 
*         be notified when the ControlPoint base class adds/removes
*         a UPnP device
*	
******************************************************************/

package org.cybergarage.upnp.device;

import org.cybergarage.upnp.Device;

public interface DeviceChangeListener
{
	public void deviceAdded( Device dev );
	public void deviceRemoved( Device dev );
}
