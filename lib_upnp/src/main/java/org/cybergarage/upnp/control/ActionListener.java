/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: ActionListener.java
*
*	Revision;
*
*	01/16/03
*		- first revision.
*	
******************************************************************/

package org.cybergarage.upnp.control;

import org.cybergarage.upnp.*;

public interface ActionListener
{
	public boolean actionControlReceived(Action action);
}
