/******************************************************************
 *
 *	CyberLink for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: Device.java
 *
 *	Revision:
 *
 *	11/28/02
 *		- first revision.
 *	02/26/03
 *		- URLBase is updated automatically.
 * 		- Description of a root device is returned from the XML node tree.
 *	05/13/03
 *		- URLBase is updated when the request is received.
 *		- Changed to create socket threads each local interfaces.
 *		  (HTTP, SSDPSearch)
 *	06/17/03
 *		- Added notify all state variables when a new subscription is received.
 *	06/18/03
 *		- Fixed a announce bug when the bind address is null on J2SE v 1.4.1_02 and Redhat 9.
 *	09/02/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Problem : bad request response sent even with successful subscriptions
 *		- Error : a return statement is missing in the httpRequestRecieved method
 *	10/21/03
 *		- Updated a udn field by a original uuid.
 *	10/22/03
 *		- Added setActionListener().
 *		- Added setQueryListener().
 *	12/12/03
 *		- Added a static() to initialize UPnP class.
 *	12/25/03
 *		- Added advertiser functions.
 *	01/05/04
 *		- Added isExpired().
 *	03/23/04
 *		- Oliver Newell <newell@media-rush.com>
 *		- Changed to update the UDN only when the field is null.
 *	04/21/04
 *		- Added isDeviceType().
 *	06/18/04
 *		- Added setNMPRMode() and isNMPRMode().
 *		- Changed getDescriptionData() to update only when the NMPR mode is false.
 *	06/21/04
 *		- Changed start() to send a bye-bye before the announce.
 *		- Changed annouce(), byebye() and deviceSearchReceived() to send the SSDP
 *		  messsage four times when the NMPR and the Wireless mode are true.
 *	07/02/04
 *		- Fixed announce() and byebye() to send the upnp::rootdevice message despite embedded devices.
 *		- Fixed getRootNode() to return the root node when the device is embedded.
 *	07/24/04
 *		- Thanks for Stefano Lenzi <kismet-sl@users.sourceforge.net>
 *		- Added getParentDevice().
 *	10/20/04 
 *		- Brent Hills <bhills@openshores.com>
 *		- Changed postSearchResponse() to add MYNAME header.
 *	11/19/04
 *		- Theo Beisch <theo.beisch@gmx.de>
 *		- Added getStateVariable(String serviceType, String name).
 *	03/22/05
 *		- Changed httpPostRequestRecieved() to return the bad request when the post request isn't the soap action.
 *	03/23/05
 *		- Added loadDescription(String) to load the description from memory.
 *	03/30/05
 *		- Added getDeviceByDescriptionURI().
 *		- Added getServiceBySCPDURL().
 *	03/31/05
 *		- Changed httpGetRequestRecieved() to return the description stream using
 *		  Device::getDescriptionData() and Service::getSCPDData() at first.
 *	04/25/05
 *		- Thanks for Mikael Hakman <mhakman@dkab.net>
 *		  Changed announce() and byebye() to close the socket after the posting.
 *	04/25/05
 *		- Thanks for Mikael Hakman <mhakman@dkab.net>
 *		  Changed deviceSearchResponse() answer with USN:UDN::<device-type> when request ST is device type.
 * 	04/25/05
 *		- Thanks for Mikael Hakman <mhakman@dkab.net>
 * 		- Changed getDescriptionData() to add a XML declaration at first line.
 * 	04/25/05
 *		- Thanks for Mikael Hakman <mhakman@dkab.net>
 *		- Added a new setActionListener() and serQueryListner() to include the sub devices. 
 *	07/24/05
 *		- Thanks for Stefano Lenzi <kismet-sl@users.sourceforge.net>
 *		- Fixed a bug of getParentDevice() to return the parent device normally.
 *	02/21/06
 *		- Changed httpRequestRecieved() not to ignore HEAD requests.
 *	04/12/06
 *		- Added setUserData() and getUserData() to set a user original data object.
 *	03/29/08
 *		- Added isRunning() to know whether the device is running.
 * 
 ******************************************************************/

package org.cybergarage.upnp;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

import org.cybergarage.http.HTTP;
import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.HTTPServerList;
import org.cybergarage.http.HTTPStatus;
import org.cybergarage.net.HostInterface;
import org.cybergarage.soap.SOAPResponse;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.ActionRequest;
import org.cybergarage.upnp.control.ActionResponse;
import org.cybergarage.upnp.control.ControlRequest;
import org.cybergarage.upnp.control.ControlResponse;
import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.upnp.control.QueryRequest;
import org.cybergarage.upnp.device.Advertiser;
import org.cybergarage.upnp.device.Description;
import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.cybergarage.upnp.device.NTS;
import org.cybergarage.upnp.device.ST;
import org.cybergarage.upnp.device.SearchListener;
import org.cybergarage.upnp.device.USN;
import org.cybergarage.upnp.device.PresentationListener;
import org.cybergarage.upnp.event.Subscriber;
import org.cybergarage.upnp.event.Subscription;
import org.cybergarage.upnp.event.SubscriptionRequest;
import org.cybergarage.upnp.event.SubscriptionResponse;
import org.cybergarage.upnp.ssdp.SSDPNotifyRequest;
import org.cybergarage.upnp.ssdp.SSDPNotifySocket;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.cybergarage.upnp.ssdp.SSDPSearchResponse;
import org.cybergarage.upnp.ssdp.SSDPSearchResponseSocket;
import org.cybergarage.upnp.ssdp.SSDPSearchSocketList;
import org.cybergarage.upnp.xml.DeviceData;
import org.cybergarage.util.Debug;
import org.cybergarage.util.FileUtil;
import org.cybergarage.util.Mutex;
import org.cybergarage.util.TimerUtil;
import org.cybergarage.xml.Node;
import org.cybergarage.xml.Parser;
import org.cybergarage.xml.ParserException;
import org.cybergarage.xml.XML;

