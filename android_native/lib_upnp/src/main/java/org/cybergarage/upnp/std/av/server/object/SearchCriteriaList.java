/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File: SearchCriteriaList.java
*
*	Revision;
*
*	08/07/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object;

import java.util.*;

public class SearchCriteriaList extends Vector 
{
	public SearchCriteriaList() 
	{
	}
	
	public SearchCriteria getSearchCriteria(int n)
	{
		return (SearchCriteria)get(n);
	}

	public SearchCriteria getSearchCriteria(String name) 
	{
		if (name == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			SearchCriteria node = getSearchCriteria(n);
			if (name.compareTo(node.getProperty()) == 0)
				return node;
		}
		return null;
	}

	////////////////////////////////////////////////
	// compare
	////////////////////////////////////////////////

	public boolean compare(ContentNode cnode, SearchCapList searchCapList)
	{
		int n;
		int searchCriCnt = size();
	
		// Set compare result
		for (n=0; n<searchCriCnt; n++) {
			SearchCriteria searchCri = getSearchCriteria(n);
			String property = searchCri.getProperty();
			if (property == null) {
				searchCri.setResult(true);
				continue;
			}
			SearchCap searchCap = searchCapList.getSearchCap(property);
			if (searchCap == null) {
				searchCri.setResult(true);
				continue;
			}
			boolean cmpResult = searchCap.compare(searchCri, cnode);
			searchCri.setResult(cmpResult);
		}

		// Eval only logical ADD operation at first;
		SearchCriteriaList orSearchCriList = new SearchCriteriaList();
		for (n=0; n<searchCriCnt; n++) {
			SearchCriteria currSearchCri = getSearchCriteria(n);
			if (n<(searchCriCnt-1)) {
				if (currSearchCri.isLogicalAND() == true) {
					SearchCriteria nextSearchCri = getSearchCriteria(n+1);
					boolean currResult = currSearchCri.getResult();
					boolean nextResult = nextSearchCri.getResult();
					boolean logicalAND = (currResult & nextResult) ? true : false;
					nextSearchCri.setResult(logicalAND);
					continue;
				}
			}
			SearchCriteria orSearchCri = new SearchCriteria(currSearchCri);
			orSearchCriList.add(orSearchCri);
		}

		// Eval logical OR operation;
		int orSearchCriCnt = orSearchCriList.size();
		for (n=0; n<orSearchCriCnt; n++) {
			SearchCriteria searchCri = getSearchCriteria(n);
			if (searchCri.getResult() == true)
				return true;
		}

		return false;
	}
}
