/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: QueryRequest.java
*
*	Revision;
*
*	01/29/03
*		- first revision.
*	09/02/03
*		- Giordano Sassaroli <sassarol@cefriel.it>
*		- Error : redundant code, the setRequest method in QueryRequest invokes setURI even if after a couple of rows setRequestHost is invoked
*	
******************************************************************/

package org.cybergarage.upnp.control;

import org.cybergarage.http.*;
import org.cybergarage.xml.*;
import org.cybergarage.soap.*;

import org.cybergarage.upnp.*;

public class QueryRequest extends ControlRequest
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public QueryRequest()
	{
	}

	public QueryRequest(HTTPRequest httpReq)
	{
		set(httpReq);
	}

	////////////////////////////////////////////////
	//	Qyery
	////////////////////////////////////////////////

	private Node getVarNameNode()
	{
		Node bodyNode = getBodyNode();
		if (bodyNode == null)
			return null;
		if (bodyNode.hasNodes() == false)
			return null;
		Node queryStateVarNode = bodyNode.getNode(0);
		if (queryStateVarNode == null)
			return null;
		if (queryStateVarNode.hasNodes() == false)
			return null;
		return queryStateVarNode.getNode(0);
	}
	
	public String getVarName()
	{
		Node node = getVarNameNode();
		if (node == null)
			return "";
		return node.getValue();
	}

	////////////////////////////////////////////////
	//	setRequest
	////////////////////////////////////////////////
	
	public void setRequest(StateVariable stateVar)
	{
		Service service = stateVar.getService();		
		
		String ctrlURL = service.getControlURL();

		setRequestHost(service);

		setEnvelopeNode(SOAP.createEnvelopeBodyNode());
		Node envNode = getEnvelopeNode();
		Node bodyNode = getBodyNode();
		Node qeuryNode = createContentNode(stateVar);
		bodyNode.addNode(qeuryNode);
		setContent(envNode);

		setSOAPAction(Control.QUERY_SOAPACTION);
	}

	////////////////////////////////////////////////
	//	Contents
	////////////////////////////////////////////////

	private Node createContentNode(StateVariable stateVar)
	{
		Node queryVarNode = new Node();
		queryVarNode.setName(Control.NS, Control.QUERY_STATE_VARIABLE);
		queryVarNode.setNameSpace(Control.NS, Control.XMLNS);

		Node varNode = new Node();
		varNode.setName(Control.NS, Control.VAR_NAME);
		varNode.setValue(stateVar.getName());
		queryVarNode.addNode(varNode);
		
		return queryVarNode;
	}
	
	////////////////////////////////////////////////
	//	post
	////////////////////////////////////////////////

	public QueryResponse post()
	{
		SOAPResponse soapRes = postMessage(getRequestHost(), getRequestPort());
		return new QueryResponse(soapRes);
	}
}

