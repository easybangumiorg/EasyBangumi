/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File : SearchAction
*
*	Revision:
*
*	08/16/04
*		- Changed getObjectID() to return the string value.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.action;

import org.cybergarage.upnp.*;

public class SearchAction extends Action
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	
	public final static String CONTAINER_ID = "ContainerID";
	public final static String SEARCH_CRITERIA = "SearchCriteria";
	public final static String FILTER = "Filter";
	public final static String STARTING_INDEX = "StartingIndex";
	public final static String REQUESTED_COUNT = "RequestedCount";
	public final static String SORT_CRITERIA = "SortCriteria";
	
	public final static String RESULT = "Result";
	public final static String NUMBER_RETURNED = "NumberReturned";
	public final static String TOTAL_MACHES = "TotalMatches";
	public final static String UPDATE_ID = "UpdateID";

	////////////////////////////////////////////////
	// Constrictor
	////////////////////////////////////////////////
	
	public SearchAction(Action action)
	{
		super(action);
	}

	////////////////////////////////////////////////
	// Request
	////////////////////////////////////////////////

	public String getContainerID()
	{
		return getArgumentValue(CONTAINER_ID);
	}

	public String getSearchCriteria()
	{
		return getArgumentValue(SEARCH_CRITERIA);
	}
	
	public int getStartingIndex()
	{
		return getArgumentIntegerValue(STARTING_INDEX);
	}

	public int getRequestedCount()
	{
		return getArgumentIntegerValue(REQUESTED_COUNT);
	}
	
	public String getSortCriteria()
	{
		return getArgumentValue(SORT_CRITERIA);
	}

	public String getFilter()
	{
		return getArgumentValue(FILTER);
	}
	
	////////////////////////////////////////////////
	// Result
	////////////////////////////////////////////////

	public void setResult(String value)
	{
		setArgumentValue(RESULT, value);
	}

	public void setNumberReturned(int value)
	{
		setArgumentValue(NUMBER_RETURNED, value);
	}

	public void setTotalMaches(int value)
	{
		setArgumentValue(TOTAL_MACHES, value);
	}

	public void setUpdateID(int value)
	{
		setArgumentValue(UPDATE_ID, value);
	}
}
