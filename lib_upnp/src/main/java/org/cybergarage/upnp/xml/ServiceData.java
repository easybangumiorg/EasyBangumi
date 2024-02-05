/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: ServiceData.java
*
*	Revision;
*
*	03/28/03
*		- first revision.
*	01/06/04
*		- Moved setQueryListener() and getQueryListener() to StateVariableData class.
*	03/30/05
*		- Removed setDescriptionURL() and getDescriptionURL().
*
******************************************************************/

package org.cybergarage.upnp.xml;

import org.cybergarage.util.*;
import org.cybergarage.xml.*;

import org.cybergarage.upnp.event.*;

public class ServiceData extends NodeData
{
	public ServiceData() 
	{
	}

	////////////////////////////////////////////////
	// controlActionListenerList
	////////////////////////////////////////////////

	private ListenerList controlActionListenerList = new ListenerList();

	public ListenerList getControlActionListenerList() {
		return controlActionListenerList;
	}

	////////////////////////////////////////////////
	// scpdNode
	////////////////////////////////////////////////

	private Node scpdNode = null;

	public Node getSCPDNode() {
		return scpdNode;
	}

	public void setSCPDNode(Node node) {
		scpdNode = node;
	}

	////////////////////////////////////////////////
	// SubscriberList
	////////////////////////////////////////////////

	private SubscriberList subscriberList = new SubscriberList();
	
	public SubscriberList getSubscriberList() {
		return subscriberList;
	}

	////////////////////////////////////////////////
	// SID
	////////////////////////////////////////////////

    private String descriptionURL = "";

    public String getDescriptionURL() {
            return descriptionURL;
    }

    public void setDescriptionURL(String descriptionURL) {
            this.descriptionURL = descriptionURL;
    }
	
	////////////////////////////////////////////////
	// SID
	////////////////////////////////////////////////

	private String sid = "";
	
	public String getSID() {
		return sid;
	}

	public void setSID(String id) {
		sid = id;
	}

	////////////////////////////////////////////////
	// Timeout
	////////////////////////////////////////////////

	private long timeout = 0;

	public long getTimeout() 
	{
		return timeout;
	}

	public void setTimeout(long value) 
	{
		timeout = value;
	}

}

