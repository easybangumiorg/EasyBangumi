/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: ControlRequest.java
*
*	Revision;
*
*	01/29/03
*		- first revision.
*	05/09/05
*		- Changed getActionName() to return when the delimiter is not found.
*	
******************************************************************/

package org.cybergarage.upnp.control;

import org.cybergarage.http.*;
import org.cybergarage.xml.*;
import org.cybergarage.soap.*;

import org.cybergarage.upnp.*;

public class ActionRequest extends ControlRequest
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public ActionRequest()
	{
	}

	public ActionRequest(HTTPRequest httpReq)
	{
		set(httpReq);
	}

	////////////////////////////////////////////////
	//	Action
	////////////////////////////////////////////////
	
	public Node getActionNode()
	{
		Node bodyNode = getBodyNode();
		if (bodyNode == null)
			return null;
		if (bodyNode.hasNodes() == false)
			return null;
		return bodyNode.getNode(0);
	}

	public String getActionName()
	{
		Node node = getActionNode();
		if (node == null)
			return "";
		String name = node.getName();
		if (name == null)
			return "";
		int idx = name.indexOf(SOAP.DELIM)+1;
		if (idx < 0)
			return "";
		return name.substring(idx, name.length());
	}

	public ArgumentList getArgumentList()
	{
		Node actNode = getActionNode();
		int nArgNodes = actNode.getNNodes();
		ArgumentList argList = new ArgumentList();
		for (int n=0; n<nArgNodes; n++) {
			Argument arg = new Argument();
			Node argNode = actNode.getNode(n);
			arg.setName(argNode.getName());
			arg.setValue(argNode.getValue());
			argList.add(arg);
		}
		return argList;
	}

	////////////////////////////////////////////////
	//	setRequest
	////////////////////////////////////////////////
	
	public void setRequest(Action action, ArgumentList argList)
	{
		Service service = action.getService();		
		
		setRequestHost(service);

		setEnvelopeNode(SOAP.createEnvelopeBodyNode());
		Node envNode = getEnvelopeNode();
		Node bodyNode = getBodyNode();
		Node argNode = createContentNode(service, action, argList);
		bodyNode.addNode(argNode);
		setContent(envNode);

		String serviceType = service.getServiceType();
		String actionName = action.getName();
		String soapAction = "\"" +
			serviceType +
			"#" + actionName +
			"\"";
		setSOAPAction(soapAction);
	}

	////////////////////////////////////////////////
	//	Contents
	////////////////////////////////////////////////

	private Node createContentNode(Service service, Action action, ArgumentList argList)
	{
		String actionName = action.getName();
		String serviceType = service.getServiceType();
		
		Node actionNode = new Node();
		actionNode.setName(Control.NS, actionName);
		actionNode.setNameSpace(Control.NS, serviceType);

		int argListCnt = argList.size();
		for (int n=0; n<argListCnt; n++) {
			Argument arg = argList.getArgument(n);
			Node argNode = new Node();
			argNode.setName(arg.getName());
			argNode.setValue(arg.getValue());
			actionNode.addNode(argNode);
		}
		
		return actionNode;
	}

	////////////////////////////////////////////////
	//	post
	////////////////////////////////////////////////

	public ActionResponse post()
	{
		SOAPResponse soapRes = postMessage(getRequestHost(), getRequestPort());
		return new ActionResponse(soapRes);
	}
}