public class Device implements org.cybergarage.http.HTTPRequestListener,
		SearchListener {
	// //////////////////////////////////////////////
	// Constants
	// //////////////////////////////////////////////

	public final static String ELEM_NAME = "device";
	public final static String UPNP_ROOTDEVICE = "upnp:rootdevice";

	public final static int DEFAULT_STARTUP_WAIT_TIME = 1000;
	public final static int DEFAULT_DISCOVERY_WAIT_TIME = 300;
	public final static int DEFAULT_LEASE_TIME = 30 * 60;

	public final static int HTTP_DEFAULT_PORT = 4004;

	public final static String DEFAULT_DESCRIPTION_URI = "/description.xml";
	public final static String DEFAULT_PRESENTATION_URI = "/presentation";

	// //////////////////////////////////////////////
	// Member
	// //////////////////////////////////////////////

	private Node rootNode;
	private Node deviceNode;

	public Node getRootNode() {
		if (rootNode != null)
			return rootNode;
		if (deviceNode == null)
			return null;
		return deviceNode.getRootNode();
	}

	public Node getDeviceNode() {
		return deviceNode;
	}

	public void setRootNode(Node node) {
		rootNode = node;
	}

	public void setDeviceNode(Node node) {
		deviceNode = node;
	}

	// //////////////////////////////////////////////
	// Initialize
	// //////////////////////////////////////////////

	static {
		UPnP.initialize();
	}

	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	public Device(Node root, Node device) {
		rootNode = root;
		deviceNode = device;
		setUUID(UPnP.createUUID());
		setWirelessMode(false);
	}

	public Device() {
		this(null, null);
	}

	public Device(Node device) {
		this(null, device);
	}

	public Device(File descriptionFile) throws InvalidDescriptionException {
		this(null, null);
		loadDescription(descriptionFile);
	}

	/**
	 * @since 1.8.0
	 */
	public Device(InputStream input) throws InvalidDescriptionException {
		this(null, null);
		loadDescription(input);
	}

	public Device(String descriptionFileName)
			throws InvalidDescriptionException {
		this(new File(descriptionFileName));
	}

	// //////////////////////////////////////////////
	// Mutex
	// //////////////////////////////////////////////

	private Mutex mutex = new Mutex();

	public void lock() {
		mutex.lock();
	}

	public void unlock() {
		mutex.unlock();
	}

	// //////////////////////////////////////////////
	// getAbsoluteURL
	// //////////////////////////////////////////////

	public String getAbsoluteURL(String urlString, String baseURLStr,
			String locationURLStr) {
		if ((urlString == null) || (urlString.length() <= 0))
			return "";

		try {
			URL url = new URL(urlString);
			return url.toString();
		} catch (Exception e) {
		}

		if ((baseURLStr == null) || (baseURLStr.length() <= 0)) {
			if ((locationURLStr != null) && (0 < locationURLStr.length())) {
				if (!locationURLStr.endsWith("/") || !urlString.startsWith("/")) {
					try {
						URL locationURL = new URL(locationURLStr);
						// 重新拼接url
						String absUrl = locationURL.getProtocol() + "://" + locationURL.getHost() + ":" + locationURL.getPort() + urlString;
						URL url = new URL(absUrl);
						return url.toString();
					} catch (Exception e) {}
				} else {
					String absUrl = locationURLStr + urlString.substring(1);
					try {
						URL url = new URL(absUrl);
						return url.toString();
					} catch (Exception e) {
					}
				}

				String absUrl = HTTP.getAbsoluteURL(locationURLStr, urlString);
				try {
					URL url = new URL(absUrl);
					return url.toString();
				} catch (Exception e) {
				}

				// Thanks for Steven Yen (2003/09/03)
				Device rootDev = getRootDevice();
				if (rootDev != null) {
					String location = rootDev.getLocation();
					String locationHost = HTTP.getHost(location);
					int locationPort = HTTP.getPort(location);
					baseURLStr = HTTP.getRequestHostURL(locationHost,
							locationPort);
				}
			}
		}

		if ((baseURLStr != null) && (0 < baseURLStr.length())) {
			if (!baseURLStr.endsWith("/") || !urlString.startsWith("/")) {
				String absUrl = baseURLStr + urlString;
				try {
					URL url = new URL(absUrl);
					return url.toString();
				} catch (Exception e) {
				}
			} else {
				String absUrl = baseURLStr + urlString.substring(1);
				try {
					URL url = new URL(absUrl);
					return url.toString();
				} catch (Exception e) {
				}
			}

			String absUrl = HTTP.getAbsoluteURL(baseURLStr, urlString);
			try {
				URL url = new URL(absUrl);
				return url.toString();
			} catch (Exception e) {
			}
		}

		return urlString;
	}

	public String getAbsoluteURL(String urlString) {
		String baseURLStr = null;
		String locationURLStr = null;

		Device rootDev = getRootDevice();
		if (rootDev != null) {
			baseURLStr = rootDev.getURLBase();
			locationURLStr = rootDev.getLocation();
		}

		return getAbsoluteURL(urlString, baseURLStr, locationURLStr);
	}

	// //////////////////////////////////////////////
	// NMPR
	// //////////////////////////////////////////////

	public void setNMPRMode(boolean flag) {
		Node devNode = getDeviceNode();
		if (devNode == null)
			return;
		if (flag == true) {
			devNode.setNode(UPnP.INMPR03, UPnP.INMPR03_VERSION);
			devNode.removeNode(Device.URLBASE_NAME);
		} else {
			devNode.removeNode(UPnP.INMPR03);
		}
	}

	public boolean isNMPRMode() {
		Node devNode = getDeviceNode();
		if (devNode == null)
			return false;
		return (devNode.getNode(UPnP.INMPR03) != null) ? true : false;
	}

	// //////////////////////////////////////////////
	// Wireless
	// //////////////////////////////////////////////

	private boolean wirelessMode;

	public void setWirelessMode(boolean flag) {
		wirelessMode = flag;
	}

	public boolean isWirelessMode() {
		return wirelessMode;
	}

	public int getSSDPAnnounceCount() {
		if (isNMPRMode() == true && isWirelessMode() == true)
			return UPnP.INMPR03_DISCOVERY_OVER_WIRELESS_COUNT;
		return 1;
	}

	// //////////////////////////////////////////////
	// Device UUID
	// //////////////////////////////////////////////

	private String devUUID;

	private void setUUID(String uuid) {
		this.devUUID = uuid;
	}

	public String getUUID() {
		return this.devUUID;
	}

	private void updateUDN() {
		setUDN("uuid:" + getUUID());
	}

	// //////////////////////////////////////////////
	// BootId
	// //////////////////////////////////////////////

	private int bootId;

	private void updateBootId() {
		this.bootId = UPnP.createBootId();
	}

	public int getBootId() {
		return this.bootId;
	}

	// //////////////////////////////////////////////
	// configID
	// //////////////////////////////////////////////

	private final static String CONFIG_ID = "configId";

	private void updateConfigId(Device dev) {
		int configId = 0;

		DeviceList cdevList = dev.getDeviceList();
		int cdevCnt = cdevList.size();
		for (int n = 0; n < cdevCnt; n++) {
			Device cdev = cdevList.getDevice(n);
			updateConfigId(cdev);
			configId += cdev.getConfigId();
			configId &= UPnP.CONFIGID_UPNP_ORG_MAX;
		}

		ServiceList serviceList = dev.getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			service.updateConfigId();
			configId += service.getConfigId();
			configId &= UPnP.CONFIGID_UPNP_ORG_MAX;
		}

		Node devNode = getDeviceNode();
		if (devNode == null)
			return;

		String devDescXml = devNode.toString();
		configId += UPnP.caluculateConfigId(devDescXml);
		configId &= UPnP.CONFIGID_UPNP_ORG_MAX;
		devNode.setAttribute(CONFIG_ID, configId);
	}

	public void updateConfigId() {
		updateConfigId(this);
	}

	public int getConfigId() {
		Node devNode = getDeviceNode();
		if (devNode == null)
			return 0;
		return devNode.getAttributeIntegerValue(CONFIG_ID);
	}

	// //////////////////////////////////////////////
	// Root Device
	// //////////////////////////////////////////////

	public Device getRootDevice() {
		Node rootNode = getRootNode();
		if (rootNode == null)
			return null;
		Node devNode = rootNode.getNode(Device.ELEM_NAME);
		if (devNode == null)
			return null;
		return new Device(rootNode, devNode);
	}

	// //////////////////////////////////////////////
	// Parent Device
	// //////////////////////////////////////////////

	// Thanks for Stefano Lenzi (07/24/04)

	/**
	 * 
	 * @return A Device that contain this object.<br>
	 *         Return <code>null</code> if this is a root device.
	 * @author Stefano "Kismet" Lenzi
	 */
	public Device getParentDevice() {
		if (isRootDevice())
			return null;
		Node devNode = getDeviceNode();
		Node aux = null;
		// <device><deviceList><device>
		aux = devNode.getParentNode().getParentNode();
		return new Device(aux);
	}

	/**
	 * Add a Service to device without checking for duplicate or syntax error
	 * 
	 * @param s
	 *            Add Service s to the Device
	 */
	public void addService(Service s) {
		Node serviceListNode = getDeviceNode().getNode(ServiceList.ELEM_NAME);
		if (serviceListNode == null) {
			serviceListNode = new Node(ServiceList.ELEM_NAME);
			getDeviceNode().addNode(serviceListNode);
		}
		serviceListNode.addNode(s.getServiceNode());
	}

	/**
	 * Add a Device to device without checking for duplicate or syntax error.
	 * This method set or reset the root node of the Device and itself<br>
	 * <br>
	 * Note: This method should be used to create a dynamic<br>
	 * Device withtout writing any XML that describe the device<br>
	 * .
	 * 
	 * @param d
	 *            Add Device d to the Device
	 * 
	 * @author Stefano "Kismet" Lenzi - kismet-sl@users.sourceforge.net - 2005
	 * 
	 */
	public void addDevice(Device d) {
		Node deviceListNode = getDeviceNode().getNode(DeviceList.ELEM_NAME);
		if (deviceListNode == null) {
			// deviceListNode = new Node(ServiceList.ELEM_NAME); twa wrong
			// ELEM_NAME;
			deviceListNode = new Node(DeviceList.ELEM_NAME);
			getDeviceNode().addNode(deviceListNode);
		}
		deviceListNode.addNode(d.getDeviceNode());
		d.setRootNode(null);
		if (getRootNode() == null) {
			Node root = new Node(RootDescription.ROOT_ELEMENT);
			root.setNameSpace("", RootDescription.ROOT_ELEMENT_NAMESPACE);
			Node spec = new Node(RootDescription.SPECVERSION_ELEMENT);
			Node maj = new Node(RootDescription.MAJOR_ELEMENT);
			maj.setValue("1");
			Node min = new Node(RootDescription.MINOR_ELEMENT);
			min.setValue("0");
			spec.addNode(maj);
			spec.addNode(min);
			root.addNode(spec);
			setRootNode(root);
		}
	}

	// //////////////////////////////////////////////
	// UserData
	// //////////////////////////////////////////////

	private DeviceData getDeviceData() {
		Node node = getDeviceNode();
		DeviceData userData = (DeviceData) node.getUserData();
		if (userData == null) {
			userData = new DeviceData();
			node.setUserData(userData);
			userData.setNode(node);
		}
		return userData;
	}

	// //////////////////////////////////////////////
	// Description
	// //////////////////////////////////////////////

	private void setDescriptionFile(File file) {
		getDeviceData().setDescriptionFile(file);
	}

	public File getDescriptionFile() {
		return getDeviceData().getDescriptionFile();
	}

	private void setDescriptionURI(String uri) {
		getDeviceData().setDescriptionURI(uri);
	}

	private String getDescriptionURI() {
		return getDeviceData().getDescriptionURI();
	}

	private boolean isDescriptionURI(String uri) {
		String descriptionURI = getDescriptionURI();
		if (uri == null || descriptionURI == null)
			return false;
		return descriptionURI.equals(uri);
	}

	public String getDescriptionFilePath() {
		File descriptionFile = getDescriptionFile();
		if (descriptionFile == null)
			return "";
		return descriptionFile.getAbsoluteFile().getParent();
	}

	/**
	 * @since 1.8.0
	 */
	public boolean loadDescription(InputStream input)
			throws InvalidDescriptionException {
		try {
			Parser parser = UPnP.getXMLParser();
			rootNode = parser.parse(input);
			if (rootNode == null)
				throw new InvalidDescriptionException(
						Description.NOROOT_EXCEPTION);
			deviceNode = rootNode.getNode(Device.ELEM_NAME);
			if (deviceNode == null)
				throw new InvalidDescriptionException(
						Description.NOROOTDEVICE_EXCEPTION);
		} catch (ParserException e) {
			throw new InvalidDescriptionException(e);
		}

		if (initializeLoadedDescription() == false)
			return false;

		setDescriptionFile(null);

		return true;
	}

	public boolean loadDescription(String descString)
			throws InvalidDescriptionException {
		try {
			Parser parser = UPnP.getXMLParser();
			rootNode = parser.parse(descString);
			if (rootNode == null)
				throw new InvalidDescriptionException(
						Description.NOROOT_EXCEPTION);
			deviceNode = rootNode.getNode(Device.ELEM_NAME);
			if (deviceNode == null)
				throw new InvalidDescriptionException(
						Description.NOROOTDEVICE_EXCEPTION);
		} catch (ParserException e) {
			throw new InvalidDescriptionException(e);
		}

		if (initializeLoadedDescription() == false)
			return false;

		setDescriptionFile(null);

		return true;
	}

	public boolean loadDescription(File file)
			throws InvalidDescriptionException {
		try {
			Parser parser = UPnP.getXMLParser();
			rootNode = parser.parse(file);
			if (rootNode == null)
				throw new InvalidDescriptionException(
						Description.NOROOT_EXCEPTION, file);
			deviceNode = rootNode.getNode(Device.ELEM_NAME);
			if (deviceNode == null)
				throw new InvalidDescriptionException(
						Description.NOROOTDEVICE_EXCEPTION, file);
		} catch (ParserException e) {
			throw new InvalidDescriptionException(e);
		}

		if (initializeLoadedDescription() == false)
			return false;

		setDescriptionFile(file);

		return true;
	}

	private boolean initializeLoadedDescription() {
		setDescriptionURI(DEFAULT_DESCRIPTION_URI);
		setLeaseTime(DEFAULT_LEASE_TIME);
		setHTTPPort(HTTP_DEFAULT_PORT);

		// Thanks for Oliver Newell (03/23/04)
		if (hasUDN() == false)
			updateUDN();

		return true;
	}

	// //////////////////////////////////////////////
	// isDeviceNode
	// //////////////////////////////////////////////

	public static boolean isDeviceNode(Node node) {
		return Device.ELEM_NAME.equals(node.getName());
	}

	// //////////////////////////////////////////////
	// Root Device
	// //////////////////////////////////////////////

	public boolean isRootDevice() {
		return getRootNode().getNode("device").getNodeValue(UDN)
				.equals(getUDN());
	}

	// //////////////////////////////////////////////
	// Root Device
	// //////////////////////////////////////////////

	public void setSSDPPacket(SSDPPacket packet) {
		getDeviceData().setSSDPPacket(packet);
	}

	public SSDPPacket getSSDPPacket() {
		if (isRootDevice() == false)
			return null;
		return getDeviceData().getSSDPPacket();
	}

	// //////////////////////////////////////////////
	// Location
	// //////////////////////////////////////////////

	public void setLocation(String value) {
		getDeviceData().setLocation(value);
	}

	public String getLocation() {
		SSDPPacket packet = getSSDPPacket();
		if (packet != null)
			return packet.getLocation();
		return getDeviceData().getLocation();
	}

	// //////////////////////////////////////////////
	// LeaseTime
	// //////////////////////////////////////////////

	public void setLeaseTime(int value) {
		getDeviceData().setLeaseTime(value);
		Advertiser adv = getAdvertiser();
		if (adv != null) {
			announce();
			adv.restart();
		}
	}

	public int getLeaseTime() {
		SSDPPacket packet = getSSDPPacket();
		if (packet != null)
			return packet.getLeaseTime();
		return getDeviceData().getLeaseTime();
	}

	// //////////////////////////////////////////////
	// TimeStamp
	// //////////////////////////////////////////////

	public long getTimeStamp() {
		SSDPPacket packet = getSSDPPacket();
		if (packet != null)
			return packet.getTimeStamp();
		return 0;
	}

	public long getElapsedTime() {
		return (System.currentTimeMillis() - getTimeStamp()) / 1000;
	}

	public boolean isExpired() {
		long elipsedTime = getElapsedTime();
		long leaseTime = getLeaseTime()
				+ UPnP.DEFAULT_EXPIRED_DEVICE_EXTRA_TIME;
		if (leaseTime < elipsedTime)
			return true;
		return false;
	}

	// //////////////////////////////////////////////
	// URL Base
	// //////////////////////////////////////////////

	private final static String URLBASE_NAME = "URLBase";

	private void setURLBase(String value) {
		if (isRootDevice() == true) {
			Node node = getRootNode().getNode(URLBASE_NAME);
			if (node != null) {
				node.setValue(value);
				return;
			}
			node = new Node(URLBASE_NAME);
			node.setValue(value);
			int index = 1;
			if (getRootNode().hasNodes() == false)
				index = 1;
			getRootNode().insertNode(node, index);
		}
	}

	private void updateURLBase(String host) {
		String urlBase = HostInterface.getHostURL(host, getHTTPPort(), "");
		setURLBase(urlBase);
	}

	public String getURLBase() {
		if (isRootDevice() == true)
			return getRootNode().getNodeValue(URLBASE_NAME);
		return "";
	}

	// //////////////////////////////////////////////
	// deviceType
	// //////////////////////////////////////////////

	private final static String DEVICE_TYPE = "deviceType";

	public void setDeviceType(String value) {
		getDeviceNode().setNode(DEVICE_TYPE, value);
	}

	public String getDeviceType() {
		return getDeviceNode().getNodeValue(DEVICE_TYPE);
	}

	public boolean isDeviceType(String value) {
		if (value == null)
			return false;
		return value.equals(getDeviceType());
	}

	// //////////////////////////////////////////////
	// friendlyName
	// //////////////////////////////////////////////

	private final static String FRIENDLY_NAME = "friendlyName";

	public void setFriendlyName(String value) {
		getDeviceNode().setNode(FRIENDLY_NAME, value);
	}

	public String getFriendlyName() {
		return getDeviceNode().getNodeValue(FRIENDLY_NAME);
	}

	// //////////////////////////////////////////////
	// manufacture
	// //////////////////////////////////////////////

	private final static String MANUFACTURE = "manufacturer";

	public void setManufacture(String value) {
		getDeviceNode().setNode(MANUFACTURE, value);
	}

	public String getManufacture() {
		return getDeviceNode().getNodeValue(MANUFACTURE);
	}

	// //////////////////////////////////////////////
	// manufactureURL
	// //////////////////////////////////////////////

	private final static String MANUFACTURE_URL = "manufacturerURL";

	public void setManufactureURL(String value) {
		getDeviceNode().setNode(MANUFACTURE_URL, value);
	}

	public String getManufactureURL() {
		return getDeviceNode().getNodeValue(MANUFACTURE_URL);
	}

	// //////////////////////////////////////////////
	// modelDescription
	// //////////////////////////////////////////////

	private final static String MODEL_DESCRIPTION = "modelDescription";

	public void setModelDescription(String value) {
		getDeviceNode().setNode(MODEL_DESCRIPTION, value);
	}

	public String getModelDescription() {
		return getDeviceNode().getNodeValue(MODEL_DESCRIPTION);
	}

	// //////////////////////////////////////////////
	// modelName
	// //////////////////////////////////////////////

	private final static String MODEL_NAME = "modelName";

	public void setModelName(String value) {
		getDeviceNode().setNode(MODEL_NAME, value);
	}

	public String getModelName() {
		return getDeviceNode().getNodeValue(MODEL_NAME);
	}

	// //////////////////////////////////////////////
	// modelNumber
	// //////////////////////////////////////////////

	private final static String MODEL_NUMBER = "modelNumber";

	public void setModelNumber(String value) {
		getDeviceNode().setNode(MODEL_NUMBER, value);
	}

	public String getModelNumber() {
		return getDeviceNode().getNodeValue(MODEL_NUMBER);
	}

	// //////////////////////////////////////////////
	// modelURL
	// //////////////////////////////////////////////

	private final static String MODEL_URL = "modelURL";

	public void setModelURL(String value) {
		getDeviceNode().setNode(MODEL_URL, value);
	}

	public String getModelURL() {
		return getDeviceNode().getNodeValue(MODEL_URL);
	}

	// //////////////////////////////////////////////
	// serialNumber
	// //////////////////////////////////////////////

	private final static String SERIAL_NUMBER = "serialNumber";

	public void setSerialNumber(String value) {
		getDeviceNode().setNode(SERIAL_NUMBER, value);
	}

	public String getSerialNumber() {
		return getDeviceNode().getNodeValue(SERIAL_NUMBER);
	}

	// //////////////////////////////////////////////
	// UDN
	// //////////////////////////////////////////////

	private final static String UDN = "UDN";

	public void setUDN(String value) {
		getDeviceNode().setNode(UDN, value);
	}

	public String getUDN() {
		return getDeviceNode().getNodeValue(UDN);
	}

	public boolean hasUDN() {
		String udn = getUDN();
		if (udn == null || udn.length() <= 0)
			return false;
		return true;
	}

	// //////////////////////////////////////////////
	// UPC
	// //////////////////////////////////////////////

	private final static String UPC = "UPC";

	public void setUPC(String value) {
		getDeviceNode().setNode(UPC, value);
	}

	public String getUPC() {
		return getDeviceNode().getNodeValue(UPC);
	}

	// //////////////////////////////////////////////
	// presentationURL
	// //////////////////////////////////////////////

	private final static String presentationURL = "presentationURL";
	private PresentationListener presentationListener;

	public void setPresentationURL(String value) {
		getDeviceNode().setNode(presentationURL, value);
	}

	public String getPresentationURL() {
		return getDeviceNode().getNodeValue(presentationURL);
	}

	public boolean removePresentationURL() {
		return getDeviceNode().removeNode(presentationURL);
	}

	private boolean isPresentationRequest(HTTPRequest httpReq) {
		if (!httpReq.isGetRequest())
			return false;
		String urlPath = httpReq.getURI();
		if (urlPath == null)
			return false;
		String presentationURL = getPresentationURL();
		if (presentationURL == null)
			return false;
		return urlPath.startsWith(presentationURL);
	}

	public void setPresentationListener(PresentationListener listener) {
		this.presentationListener = listener;

		if (listener != null) {
			setPresentationURL(DEFAULT_PRESENTATION_URI);
		} else {
			removePresentationURL();
		}
	}

	public boolean hasPresentationListener() {
		return (this.presentationListener != null) ? true : false;
	}

	public PresentationListener getPresentationListener() {
		return this.presentationListener;
	}

	// //////////////////////////////////////////////
	// deviceList
	// //////////////////////////////////////////////

	public DeviceList getDeviceList() {
		DeviceList devList = new DeviceList();
		Node devListNode = getDeviceNode().getNode(DeviceList.ELEM_NAME);
		if (devListNode == null)
			return devList;
		int nNode = devListNode.getNNodes();
		for (int n = 0; n < nNode; n++) {
			Node node = devListNode.getNode(n);
			if (Device.isDeviceNode(node) == false)
				continue;
			Device dev = new Device(node);
			devList.add(dev);
		}
		return devList;
	}

	public boolean isDevice(String name) {
		if (name == null)
			return false;
		if (name.endsWith(getUDN()) == true)
			return true;
		if (name.equals(getFriendlyName()) == true)
			return true;
		if (name.endsWith(getDeviceType()) == true)
			return true;
		return false;
	}

	public Device getDevice(String name) {
		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			if (dev.isDevice(name) == true)
				return dev;
			Device cdev = dev.getDevice(name);
			if (cdev != null)
				return cdev;
		}
		return null;
	}

	public Device getDeviceByDescriptionURI(String uri) {
		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			if (dev.isDescriptionURI(uri) == true)
				return dev;
			Device cdev = dev.getDeviceByDescriptionURI(uri);
			if (cdev != null)
				return cdev;
		}
		return null;
	}

	// //////////////////////////////////////////////
	// serviceList
	// //////////////////////////////////////////////

	public ServiceList getServiceList() {
		ServiceList serviceList = new ServiceList();
		Node serviceListNode = getDeviceNode().getNode(ServiceList.ELEM_NAME);
		if (serviceListNode == null)
			return serviceList;
		int nNode = serviceListNode.getNNodes();
		for (int n = 0; n < nNode; n++) {
			Node node = serviceListNode.getNode(n);
			if (Service.isServiceNode(node) == false)
				continue;
			Service service = new Service(node);
			serviceList.add(service);
		}
		return serviceList;
	}

	public Service getService(String name) {
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			if (service.isService(name) == true)
				return service;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			Service service = dev.getService(name);
			if (service != null)
				return service;
		}

		return null;
	}

	public Service getServiceBySCPDURL(String searchUrl) {
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			if (service.isSCPDURL(searchUrl) == true)
				return service;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			Service service = dev.getServiceBySCPDURL(searchUrl);
			if (service != null)
				return service;
		}

		return null;
	}

	public Service getServiceByControlURL(String searchUrl) {
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			if (service.isControlURL(searchUrl) == true)
				return service;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			Service service = dev.getServiceByControlURL(searchUrl);
			if (service != null)
				return service;
		}

		return null;
	}

	public Service getServiceByEventSubURL(String searchUrl) {
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			if (service.isEventSubURL(searchUrl) == true)
				return service;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			Service service = dev.getServiceByEventSubURL(searchUrl);
			if (service != null)
				return service;
		}

		return null;
	}

	public Service getSubscriberService(String uuid) {
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			String sid = service.getSID();
			if (uuid.equals(sid) == true)
				return service;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			Service service = dev.getSubscriberService(uuid);
			if (service != null)
				return service;
		}

		return null;
	}

	// //////////////////////////////////////////////
	// StateVariable
	// //////////////////////////////////////////////

	public StateVariable getStateVariable(String serviceType, String name) {
		if (serviceType == null && name == null)
			return null;

		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			// Thanks for Theo Beisch (11/09/04)
			if (serviceType != null) {
				if (service.getServiceType().equals(serviceType) == false)
					continue;
			}
			StateVariable stateVar = service.getStateVariable(name);
			if (stateVar != null)
				return stateVar;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			StateVariable stateVar = dev.getStateVariable(serviceType, name);
			if (stateVar != null)
				return stateVar;
		}

		return null;
	}

	public StateVariable getStateVariable(String name) {
		return getStateVariable(null, name);
	}

	// //////////////////////////////////////////////
	// Action
	// //////////////////////////////////////////////

	public Action getAction(String name) {
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			ActionList actionList = service.getActionList();
			int actionCnt = actionList.size();
			for (int i = 0; i < actionCnt; i++) {
				Action action = (Action) actionList.getAction(i);
				String actionName = action.getName();
				if (actionName == null)
					continue;
				if (actionName.equals(name) == true)
					return action;
			}
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++) {
			Device dev = devList.getDevice(n);
			Action action = dev.getAction(name);
			if (action != null)
				return action;
		}

		return null;
	}

	// //////////////////////////////////////////////
	// iconList
	// //////////////////////////////////////////////

	private HashMap<String, byte[]> iconBytesMap = new HashMap<String, byte[]>();

	public boolean isIconBytesURI(String uri) {
		byte iconBytes[] = iconBytesMap.get(uri);
		if (iconBytes == null)
		{
		    Icon icon = getIconByURI(uri);
		    if(icon !=null)
		    {
		        return icon.hasBytes();
		    }
			return false;
		}
		return true;
	}

	public Icon getIconByURI(String uri) {
		IconList iconList = getIconList();
		if (iconList.size() <= 0)
			return null;

		int nIcon = iconList.size();
		for (int n = 0; n < nIcon; n++) {
			Icon icon = iconList.getIcon(n);
			if (icon.isURL(uri))
				return icon;
		}
		
		return null;
	}

	public boolean addIcon(Icon icon) {
		Node deviceNode = getDeviceNode();
		if (deviceNode == null)
			return false;

		Node iconListNode = deviceNode.getNode(IconList.ELEM_NAME);
		if (iconListNode == null) {
			iconListNode = new Node(IconList.ELEM_NAME);
			deviceNode.addNode(iconListNode);
		}

		Node iconNode = new Node(Icon.ELEM_NAME);
		if (icon.getIconNode() != null) {
			iconNode.set(icon.getIconNode());
		}
		iconListNode.addNode(iconNode);

		if (icon.hasURL() && icon.hasBytes()) {
			iconBytesMap.put(icon.getURL(), icon.getBytes());
		}

		return true;
	}

	public IconList getIconList() {
		IconList iconList = new IconList();
		Node iconListNode = getDeviceNode().getNode(IconList.ELEM_NAME);
		if (iconListNode == null)
			return iconList;
		int nNode = iconListNode.getNNodes();
		for (int n = 0; n < nNode; n++) {
			Node node = iconListNode.getNode(n);
			if (Icon.isIconNode(node) == false)
				continue;
			Icon icon = new Icon(node);
			if (icon.hasURL()) {
				String iconURL = icon.getURL();
				byte iconBytes[] = iconBytesMap.get(iconURL);
				if (iconBytes != null) {
					icon.setBytes(iconBytes);
				}
			}
			iconList.add(icon);
		}
		return iconList;
	}

	public Icon getIcon(int n) {
		IconList iconList = getIconList();
		if (n < 0 && (iconList.size() - 1) < n)
			return null;
		return iconList.getIcon(n);
	}

	public Icon getSmallestIcon() {
		Icon smallestIcon = null;
		IconList iconList = getIconList();
		int iconCount = iconList.size();
		for (int n = 0; n < iconCount; n++) {
			Icon icon = iconList.getIcon(n);
			if (null == smallestIcon) {
				smallestIcon = icon;
				continue;
			}
			if (icon.getWidth() < smallestIcon.getWidth())
				smallestIcon = icon;
		}

		return smallestIcon;
	}

	// //////////////////////////////////////////////
	// Notify
	// //////////////////////////////////////////////

	public String getLocationURL(String host) {
		return HostInterface.getHostURL(host, getHTTPPort(),
				getDescriptionURI());
	}

	private String getNotifyDeviceNT() {
		if (isRootDevice() == false)
			return getUDN();
		return UPNP_ROOTDEVICE;
	}

	private String getNotifyDeviceUSN() {
		if (isRootDevice() == false)
			return getUDN();
		return getUDN() + "::" + UPNP_ROOTDEVICE;
	}

	private String getNotifyDeviceTypeNT() {
		return getDeviceType();
	}

	private String getNotifyDeviceTypeUSN() {
		return getUDN() + "::" + getDeviceType();
	}

	public final static void notifyWait() {
		TimerUtil.waitRandom(DEFAULT_DISCOVERY_WAIT_TIME);
	}

	public void announce(String bindAddr) {
		String devLocation = getLocationURL(bindAddr);

		SSDPNotifySocket ssdpSock = new SSDPNotifySocket(bindAddr);

		SSDPNotifyRequest ssdpReq = new SSDPNotifyRequest();
		ssdpReq.setServer(UPnP.getServerName());
		ssdpReq.setLeaseTime(getLeaseTime());
		ssdpReq.setLocation(devLocation);
		ssdpReq.setNTS(NTS.ALIVE);
		ssdpReq.setBootId(getBootId());

		// uuid:device-UUID(::upnp:rootdevice)*
		if (isRootDevice() == true) {
			String devNT = getNotifyDeviceNT();
			String devUSN = getNotifyDeviceUSN();
			ssdpReq.setNT(devNT);
			ssdpReq.setUSN(devUSN);
			ssdpSock.post(ssdpReq);

			String devUDN = getUDN();
			ssdpReq.setNT(devUDN);
			ssdpReq.setUSN(devUDN);
			ssdpSock.post(ssdpReq);
		}

		// uuid:device-UUID::urn:schemas-upnp-org:device:deviceType:v
		String devNT = getNotifyDeviceTypeNT();
		String devUSN = getNotifyDeviceTypeUSN();
		ssdpReq.setNT(devNT);
		ssdpReq.setUSN(devUSN);
		ssdpSock.post(ssdpReq);

		// Thanks for Mikael Hakman (04/25/05)
		ssdpSock.close();

		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			service.announce(bindAddr);
		}

		DeviceList childDeviceList = getDeviceList();
		int childDeviceCnt = childDeviceList.size();
		for (int n = 0; n < childDeviceCnt; n++) {
			Device childDevice = childDeviceList.getDevice(n);
			childDevice.announce(bindAddr);
		}
	}

	public void announce() {
		notifyWait();
		InetAddress[] binds = getDeviceData().getHTTPBindAddress();
		String[] bindAddresses;
		if (binds != null) {
			bindAddresses = new String[binds.length];
			for (int i = 0; i < binds.length; i++) {
				bindAddresses[i] = binds[i].getHostAddress();
			}
		} else {
			int nHostAddrs = HostInterface.getNHostAddresses();
			bindAddresses = new String[nHostAddrs];
			for (int n = 0; n < nHostAddrs; n++) {
				bindAddresses[n] = HostInterface.getHostAddress(n);
			}
		}
		for (int j = 0; j < bindAddresses.length; j++) {
			if (bindAddresses[j] == null || bindAddresses[j].length() == 0)
				continue;
			int ssdpCount = getSSDPAnnounceCount();
			for (int i = 0; i < ssdpCount; i++)
				announce(bindAddresses[j]);

		}
	}

	public void byebye(String bindAddr) {
		SSDPNotifySocket ssdpSock = new SSDPNotifySocket(bindAddr);

		SSDPNotifyRequest ssdpReq = new SSDPNotifyRequest();
		ssdpReq.setNTS(NTS.BYEBYE);

		// uuid:device-UUID(::upnp:rootdevice)*
		if (isRootDevice() == true) {
			String devNT = getNotifyDeviceNT();
			String devUSN = getNotifyDeviceUSN();
			ssdpReq.setNT(devNT);
			ssdpReq.setUSN(devUSN);
			ssdpSock.post(ssdpReq);
		}

		// uuid:device-UUID::urn:schemas-upnp-org:device:deviceType:v
		String devNT = getNotifyDeviceTypeNT();
		String devUSN = getNotifyDeviceTypeUSN();
		ssdpReq.setNT(devNT);
		ssdpReq.setUSN(devUSN);
		ssdpSock.post(ssdpReq);

		// Thanks for Mikael Hakman (04/25/05)
		ssdpSock.close();

		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			service.byebye(bindAddr);
		}

		DeviceList childDeviceList = getDeviceList();
		int childDeviceCnt = childDeviceList.size();
		for (int n = 0; n < childDeviceCnt; n++) {
			Device childDevice = childDeviceList.getDevice(n);
			childDevice.byebye(bindAddr);
		}
	}

	public void byebye() {

		InetAddress[] binds = getDeviceData().getHTTPBindAddress();
		String[] bindAddresses;
		if (binds != null) {
			bindAddresses = new String[binds.length];
			for (int i = 0; i < binds.length; i++) {
				bindAddresses[i] = binds[i].getHostAddress();
			}
		} else {
			int nHostAddrs = HostInterface.getNHostAddresses();
			bindAddresses = new String[nHostAddrs];
			for (int n = 0; n < nHostAddrs; n++) {
				bindAddresses[n] = HostInterface.getHostAddress(n);
			}
		}

		for (int j = 0; j < bindAddresses.length; j++) {
			if (bindAddresses[j] == null || bindAddresses[j].length() <= 0)
				continue;
			int ssdpCount = getSSDPAnnounceCount();
			for (int i = 0; i < ssdpCount; i++)
				byebye(bindAddresses[j]);
		}
	}

	// //////////////////////////////////////////////
	// Search
	// //////////////////////////////////////////////

	private static Calendar cal = Calendar.getInstance();

	public boolean postSearchResponse(SSDPPacket ssdpPacket, String st,
			String usn) {
		String localAddr = ssdpPacket.getLocalAddress();
		Device rootDev = getRootDevice();
		String rootDevLocation = rootDev.getLocationURL(localAddr);

		SSDPSearchResponse ssdpRes = new SSDPSearchResponse();
		ssdpRes.setLeaseTime(getLeaseTime());
		ssdpRes.setDate(cal);
		ssdpRes.setST(st);
		ssdpRes.setUSN(usn);
		ssdpRes.setLocation(rootDevLocation);
		ssdpRes.setBootId(getBootId());
		// Thanks for Brent Hills (10/20/04)
		ssdpRes.setMYNAME(getFriendlyName());

		int mx = ssdpPacket.getMX();
		TimerUtil.waitRandom(mx * 1000);

		String remoteAddr = ssdpPacket.getRemoteAddress();
		int remotePort = ssdpPacket.getRemotePort();
		SSDPSearchResponseSocket ssdpResSock = new SSDPSearchResponseSocket();
		if (Debug.isOn() == true)
			ssdpRes.print();
		int ssdpCount = getSSDPAnnounceCount();
		for (int i = 0; i < ssdpCount; i++)
			ssdpResSock.post(remoteAddr, remotePort, ssdpRes);

		return true;
	}

	public void deviceSearchResponse(SSDPPacket ssdpPacket) {
		String ssdpST = ssdpPacket.getST();

		if (ssdpST == null)
			return;

		boolean isRootDevice = isRootDevice();

		String devUSN = getUDN();
		if (isRootDevice == true)
			devUSN += "::" + USN.ROOTDEVICE;

		if (ST.isAllDevice(ssdpST) == true) {
			String devNT = getNotifyDeviceNT();
			int repeatCnt = (isRootDevice == true) ? 3 : 2;
			for (int n = 0; n < repeatCnt; n++)
				postSearchResponse(ssdpPacket, devNT, devUSN);
		} else if (ST.isRootDevice(ssdpST) == true) {
			if (isRootDevice == true)
				postSearchResponse(ssdpPacket, ST.ROOT_DEVICE, devUSN);
		} else if (ST.isUUIDDevice(ssdpST) == true) {
			String devUDN = getUDN();
			if (ssdpST.equals(devUDN) == true)
				postSearchResponse(ssdpPacket, devUDN, devUSN);
		} else if (ST.isURNDevice(ssdpST) == true) {
			String devType = getDeviceType();
			if (ssdpST.equals(devType) == true) {
				// Thanks for Mikael Hakman (04/25/05)
				devUSN = getUDN() + "::" + devType;
				postSearchResponse(ssdpPacket, devType, devUSN);
			}
		}

		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++) {
			Service service = serviceList.getService(n);
			service.serviceSearchResponse(ssdpPacket);
		}

		DeviceList childDeviceList = getDeviceList();
		int childDeviceCnt = childDeviceList.size();
		for (int n = 0; n < childDeviceCnt; n++) {
			Device childDevice = childDeviceList.getDevice(n);
			childDevice.deviceSearchResponse(ssdpPacket);
		}
	}

	public void deviceSearchReceived(SSDPPacket ssdpPacket) {
		deviceSearchResponse(ssdpPacket);
	}

	// //////////////////////////////////////////////
	// HTTP Server
	// //////////////////////////////////////////////

	public void setHTTPPort(int port) {
		getDeviceData().setHTTPPort(port);
	}

	public int getHTTPPort() {
		return getDeviceData().getHTTPPort();
	}

	public void setHTTPBindAddress(InetAddress[] inets) {
		this.getDeviceData().setHTTPBindAddress(inets);
	}

	public InetAddress[] getHTTPBindAddress() {
		return this.getDeviceData().getHTTPBindAddress();
	}

	/**
	 * 
	 * @return
	 * @since 1.8
	 */
	public String getSSDPIPv4MulticastAddress() {
		return this.getDeviceData().getMulticastIPv4Address();
	}

	/**
	 * 
	 * @param ip
	 * @since 1.8
	 */
	public void getSSDPIPv4MulticastAddress(String ip) {
		this.getDeviceData().setMulticastIPv4Address(ip);
	}

	/**
	 * 
	 * @return
	 * @since 1.8
	 */
	public String getSSDPIPv6MulticastAddress() {
		return this.getDeviceData().getMulticastIPv6Address();
	}

	/**
	 * 
	 * @param ip
	 * @since 1.8
	 */
	public void getSSDPIPv6MulticastAddress(String ip) {
		this.getDeviceData().setMulticastIPv6Address(ip);
	}

	public void httpRequestRecieved(HTTPRequest httpReq) {
		if (Debug.isOn() == true)
			httpReq.print();

		if (hasPresentationListener() && isPresentationRequest(httpReq)) {
			PresentationListener listener = getPresentationListener();
			listener.httpRequestRecieved(httpReq);
			return;
		}

		if (httpReq.isGetRequest() == true || httpReq.isHeadRequest() == true) {
			httpGetRequestRecieved(httpReq);
			return;
		}
		if (httpReq.isPostRequest() == true) {
			httpPostRequestRecieved(httpReq);
			return;
		}

		if (httpReq.isSubscribeRequest() == true
				|| httpReq.isUnsubscribeRequest() == true) {
			SubscriptionRequest subReq = new SubscriptionRequest(httpReq);
			deviceEventSubscriptionRecieved(subReq);
			return;
		}

		httpReq.returnBadRequest();
	}

	private synchronized byte[] getDescriptionData(String host) {
		if (isNMPRMode() == false)
			updateURLBase(host);
		Node rootNode = getRootNode();
		if (rootNode == null)
			return new byte[0];
		// Thanks for Mikael Hakman (04/25/05)
		String desc = new String();
		desc += UPnP.XML_DECLARATION;
		desc += "\n";
		desc += rootNode.toString();
		return desc.getBytes();
	}

	private void httpGetRequestRecieved(HTTPRequest httpReq) {
		String uri = httpReq.getURI();
		Debug.message("httpGetRequestRecieved = " + uri);
		if (uri == null) {
			httpReq.returnBadRequest();
			return;
		}

		Device embDev;
		Service embService;

		byte fileByte[] = new byte[0];
		String contentType = null;
		String contentLanguage = null;

		if (isDescriptionURI(uri) == true) {
			String localAddr = httpReq.getLocalAddress();
			if ((localAddr == null) || (localAddr.length() <= 0))
				localAddr = HostInterface.getInterface();
			contentType = XML.DEFAULT_CONTENT_TYPE;
			contentLanguage = XML.DEFAULT_CONTENT_LANGUAGE;
			fileByte = getDescriptionData(localAddr);
		} else if ((embDev = getDeviceByDescriptionURI(uri)) != null) {
			String localAddr = httpReq.getLocalAddress();
			contentType = XML.DEFAULT_CONTENT_TYPE;
			contentLanguage = XML.DEFAULT_CONTENT_LANGUAGE;
			fileByte = embDev.getDescriptionData(localAddr);
		} else if ((embService = getServiceBySCPDURL(uri)) != null) {
			contentType = XML.DEFAULT_CONTENT_TYPE;
			contentLanguage = XML.DEFAULT_CONTENT_LANGUAGE;
			fileByte = embService.getSCPDData();
		} else if (isIconBytesURI(uri) == true) {
			Icon devIcon = getIconByURI(uri);
			if (devIcon != null) {
				contentType = devIcon.getMimeType();
				fileByte = devIcon.getBytes();
			}
		} else {
			httpReq.returnBadRequest();
			return;
		}

		HTTPResponse httpRes = new HTTPResponse();
		httpRes.setStatusCode(HTTPStatus.OK);
		if (contentType != null) {
			httpRes.setContentType(contentType);
		}
		if (contentLanguage != null) {
			// FIXME Check ACCEPT-LANGUAGE header in client request, and set a
			// suitable code.
			httpRes.setContentLanguage(contentLanguage);
		}
		httpRes.setContent(fileByte);

		httpReq.post(httpRes);
	}

	private void httpPostRequestRecieved(HTTPRequest httpReq) {
		if (httpReq.isSOAPAction() == true) {
			// SOAPRequest soapReq = new SOAPRequest(httpReq);
			soapActionRecieved(httpReq);
			return;
		}
		httpReq.returnBadRequest();
	}

	// //////////////////////////////////////////////
	// SOAP
	// //////////////////////////////////////////////

	private void soapBadActionRecieved(HTTPRequest soapReq) {
		SOAPResponse soapRes = new SOAPResponse();
		soapRes.setStatusCode(HTTPStatus.BAD_REQUEST);
		soapReq.post(soapRes);
	}

	private void soapActionRecieved(HTTPRequest soapReq) {
		String uri = soapReq.getURI();
		Service ctlService = getServiceByControlURL(uri);
		if (ctlService != null) {
			ActionRequest crlReq = new ActionRequest(soapReq);
			deviceControlRequestRecieved(crlReq, ctlService);
			return;
		}
		soapBadActionRecieved(soapReq);
	}

	// //////////////////////////////////////////////
	// controlAction
	// //////////////////////////////////////////////

	private void deviceControlRequestRecieved(ControlRequest ctlReq,
			Service service) {
		if (ctlReq.isQueryControl() == true)
			deviceQueryControlRecieved(new QueryRequest(ctlReq), service);
		else
			deviceActionControlRecieved(new ActionRequest(ctlReq), service);
	}

	private void invalidActionControlRecieved(ControlRequest ctlReq) {
		ControlResponse actRes = new ActionResponse();
		actRes.setFaultResponse(UPnPStatus.INVALID_ACTION);
		ctlReq.post(actRes);
	}

	private void invalidArgumentsControlRecieved(ControlRequest ctlReq) {
		ControlResponse actRes = new ActionResponse();
		actRes.setFaultResponse(UPnPStatus.INVALID_ARGS);
		ctlReq.post(actRes);
	}

	private void deviceActionControlRecieved(ActionRequest ctlReq,
			Service service) {
		if (Debug.isOn() == true)
			ctlReq.print();

		String actionName = ctlReq.getActionName();
		Action action = service.getAction(actionName);
		if (action == null) {
			invalidActionControlRecieved(ctlReq);
			return;
		}
		ArgumentList actionArgList = action.getArgumentList();
		ArgumentList reqArgList = ctlReq.getArgumentList();
		try {
			actionArgList.setReqArgs(reqArgList);
		} catch (IllegalArgumentException ex) {
			invalidArgumentsControlRecieved(ctlReq);
			return;
		}
		if (action.performActionListener(ctlReq) == false)
			invalidActionControlRecieved(ctlReq);
	}

	private void deviceQueryControlRecieved(QueryRequest ctlReq, Service service) {
		if (Debug.isOn() == true)
			ctlReq.print();
		String varName = ctlReq.getVarName();
		if (service.hasStateVariable(varName) == false) {
			invalidActionControlRecieved(ctlReq);
			return;
		}
		StateVariable stateVar = getStateVariable(varName);
		if (stateVar.performQueryListener(ctlReq) == false)
			invalidActionControlRecieved(ctlReq);
	}

	// //////////////////////////////////////////////
	// eventSubscribe
	// //////////////////////////////////////////////

	private void upnpBadSubscriptionRecieved(SubscriptionRequest subReq,
			int code) {
		SubscriptionResponse subRes = new SubscriptionResponse();
		subRes.setErrorResponse(code);
		subReq.post(subRes);
	}

	private void deviceEventSubscriptionRecieved(SubscriptionRequest subReq) {
		String uri = subReq.getURI();
		Service service = getServiceByEventSubURL(uri);
		if (service == null) {
			subReq.returnBadRequest();
			return;
		}
		if (subReq.hasCallback() == false && subReq.hasSID() == false) {
			upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
			return;
		}

		// UNSUBSCRIBE
		if (subReq.isUnsubscribeRequest() == true) {
			deviceEventUnsubscriptionRecieved(service, subReq);
			return;
		}

		// SUBSCRIBE (NEW)
		if (subReq.hasCallback() == true) {
			deviceEventNewSubscriptionRecieved(service, subReq);
			return;
		}

		// SUBSCRIBE (RENEW)
		if (subReq.hasSID() == true) {
			deviceEventRenewSubscriptionRecieved(service, subReq);
			return;
		}

		upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
	}

	private void deviceEventNewSubscriptionRecieved(Service service,
			SubscriptionRequest subReq) {
		String callback = subReq.getCallback();
		try {
			new URL(callback);
		} catch (Exception e) {
			upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
			return;
		}

		long timeOut = subReq.getTimeout();
		String sid = Subscription.createSID();

		Subscriber sub = new Subscriber();
		sub.setDeliveryURL(callback);
		sub.setTimeOut(timeOut);
		sub.setSID(sid);
		service.addSubscriber(sub);

		SubscriptionResponse subRes = new SubscriptionResponse();
		subRes.setStatusCode(HTTPStatus.OK);
		subRes.setSID(sid);
		subRes.setTimeout(timeOut);
		if (Debug.isOn() == true)
			subRes.print();
		subReq.post(subRes);

		if (Debug.isOn() == true)
			subRes.print();

		service.notifyAllStateVariables();
	}

	private void deviceEventRenewSubscriptionRecieved(Service service,
			SubscriptionRequest subReq) {
		String sid = subReq.getSID();
		Subscriber sub = service.getSubscriber(sid);

		if (sub == null) {
			upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
			return;
		}

		long timeOut = subReq.getTimeout();
		sub.setTimeOut(timeOut);
		sub.renew();

		SubscriptionResponse subRes = new SubscriptionResponse();
		subRes.setStatusCode(HTTPStatus.OK);
		subRes.setSID(sid);
		subRes.setTimeout(timeOut);
		subReq.post(subRes);

		if (Debug.isOn() == true)
			subRes.print();
	}

	private void deviceEventUnsubscriptionRecieved(Service service,
			SubscriptionRequest subReq) {
		String sid = subReq.getSID();
		Subscriber sub = service.getSubscriber(sid);

		if (sub == null) {
			upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
			return;
		}

		service.removeSubscriber(sub);

		SubscriptionResponse subRes = new SubscriptionResponse();
		subRes.setStatusCode(HTTPStatus.OK);
		subReq.post(subRes);

		if (Debug.isOn() == true)
			subRes.print();
	}

	// //////////////////////////////////////////////
	// Thread
	// //////////////////////////////////////////////

	private HTTPServerList getHTTPServerList() {
		return getDeviceData().getHTTPServerList();
	}

	/**
	 * 
	 * @param port
	 *            The port to use for binding the SSDP service
	 */
	public void setSSDPPort(int port) {
		this.getDeviceData().setSSDPPort(port);
	}

	/**
	 * 
	 * @return The port to use for binding the SSDP service
	 */
	public int getSSDPPort() {
		return this.getDeviceData().getSSDPPort();
	}

	/**
	 * 
	 * @param inets
	 *            The IP that will be used for binding the SSDP service. Use
	 *            <code>null</code> to get the default beahvior
	 */
	public void setSSDPBindAddress(InetAddress[] inets) {
		this.getDeviceData().setSSDPBindAddress(inets);
	}

	/**
	 * 
	 * @return inets The IP that will be used for binding the SSDP service. null
	 *         means the default setted by the class UPnP
	 */
	public InetAddress[] getSSDPBindAddress() {
		return this.getDeviceData().getSSDPBindAddress();
	}

	/**
	 * 
	 * @param ip
	 *            The IPv4 address used for Multicast comunication
	 */
	public void setMulticastIPv4Address(String ip) {
		this.getDeviceData().setMulticastIPv4Address(ip);
	}

	/**
	 * 
	 * @return The IPv4 address used for Multicast comunication
	 */
	public String getMulticastIPv4Address() {
		return this.getDeviceData().getMulticastIPv4Address();
	}

	/**
	 * 
	 * @param ip
	 *            The IPv address used for Multicast comunication
	 */
	public void setMulticastIPv6Address(String ip) {
		this.getDeviceData().setMulticastIPv6Address(ip);
	}

	/**
	 * 
	 * @return The IPv address used for Multicast comunication
	 */
	public String getMulticastIPv6Address() {
		return this.getDeviceData().getMulticastIPv6Address();
	}

	private SSDPSearchSocketList getSSDPSearchSocketList() {
		return getDeviceData().getSSDPSearchSocketList();
	}

	private void setAdvertiser(Advertiser adv) {
		getDeviceData().setAdvertiser(adv);
	}

	private Advertiser getAdvertiser() {
		return getDeviceData().getAdvertiser();
	}

	public boolean start() {
		stop(true);

		// //////////////////////////////////////
		// HTTP Server
		// //////////////////////////////////////

		int retryCnt = 0;
		int bindPort = getHTTPPort();
		HTTPServerList httpServerList = getHTTPServerList();
		while (httpServerList.open(bindPort) == false) {
			retryCnt++;
			if (UPnP.SERVER_RETRY_COUNT < retryCnt)
				return false;
			setHTTPPort(bindPort + 1);
			bindPort = getHTTPPort();
		}
		httpServerList.addRequestListener(this);
		httpServerList.start();

		// //////////////////////////////////////
		// SSDP Seach Socket
		// //////////////////////////////////////

		SSDPSearchSocketList ssdpSearchSockList = getSSDPSearchSocketList();
		if (ssdpSearchSockList.open() == false)
			return false;
		ssdpSearchSockList.addSearchListener(this);
		ssdpSearchSockList.start();

		// //////////////////////////////////////
		// BOOTID/CONFIGID.UPNP.ORG
		// //////////////////////////////////////

		updateBootId();
		updateConfigId();

		// //////////////////////////////////////
		// Announce
		// //////////////////////////////////////

		announce();

		// //////////////////////////////////////
		// Advertiser
		// //////////////////////////////////////

		Advertiser adv = new Advertiser(this);
		setAdvertiser(adv);
		adv.start();

		return true;
	}

	private boolean stop(boolean doByeBye) {
		if (doByeBye == true)
			byebye();

		HTTPServerList httpServerList = getHTTPServerList();
		httpServerList.stop();
		httpServerList.close();
		httpServerList.clear();

		SSDPSearchSocketList ssdpSearchSockList = getSSDPSearchSocketList();
		ssdpSearchSockList.stop();
		ssdpSearchSockList.close();
		ssdpSearchSockList.clear();

		Advertiser adv = getAdvertiser();
		if (adv != null) {
			adv.stop();
			setAdvertiser(null);
		}

		return true;
	}

	public boolean stop() {
		return stop(true);
	}

	public boolean isRunning() {
		return (getAdvertiser() != null) ? true : false;
	}

	// //////////////////////////////////////////////
	// Interface Address
	// //////////////////////////////////////////////

	public String getInterfaceAddress() {
		SSDPPacket ssdpPacket = getSSDPPacket();
		if (ssdpPacket == null)
			return "";
		return ssdpPacket.getLocalAddress();
	}

	// //////////////////////////////////////////////
	// Acion/QueryListener
	// //////////////////////////////////////////////

	public void setActionListener(ActionListener listener) {
		ServiceList serviceList = getServiceList();
		int nServices = serviceList.size();
		for (int n = 0; n < nServices; n++) {
			Service service = serviceList.getService(n);
			service.setActionListener(listener);
		}
	}

	public void setQueryListener(QueryListener listener) {
		ServiceList serviceList = getServiceList();
		int nServices = serviceList.size();
		for (int n = 0; n < nServices; n++) {
			Service service = serviceList.getService(n);
			service.setQueryListener(listener);
		}
	}

	// //////////////////////////////////////////////
	// Acion/QueryListener (includeSubDevices)
	// //////////////////////////////////////////////

	// Thanks for Mikael Hakman (04/25/05)
	public void setActionListener(ActionListener listener,
			boolean includeSubDevices) {
		setActionListener(listener);
		if (includeSubDevices == true) {
			DeviceList devList = getDeviceList();
			int devCnt = devList.size();
			for (int n = 0; n < devCnt; n++) {
				Device dev = devList.getDevice(n);
				dev.setActionListener(listener, true);
			}
		}
	}

	// Thanks for Mikael Hakman (04/25/05)
	public void setQueryListener(QueryListener listener,
			boolean includeSubDevices) {
		setQueryListener(listener);
		if (includeSubDevices == true) {
			DeviceList devList = getDeviceList();
			int devCnt = devList.size();
			for (int n = 0; n < devCnt; n++) {
				Device dev = devList.getDevice(n);
				dev.setQueryListener(listener, true);
			}
		}
	}

	// //////////////////////////////////////////////
	// userData
	// //////////////////////////////////////////////

	private Object userData = null;

	public void setUserData(Object data) {
		userData = data;
	}

	public Object getUserData() {
		return userData;
	}

	// //////////////////////////////////////////////
	// output
	// //////////////////////////////////////////////

	/*
	 * public void output(PrintWriter ps) { ps.println("deviceType = " +
	 * getDeviceType()); ps.println("freindlyName = " + getFriendlyName());
	 * ps.println("presentationURL = " + getPresentationURL());
	 * 
	 * DeviceList devList = getDeviceList(); ps.println("devList = " +
	 * devList.size());
	 * 
	 * ServiceList serviceList = getServiceList(); ps.println("serviceList = " +
	 * serviceList.size());
	 * 
	 * IconList iconList = getIconList(); ps.println("iconList = " +
	 * iconList.size()); }
	 * 
	 * public void print() { PrintWriter pr = new PrintWriter(System.out);
	 * output(pr); pr.flush(); }
	 */

}
