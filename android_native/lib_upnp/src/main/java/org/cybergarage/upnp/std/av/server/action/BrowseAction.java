/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : BrowseAction
*
*	Revision:
*
*	10/22/03
*		- first revision.
*	04/27/04
*		- Changed getObjectID() to return the string value.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.action;

import org.cybergarage.upnp.*;

public class BrowseAction extends Action
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	
	public final static String OBJECT_ID = "ObjectID";
	public final static String BROWSE_FLAG = "BrowseFlag";
	public final static String FILTER = "Filter";
	public final static String STARTING_INDEX = "StartingIndex";
	public final static String REQUESTED_COUNT = "RequestedCount";
	public final static String SORT_CRITERIA = "SortCriteria";
	
	public final static String BROWSE_METADATA = "BrowseMetadata";
	public final static String BROWSE_DIRECT_CHILDREN = "BrowseDirectChildren";

	public final static String RESULT = "Result";
	public final static String NUMBER_RETURNED = "NumberReturned";
	public final static String TOTAL_MACHES = "TotalMatches";
	public final static String UPDATE_ID = "UpdateID";

	////////////////////////////////////////////////
	// Constrictor
	////////////////////////////////////////////////
	
	public BrowseAction(Action action)
	{
		super(action);
	}

	////////////////////////////////////////////////
	// Request
	////////////////////////////////////////////////

	public String getBrowseFlag()
	{
		return getArgumentValue(BROWSE_FLAG);
	}
	
	public boolean isMetadata()
	{
		return BROWSE_METADATA.equals(getBrowseFlag());
	}

	public boolean isDirectChildren()
	{
		return BROWSE_DIRECT_CHILDREN.equals(getBrowseFlag());
	}
	
	public String getObjectID()
	{
		return getArgumentValue(OBJECT_ID);
	}

	public int getStartingIndex()
	{
		return getArgumentIntegerValue(STARTING_INDEX);
	}

	public int getRequestedCount()
	{
		return getArgumentIntegerValue(REQUESTED_COUNT);
	}
	
	public int getNumberReturned()
	{
		return getArgumentIntegerValue(NUMBER_RETURNED);
	}

	public int getTotalMatches()
	{
		return getArgumentIntegerValue(TOTAL_MACHES);
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

