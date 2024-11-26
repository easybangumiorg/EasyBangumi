/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: Argument.java
*
*	Revision;
*
*	12/05/02
*		- first revision.
*	03/28/04
*		- Added getRelatedStateVariable().
*		- Changed setRelatedStateVariable() to setRelatedStateVariableName().
*		- Changed getRelatedStateVariable() to getRelatedStateVariableName().
*		- Added getActionNode() and getAction().
*		- Added getServiceNode() and getService().
*		- Added the parent service node to the constructor.
*	04/12/06
*		- Added setUserData() and getUserData() to set a user original data object.
*
******************************************************************/

package org.cybergarage.upnp;

import org.cybergarage.upnp.xml.ArgumentData;
import org.cybergarage.xml.Node;

public class Argument
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public final static String ELEM_NAME = "argument";

	public final static String IN = "in";
	public final static String OUT = "out";

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private Node argumentNode;
	private Node serviceNode;

	public Node getArgumentNode()
	{
		return argumentNode;
	}

	private Node getServiceNode()
	{
		return serviceNode;
	}

	public Service getService()
	{
		return new Service(getServiceNode());
	}
	
	void setService(Service s){
		s.getServiceNode();
	}
	
	public Node getActionNode()
	{
		Node argumentLinstNode = getArgumentNode().getParentNode();
		if (argumentLinstNode == null)
			return null;
		Node actionNode = argumentLinstNode.getParentNode();
		if (actionNode == null)
			return null;
		if (Action.isActionNode(actionNode) == false)
			return null;
		return actionNode;
	}
	
	public Action getAction()
	{
		return new Action(getServiceNode(), getActionNode());
	}
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public Argument()
	{
		argumentNode = new Node(ELEM_NAME);
		serviceNode = null;
	}
	
	public Argument(Node servNode){
		argumentNode = new Node(ELEM_NAME);
		serviceNode = servNode;		
	}
	
	public Argument(Node servNode, Node argNode)
	{
		serviceNode = servNode;
		argumentNode = argNode;
	}

	public Argument(String name, String value)
	{
		this();
		setName(name);
		setValue(value);
	}

	////////////////////////////////////////////////
	//	isArgumentNode
	////////////////////////////////////////////////

	public static boolean isArgumentNode(Node node)
	{
		return Argument.ELEM_NAME.equals(node.getName());
	}

	////////////////////////////////////////////////
	//	name
	////////////////////////////////////////////////

	private final static String NAME = "name";
	
	public void setName(String value)
	{
		getArgumentNode().setNode(NAME, value);
	}

	public String getName()
	{
		return getArgumentNode().getNodeValue(NAME);
	}

	////////////////////////////////////////////////
	//	direction
	////////////////////////////////////////////////

	private final static String DIRECTION = "direction";
	
	public void setDirection(String value)
	{
		getArgumentNode().setNode(DIRECTION, value);
	}

	public String getDirection()
	{
		return getArgumentNode().getNodeValue(DIRECTION);
	}

	public boolean isInDirection()
	{
		String dir = getDirection();
		if (dir == null)
			return false;
		return dir.equalsIgnoreCase(IN);
	}

	public boolean isOutDirection()
	{
		return !isInDirection();
	}
	
	////////////////////////////////////////////////
	//	relatedStateVariable
	////////////////////////////////////////////////

	private final static String RELATED_STATE_VARIABLE = "relatedStateVariable";
	
	public void setRelatedStateVariableName(String value)
	{
		getArgumentNode().setNode(RELATED_STATE_VARIABLE, value);
	}

	public String getRelatedStateVariableName()
	{
		return getArgumentNode().getNodeValue(RELATED_STATE_VARIABLE);
	}

	public StateVariable getRelatedStateVariable()
	{
		Service service = getService();
		if (service == null)
			return null;
		String relatedStatVarName = getRelatedStateVariableName();
		return service.getStateVariable(relatedStatVarName);
	}
	
	////////////////////////////////////////////////
	//	UserData
	////////////////////////////////////////////////

	private ArgumentData getArgumentData()
	{
		Node node = getArgumentNode();
		ArgumentData userData = (ArgumentData)node.getUserData();
		if (userData == null) {
			userData = new ArgumentData();
			node.setUserData(userData);
			userData.setNode(node);
		}
		return userData;
	}

	////////////////////////////////////////////////
	//	value
	////////////////////////////////////////////////

	public void setValue(String value) 
	{
		getArgumentData().setValue(value);
	}
	
	public void setValue(int value) 
	{
		setValue(Integer.toString(value));
	}
	
	public String getValue() 
	{
		return getArgumentData().getValue();
	}

	public int getIntegerValue() 
	{
		String value = getValue();
		try {
			return Integer.parseInt(value);
		}
		catch (Exception e) {
		}
		return 0;
	}

	////////////////////////////////////////////////
	//	userData
	////////////////////////////////////////////////

	private Object userData = null; 
	
	public void setUserData(Object data) 
	{
		userData = data;
	}

	public Object getUserData() 
	{
		return userData;
	}
}
