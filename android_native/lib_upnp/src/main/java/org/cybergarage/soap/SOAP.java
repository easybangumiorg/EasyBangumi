/******************************************************************
*
*	CyberSOAP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: SOAP.java
*
*	Revision;
*
*	12/11/02
*		- first revision.
*	
******************************************************************/

package org.cybergarage.soap;

import org.cybergarage.xml.Node;
import org.cybergarage.xml.Parser;

public class SOAP
{
	public static final String ENVELOPE = "Envelope";
	public static final String BODY = "Body";
	public static final String RESPONSE = "Response";
	public static final String FAULT = "Fault";
	public static final String FAULT_CODE = "faultcode";
	public static final String FAULT_STRING = "faultstring";
	public static final String FAULTACTOR = "faultactor";
	public static final String DETAIL = "detail";
		
	public static final String RESULTSTATUS = "ResultStatus";
	public static final String UPNP_ERROR = "UPnPError";
	public static final String ERROR_CODE = "errorCode";
	public static final String ERROR_DESCRIPTION = "errorDescription";

	//public static final String XMLNS = "SOAP-ENV";
	public static final String XMLNS = "s";
	public static final String METHODNS = "u";
	public static final String DELIM = ":";
	
	public static final String XMLNS_URL = "http://schemas.xmlsoap.org/soap/envelope/";
	public static final String ENCSTYLE_URL = "http://schemas.xmlsoap.org/soap/encoding/";
	
	public static final String CONTENT_TYPE = "text/xml; charset=\"utf-8\"";
	public static final String VERSION_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	////////////////////////////////////////////////
	//	createEnvelopeBodyNode
	////////////////////////////////////////////////

	public final static Node createEnvelopeBodyNode()
	{
		// <Envelope>
		Node envNode = new Node(SOAP.XMLNS + SOAP.DELIM + SOAP.ENVELOPE);
		envNode.setAttribute("xmlns" + SOAP.DELIM + SOAP.XMLNS, SOAP.XMLNS_URL);
		envNode.setAttribute(SOAP.XMLNS + SOAP.DELIM + "encodingStyle", SOAP.ENCSTYLE_URL);

 		// <Body>
		Node bodyNode = new Node(SOAP.XMLNS + SOAP.DELIM + SOAP.BODY);
		envNode.addNode(bodyNode);
		
		return envNode;
	}

	////////////////////////////////////////////////
	// XML Parser
	////////////////////////////////////////////////

	private static Parser xmlParser;
	
	public final static void setXMLParser(Parser parser)
	{
		xmlParser = parser;
	}
	
	public final static Parser getXMLParser()
	{
		return xmlParser;
	}
}

