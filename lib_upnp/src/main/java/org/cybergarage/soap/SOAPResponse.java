/******************************************************************
*
*	CyberSOAP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: SOAPResponse.java
*
*	Revision;
*
*	12/17/02
*		- first revision.
*	02/13/04
*		- Ralf G. R. Bergs <Ralf@Ber.gs>, Inma Marin Lopez <inma@dif.um.es>.
*		- Added XML header, <?xml version="1.0"?> to setContent().
*	05/11/04
*		- Changed the XML header to <?xml version="1.0" encoding="utf-8"?> in setContent().
*	
******************************************************************/

package org.cybergarage.soap;

import org.cybergarage.http.HTTPResponse;
import org.cybergarage.util.Debug;
import org.cybergarage.xml.Node;
import org.cybergarage.xml.XML;

public class SOAPResponse extends HTTPResponse
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public SOAPResponse()
	{
		setRootNode(SOAP.createEnvelopeBodyNode());
		setContentType(XML.DEFAULT_CONTENT_TYPE);
	}

	public SOAPResponse(HTTPResponse httpRes)
	{
		super(httpRes);
		setRootNode(SOAP.createEnvelopeBodyNode());
		setContentType(XML.DEFAULT_CONTENT_TYPE);
	}

	public SOAPResponse(SOAPResponse soapRes)
	{
		super(soapRes);
		setEnvelopeNode(soapRes.getEnvelopeNode());
		setContentType(XML.DEFAULT_CONTENT_TYPE);
	}

	////////////////////////////////////////////////
	//	Node
	////////////////////////////////////////////////

	private Node rootNode;
	
	private void setRootNode(Node node)
	{
		rootNode = node;
	}
	
	private Node getRootNode()
	{
		return rootNode;
	}
	
	////////////////////////////////////////////////
	//	SOAP Basic
	////////////////////////////////////////////////

	public void setEnvelopeNode(Node node)
	{
		setRootNode(node);
	}

	public Node getEnvelopeNode()
	{
		return getRootNode();
	}
	
	public Node getBodyNode()
	{
		Node envNode = getEnvelopeNode();
		if (envNode == null)
			return null;
		return envNode.getNodeEndsWith(SOAP.BODY);
	}

	public Node getMethodResponseNode(String name)
	{
		Node bodyNode = getBodyNode();
		if (bodyNode == null)
			return null;
		String methodResName = name + SOAP.RESPONSE;
		return bodyNode.getNodeEndsWith(methodResName);
	}

	public Node getFaultNode()
	{
		Node bodyNode = getBodyNode();
		if (bodyNode == null)
			return null;
		return bodyNode.getNodeEndsWith(SOAP.FAULT);
	}

	public Node getFaultCodeNode()
	{
		Node faultNode = getFaultNode();
		if (faultNode == null)
			return null;
		return faultNode.getNodeEndsWith(SOAP.FAULT_CODE);
	}

	public Node getFaultStringNode()
	{
		Node faultNode = getFaultNode();
		if (faultNode == null)
			return null;
		return faultNode.getNodeEndsWith(SOAP.FAULT_STRING);
	}

	public Node getFaultActorNode()
	{
		Node faultNode = getFaultNode();
		if (faultNode == null)
			return null;
		return faultNode.getNodeEndsWith(SOAP.FAULTACTOR);
	}

	public Node getFaultDetailNode()
	{
		Node faultNode = getFaultNode();
		if (faultNode == null)
			return null;
		return faultNode.getNodeEndsWith(SOAP.DETAIL);
	}

	public String getFaultCode()
	{
		Node node = getFaultCodeNode();
		if (node == null)
			return "";
		return node.getValue();
	}
	
	public String getFaultString()
	{
		Node node = getFaultStringNode();
		if (node == null)
			return "";
		return node.getValue();
	}
	
	public String getFaultActor()
	{
		Node node = getFaultActorNode();
		if (node == null)
			return "";
		return node.getValue();
	}

	////////////////////////////////////////////////
	//	XML Contents
	////////////////////////////////////////////////
	
	public void setContent(Node node)
	{
		// Thanks for Ralf G. R. Bergs <Ralf@Ber.gs>, Inma Marin Lopez <inma@dif.um.es>.
		String conStr = "";
		conStr += SOAP.VERSION_HEADER;
		conStr += "\n";
		conStr += node.toString(); 
		setContent(conStr);
	}

	////////////////////////////////////////////////
	//	print
	////////////////////////////////////////////////
	
	public void print()
	{
		Debug.message(toString());
		if (hasContent() == true)
			return;
		Node rootElem = getRootNode();
		if (rootElem == null)
			return;
		Debug.message(rootElem.toString());
	}
}
