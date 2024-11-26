/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: UPnP.java
*
*	Revision;
*
*	11/18/02
*		- first revision.
*	05/13/03
*		- Added support for IPv6 and loopback address.
*	12/26/03
*		- Added support for XML Parser
*	06/18/03
*		- Added INMPR03 and INMPR03_VERSION.
*	04/14/06
*		- Added some functios about time-to-live, and the default value is 4.
*	05/11/09
*		- Changed loadDefaultXMLParser() to load org.cybergarage.xml.parser.XmlPullParser at first.
*	
******************************************************************/

package org.cybergarage.upnp;

import org.cybergarage.net.HostInterface;
import org.cybergarage.soap.SOAP;
import org.cybergarage.upnp.ssdp.SSDP;
import org.cybergarage.util.Debug;
import org.cybergarage.xml.Parser;

public class UPnP
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	/**
	 * Name of the system properties used to identifies the default XML Parser.<br>
	 * The value of the properties MUST BE the fully qualified class name of<br>
	 * XML Parser which CyberLink should use. 
	 */
	public final static String XML_CLASS_PROPERTTY="cyberlink.upnp.xml.parser";
	
	public final static String NAME = "CyberLinkJava";
	public final static String VERSION = "3.0";

	public final static int SERVER_RETRY_COUNT = 100;
	public final static int DEFAULT_EXPIRED_DEVICE_EXTRA_TIME = 60;

	public final static String getServerName()
	{
		String osName = System.getProperty("os.name");
		String osVer = System.getProperty("os.version");
		return osName + "/"  + osVer + " UPnP/1.0 " + NAME + "/" + VERSION;
	}
	
	public final static String INMPR03 = "INMPR03";
	public final static String INMPR03_VERSION = "1.0";
	public final static int INMPR03_DISCOVERY_OVER_WIRELESS_COUNT = 4;

	public final static String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"; 
	
	public final static int CONFIGID_UPNP_ORG_MAX = 16777215;
	
	////////////////////////////////////////////////
	//	Enable / Disable
	////////////////////////////////////////////////
	
	public final static int USE_ONLY_IPV6_ADDR = 1;
	public final static int USE_LOOPBACK_ADDR = 2;
	public final static int USE_IPV6_LINK_LOCAL_SCOPE = 3;
	public final static int USE_IPV6_SUBNET_SCOPE = 4;
	public final static int USE_IPV6_ADMINISTRATIVE_SCOPE = 5;
	public final static int USE_IPV6_SITE_LOCAL_SCOPE = 6;
	public final static int USE_IPV6_GLOBAL_SCOPE = 7;
	public final static int USE_SSDP_SEARCHRESPONSE_MULTIPLE_INTERFACES = 8;
	public final static int USE_ONLY_IPV4_ADDR = 9;
	
	public final static void setEnable(int value)
	{
		switch (value) {
		case USE_ONLY_IPV6_ADDR:
			{
				HostInterface.USE_ONLY_IPV6_ADDR = true;
			}
			break;	
		case USE_ONLY_IPV4_ADDR:
			{
				HostInterface.USE_ONLY_IPV4_ADDR = true;
			}
			break;	
		case USE_LOOPBACK_ADDR:
			{
				HostInterface.USE_LOOPBACK_ADDR = true;
			}
			break;	
		case USE_IPV6_LINK_LOCAL_SCOPE:
			{
				SSDP.setIPv6Address(SSDP.IPV6_LINK_LOCAL_ADDRESS);
			}
			break;	
		case USE_IPV6_SUBNET_SCOPE:
			{
				SSDP.setIPv6Address(SSDP.IPV6_SUBNET_ADDRESS);
			}
			break;	
		case USE_IPV6_ADMINISTRATIVE_SCOPE:
			{
				SSDP.setIPv6Address(SSDP.IPV6_ADMINISTRATIVE_ADDRESS);
			}
			break;	
		case USE_IPV6_SITE_LOCAL_SCOPE:
			{
				SSDP.setIPv6Address(SSDP.IPV6_SITE_LOCAL_ADDRESS);
			}
			break;	
		case USE_IPV6_GLOBAL_SCOPE:
			{
				SSDP.setIPv6Address(SSDP.IPV6_GLOBAL_ADDRESS);
			}
			break;	
		}
	}

	public final static void setDisable(int value)
	{
		switch (value) {
		case USE_ONLY_IPV6_ADDR:
			{
				HostInterface.USE_ONLY_IPV6_ADDR = false;
			}
			break;	
		case USE_ONLY_IPV4_ADDR:
			{
				HostInterface.USE_ONLY_IPV4_ADDR = false;
			}
			break;	
		case USE_LOOPBACK_ADDR:
			{
				HostInterface.USE_LOOPBACK_ADDR = false;
			}
			break;	
		}
	}

	public final static boolean isEnabled(int value)
	{
		switch (value) {
		case USE_ONLY_IPV6_ADDR:
			{
				return HostInterface.USE_ONLY_IPV6_ADDR;
			}
		case USE_ONLY_IPV4_ADDR:
			{
				return HostInterface.USE_ONLY_IPV4_ADDR;
			}
		case USE_LOOPBACK_ADDR:
			{
				return HostInterface.USE_LOOPBACK_ADDR;
			}
		}
		return false;
	}

	////////////////////////////////////////////////
	//	UUID
	////////////////////////////////////////////////

	private static final String toUUID(int seed)
	{
		String id = Integer.toString((int)(seed & 0xFFFF), 16);
		int idLen = id.length();
		String uuid = "";
		for (int n=0; n<(4-idLen); n++)
			uuid += "0";
		uuid += id;
		return uuid;
	}
	
	public static final String createUUID()
	{
		long time1 = System.currentTimeMillis();
		long time2 = (long)((double)System.currentTimeMillis() * Math.random());
		return 
			toUUID((int)(time1 & 0xFFFF)) + "-" +
			toUUID((int)((time1 >> 32) | 0xA000) & 0xFFFF) + "-" +
			toUUID((int)(time2 & 0xFFFF)) + "-" +
			toUUID((int)((time2 >> 32) | 0xE000) & 0xFFFF);
	}

	////////////////////////////////////////////////
	//	BootId
	////////////////////////////////////////////////

	public static final int createBootId()
	{
		return (int)(System.currentTimeMillis() / 1000L);
	}
	
	////////////////////////////////////////////////
	//	ConfigId
	////////////////////////////////////////////////

	public static final int caluculateConfigId(String configXml)
	{
		if (configXml == null)
			return 0;
		int configId = 0;
		int configLen = configXml.length();
		for (int n=0; n<configLen; n++) {
			configId += configXml.codePointAt(n);
			if (configId < CONFIGID_UPNP_ORG_MAX)
				continue;
			configId = configId % CONFIGID_UPNP_ORG_MAX;
		}
		return configId;
	}
	
	////////////////////////////////////////////////
	// XML Parser
	////////////////////////////////////////////////

	private static Parser xmlParser;
	
	public final static void setXMLParser(Parser parser)
	{
		xmlParser = parser;
		SOAP.setXMLParser(parser);
	}
	
	public final static Parser getXMLParser()
	{
		if(xmlParser == null){
			xmlParser = loadDefaultXMLParser();
			if(xmlParser == null)
				throw new RuntimeException("No XML parser defined. And unable to laod any. \n" +
						"Try to invoke UPnP.setXMLParser before UPnP.getXMLParser");			
			SOAP.setXMLParser(xmlParser);
		}
		return xmlParser;
	}

	/**
	 * This method loads the default XML Parser using the following behavior:
	 *  - First if present loads the parsers specified by the system property {@link UPnP#XML_CLASS_PROPERTTY}<br>
	 *  - Second by a fall-back technique, it tries to load the XMLParser from one<br>
	 *  of the following classes: {@link JaxpParser}, {@link kXML2Parser}, {@link XercesParser}
	 * 
	 * @return {@link Parser} which has been loaded successuflly or null otherwise
	 * 
	 * @since 1.8.0
	 */
	private static Parser loadDefaultXMLParser() {
		Parser parser = null;
		
		String[] parserClass = new String[]{
				System.getProperty(XML_CLASS_PROPERTTY),
				"org.cybergarage.xml.parser.XmlPullParser",
				"org.cybergarage.xml.parser.JaxpParser",
				"org.cybergarage.xml.parser.kXML2Parser"
		};
		
		for (int i = 0; i < parserClass.length; i++) {
			if(parserClass[i]==null)
				continue;
			try {
				parser = (Parser) Class.forName(parserClass[i]).newInstance();
				return parser;
			} catch (Throwable e) {
				Debug.warning("Unable to load "+parserClass[i]+" as XMLParser due to "+e);
			}
		}
		return null;
	}
	
	////////////////////////////////////////////////
	//	TTL
	////////////////////////////////////////////////	

	public final static int DEFAULT_TTL = 4;

	private static int timeToLive = DEFAULT_TTL;
	
	public final static void setTimeToLive(int value)
	{
		timeToLive = value;
	}
	
	public final static int getTimeToLive()
	{
		return timeToLive;
	}
	
	////////////////////////////////////////////////
	//	Initialize
	////////////////////////////////////////////////
	
	static 
	{
		////////////////////////////
		// Interface Option
		////////////////////////////
		
		//setXMLParser(new kXML2Parser());
		
		
		////////////////////////////
		// TimeToLive
		////////////////////////////
		
		setTimeToLive(DEFAULT_TTL);
		
		////////////////////////////
		// Debug Option
		////////////////////////////
		
		//Debug.on();
	}
	
	public final static void initialize()
	{
		// Dummy function to call UPnP.static
	}

}
