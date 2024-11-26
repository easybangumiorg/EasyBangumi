/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: SubscriptionRequest.java
*
*	Revision;
*
*	01/31/03
*		- first revision.
*	05/21/03
*		- Giordano Sassaroli <sassarol@cefriel.it>
*		- Description: inserted a check at the beginning of the setService method
*		- Problem : If the EventSubURL does not start with a '/', the device could refuse event subscription
*		- Error : it is not an error, but adding the '/' when missing allows the integration with the Intel devices
*	09/02/03
*		- Giordano Sassaroli <sassarol@cefriel.it>
*		- Problem : NullpointerException thrown for devices whose description use absolute urls
*		- Error : the presence of a base url is not mandatory, the API code makes the assumption that control and event subscription urls are relative. If the baseUrl is not present, the request host and port should be extracted from the control/subscription url
*		- Description: The method setRequestHost/setService should be changed as follows
*	06/11/04
*		- Markus Thurner <markus.thurner@fh-hagenberg.at> (06/11/2004)
*		- Changed setServie() to get the host address from the SSDP Location field when the URLBase is null.
*	12/06/04
*		- Grzegorz Lehmann <grzegorz.lehmann@dai-labor.de>
*		- Stefano Lenzi <kismet-sl@users.sourceforge.net>
*		- Fixed getSID() to loop between getSID() and hasSID();
*
********************************************************************/

package org.cybergarage.upnp.event;

import org.cybergarage.http.*;

import org.cybergarage.upnp.*;
import org.cybergarage.upnp.device.*;

public class SubscriptionRequest extends HTTPRequest
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public SubscriptionRequest(){
		setContentLength(0);
	}

	public SubscriptionRequest(HTTPRequest httpReq){
		this();
		set(httpReq);
	}
	
	////////////////////////////////////////////////
	//	setRequest
	////////////////////////////////////////////////
	
	private void setService(Service service)
	{
		String eventSubURL = service.getEventSubURL();
		
		// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (05/21/03)
		setURI(eventSubURL, true);

		String urlBaseStr = "";
		Device dev = service.getDevice();
		if (dev != null)
			urlBaseStr = dev.getURLBase();
		
		if (urlBaseStr == null || urlBaseStr.length() <= 0) {
			Device rootDev = service.getRootDevice();
			if (rootDev != null)
				urlBaseStr = rootDev.getURLBase();
		}
		
		// Thansk for Markus Thurner <markus.thurner@fh-hagenberg.at> (06/11/2004)
		if (urlBaseStr == null || urlBaseStr.length() <= 0) {
			Device rootDev = service.getRootDevice();
			if (rootDev != null)
				urlBaseStr = rootDev.getLocation();
		}
		
		// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/02/03)
		if (urlBaseStr == null || urlBaseStr.length() <= 0) {
			if (HTTP.isAbsoluteURL(eventSubURL))
				urlBaseStr = eventSubURL;
		}
		
		String reqHost = HTTP.getHost(urlBaseStr);
		int reqPort = HTTP.getPort(urlBaseStr);
		
		setHost(reqHost, reqPort);
		setRequestHost(reqHost);
		setRequestPort(reqPort);
	}
	
	public void setSubscribeRequest(Service service, String callback, long timeout)
	{
		setMethod(Subscription.SUBSCRIBE_METHOD);
		setService(service);
		setCallback(callback);
		setNT(NT.EVENT);
		setTimeout(timeout);
	}

	public void setRenewRequest(Service service, String uuid, long timeout)
	{
		setMethod(Subscription.SUBSCRIBE_METHOD);
		setService(service);
		setSID(uuid);
		setTimeout(timeout);
	}

	public void setUnsubscribeRequest(Service service)
	{
		setMethod(Subscription.UNSUBSCRIBE_METHOD);
		setService(service);
		setSID(service.getSID());
	}

	////////////////////////////////////////////////
	//	NT
	////////////////////////////////////////////////

	public void setNT(String value)
	{
		setHeader(HTTP.NT, value);
	}

	public String getNT()
	{
		return getHeaderValue(HTTP.NT);
	}
	
	public boolean hasNT()
	{
		String nt = getNT();
		return (nt != null && 0 < nt.length()) ? true : false;
	}
	
	////////////////////////////////////////////////
	//	CALLBACK
	////////////////////////////////////////////////

	private final static String CALLBACK_START_WITH  = "<";
	private final static String CALLBACK_END_WITH  = ">";
	
	public void setCallback(String value)
	{
		setStringHeader(HTTP.CALLBACK, value, CALLBACK_START_WITH, CALLBACK_END_WITH);
	}
	
	public String getCallback()
	{
		return getStringHeaderValue(HTTP.CALLBACK, CALLBACK_START_WITH, CALLBACK_END_WITH);
	}
	
	public boolean hasCallback()
	{
		String callback = getCallback();
		return (callback != null && 0 < callback.length()) ? true : false;
	}

	////////////////////////////////////////////////
	//	SID
	////////////////////////////////////////////////

	public void setSID(String id)
	{
		setHeader(HTTP.SID, Subscription.toSIDHeaderString(id));
	}

	public String getSID()
	{
		// Thanks for Grzegorz Lehmann and Stefano Lenzi(12/06/04)
		String sid = Subscription.getSID(getHeaderValue(HTTP.SID));
		if (sid == null)
			return "";
		return sid;
	}
	
	public boolean hasSID()
	{
		String sid = getSID();
		return (sid != null && 0 < sid.length()) ? true : false;
	}

	////////////////////////////////////////////////
	//	Timeout
	////////////////////////////////////////////////

	public final void setTimeout(long value)
	{
		setHeader(HTTP.TIMEOUT, Subscription.toTimeoutHeaderString(value));
	}

	public long getTimeout()
	{
		return Subscription.getTimeout(getHeaderValue(HTTP.TIMEOUT));
	}

	////////////////////////////////////////////////
	//	post (Response)
	////////////////////////////////////////////////

	public void post(SubscriptionResponse subRes)
	{
		super.post(subRes);
	}

	////////////////////////////////////////////////
	//	post
	////////////////////////////////////////////////

	public SubscriptionResponse post()
	{
		HTTPResponse httpRes = post(getRequestHost(), getRequestPort());
		return new SubscriptionResponse(httpRes);
	}
}
