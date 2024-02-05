/******************************************************************
*
*	CyberLink for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: AllowedValue.java
*
*	Revision:
*
*	03/27/04
*		- first revision.
*	
******************************************************************/

package org.cybergarage.upnp;

import org.cybergarage.xml.Node;

public class AllowedValue
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public final static String ELEM_NAME = "allowedValue";

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private Node allowedValueNode;

	public Node getAllowedValueNode()
	{
		return allowedValueNode;
	}
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public AllowedValue(Node node)
	{
		allowedValueNode = node;
	}

	/**
	 * Create an AllowedValue by the value String,
	 * and will create the Node structure by itself
	 * 
	 * @param value The value that will be associate to thi object
	 * 
	 * @author Stefano "Kismet" Lenzi - kismet-sl@users.sourceforge.net  - 2005
	 */
	public AllowedValue(String value) {

		//TODO Some test are done not stable
		allowedValueNode = new Node(ELEM_NAME); //better (twa)
		setValue(value);						//better (twa)
	}

	////////////////////////////////////////////////
	//	isAllowedValueNode
	////////////////////////////////////////////////

	public static boolean isAllowedValueNode(Node node)
	{
		return ELEM_NAME.equals(node.getName());
	}

	////////////////////////////////////////////////
	//	Value
	////////////////////////////////////////////////

	public void setValue(String value)
	{
		getAllowedValueNode().setValue(value);
	}

	public String getValue()
	{
		return getAllowedValueNode().getValue();
	}
}
