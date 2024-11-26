/******************************************************************
*
*	CyberXML for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: NodeList.java
*
*	Revision;
*
*	11/27/02
*		- first revision.
*
******************************************************************/

package org.cybergarage.xml;

import java.util.Vector;

public class NodeList extends Vector 
{
	public NodeList() 
	{
	}
	
	public Node getNode(int n)
	{
		return (Node)get(n);
	}

	public synchronized Node getNode(String name)
	{
		if (name == null)
			return null;
		
		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			Node node = getNode(n);
			String nodeName = node.getName();
			if (name.compareTo(nodeName) == 0)
				return node;
		}
		return null;
	}

	public synchronized Node getEndsWith(String name)
	{
		if (name == null)
			return null;

		int nLists = size(); 
		for (int n=0; n<nLists; n++) {
			Node node = getNode(n);
			String nodeName = node.getName();
			if (nodeName == null)
				continue;
			if (nodeName.endsWith(name) == true)
				return node;
		}
		return null;
	}
}

