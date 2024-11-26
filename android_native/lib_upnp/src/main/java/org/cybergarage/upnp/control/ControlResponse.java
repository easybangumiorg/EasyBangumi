/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: ControlResponse.java
*
*	Revision;
*
*	01/29/03
*		- first revision.
*	
******************************************************************/

package org.cybergarage.upnp.control;

import org.cybergarage.http.*;
import org.cybergarage.xml.*;
import org.cybergarage.soap.*;

import org.cybergarage.upnp.*;

public class ControlResponse extends SOAPResponse
{
	public static final String FAULT_CODE = "Client";
	public static final String FAULT_STRING = "UPnPError";
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public ControlResponse()
	{
		setServer(UPnP.getServerName());
	}

	public ControlResponse(SOAPResponse soapRes)
	{
		super(soapRes);
	}
	
	////////////////////////////////////////////////
	//	FaultResponse
	////////////////////////////////////////////////

	public void setFaultResponse(int errCode, String errDescr)
	{
		setStatusCode(HTTPStatus.INTERNAL_SERVER_ERROR);
		
		Node bodyNode = getBodyNode();
		Node faultNode = createFaultResponseNode(errCode, errDescr);
		bodyNode.addNode(faultNode);

		Node envNode = getEnvelopeNode();
		setContent(envNode);
	}

	public void setFaultResponse(int errCode)
	{
		setFaultResponse(errCode, UPnPStatus.code2String(errCode));
	}

	////////////////////////////////////////////////
	//	createFaultResponseNode
	////////////////////////////////////////////////

	private Node createFaultResponseNode(int errCode, String errDescr)
	{
		// <s:Fault>
		Node faultNode = new Node(SOAP.XMLNS + SOAP.DELIM + SOAP.FAULT);

 		// <faultcode>s:Client</faultcode>
		Node faultCodeNode = new Node(SOAP.FAULT_CODE);
		faultCodeNode.setValue(SOAP.XMLNS + SOAP.DELIM + FAULT_CODE);
		faultNode.addNode(faultCodeNode);
		
		// <faultstring>UPnPError</faultstring>
		Node faultStringNode = new Node(SOAP.FAULT_STRING);
		faultStringNode.setValue(FAULT_STRING);
		faultNode.addNode(faultStringNode);

		// <detail>
		Node detailNode = new Node(SOAP.DETAIL);
		faultNode.addNode(detailNode);

		// <UPnPError xmlns="urn:schemas-upnp-org:control-1-0">
		Node upnpErrorNode = new Node(FAULT_STRING);
		upnpErrorNode.setAttribute("xmlns", Control.XMLNS);
		detailNode.addNode(upnpErrorNode);

		// <errorCode>error code</errorCode>
		Node errorCodeNode = new Node(SOAP.ERROR_CODE);
		errorCodeNode.setValue(errCode);
		upnpErrorNode.addNode(errorCodeNode);

		// <errorDescription>error string</errorDescription>
		Node errorDesctiprionNode = new Node(SOAP.ERROR_DESCRIPTION);
		errorDesctiprionNode.setValue(errDescr);
		upnpErrorNode.addNode(errorDesctiprionNode);
		
		return faultNode;
	}
	
	private Node createFaultResponseNode(int errCode)
	{
		return createFaultResponseNode(errCode, UPnPStatus.code2String(errCode));
	}

	////////////////////////////////////////////////
	//	UPnP Error
	////////////////////////////////////////////////
	
	private UPnPStatus upnpErr = new UPnPStatus();
	
	private Node getUPnPErrorNode()
	{
		Node detailNode = getFaultDetailNode();
		if (detailNode == null)
			return null;
		return detailNode.getNodeEndsWith(SOAP.UPNP_ERROR);
	}

	private Node getUPnPErrorCodeNode()
	{
		Node errorNode = getUPnPErrorNode();
		if (errorNode == null)
			return null;
		return errorNode.getNodeEndsWith(SOAP.ERROR_CODE);
	}

	private Node getUPnPErrorDescriptionNode()
	{
		Node errorNode = getUPnPErrorNode();
		if (errorNode == null)
			return null;
		return errorNode.getNodeEndsWith(SOAP.ERROR_DESCRIPTION);
	}

	public int getUPnPErrorCode()
	{
		Node errorCodeNode = getUPnPErrorCodeNode();
		if (errorCodeNode == null)
			return -1;
		String errorCodeStr = errorCodeNode.getValue();
		try {
			return Integer.parseInt(errorCodeStr);
		}
		catch (Exception e) {
			return -1;
		}
	}

	public String getUPnPErrorDescription()
	{
		Node errorDescNode = getUPnPErrorDescriptionNode();
		if (errorDescNode == null)
			return "";
		return errorDescNode.getValue();
	}

	public UPnPStatus getUPnPError()
	{
		int code = 0;
		String desc = "";
		code = getUPnPErrorCode();
		desc = getUPnPErrorDescription();
		upnpErr.setCode(code);
		upnpErr.setDescription(desc);
		return upnpErr;
	}

}
