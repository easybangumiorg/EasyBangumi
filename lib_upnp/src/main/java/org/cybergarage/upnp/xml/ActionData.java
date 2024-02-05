/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: ActionData.java
*
*	Revision;
*
*	03/28/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.xml;

import org.cybergarage.upnp.control.*;

public class ActionData extends NodeData
{
	public ActionData()
	{
	}

	////////////////////////////////////////////////
	// ActionListener
	////////////////////////////////////////////////

	private ActionListener actionListener = null;

	public ActionListener getActionListener() {
		return actionListener;
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	////////////////////////////////////////////////
	// ControlResponse
	////////////////////////////////////////////////

	private ControlResponse ctrlRes = null;

	public ControlResponse getControlResponse() 
	{
		return ctrlRes;
	}

	public void setControlResponse(ControlResponse res) 
	{
		ctrlRes = res;
	}

}

