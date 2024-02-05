/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File:StateVariableData.java
*
*	Revision;
*
*	02/05/03
*		- first revision.
*	01/06/04
*		- Added setQueryListener() and getQueryListener().
*
******************************************************************/

package org.cybergarage.upnp.xml;

import org.cybergarage.upnp.control.*;

public class StateVariableData extends NodeData
{
	public StateVariableData() 
	{
	}

	////////////////////////////////////////////////
	// value
	////////////////////////////////////////////////

	private String value = "";

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	////////////////////////////////////////////////
	// QueryListener
	////////////////////////////////////////////////

	private QueryListener queryListener = null;

	public QueryListener getQueryListener() {
		return queryListener;
	}

	public void setQueryListener(QueryListener queryListener) {
		this.queryListener = queryListener;
	}
	
	////////////////////////////////////////////////
	// QueryResponse
	////////////////////////////////////////////////

	private QueryResponse queryRes = null;

	public QueryResponse getQueryResponse() 
	{
		return queryRes;
	}

	public void setQueryResponse(QueryResponse res) 
	{
		queryRes = res;
	}

}

