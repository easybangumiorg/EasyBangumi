/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: Icon.java
*
*	Revision;
*
*	11/28/02
*		- first revision.
*	04/12/06
*		- Added setUserData() and getUserData() to set a user original data object.
*	
******************************************************************/

package org.cybergarage.upnp;

import org.cybergarage.xml.Node;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Icon
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public final static String ELEM_NAME = "icon";

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private Node iconNode;

	public Node getIconNode()
	{
		return iconNode;
	}
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public Icon(Node node)
	{
		iconNode = node;
	}

	public Icon() {
		this(new Node(ELEM_NAME));
	}
	
	////////////////////////////////////////////////
	//	isIconNode
	////////////////////////////////////////////////

	public static boolean isIconNode(Node node)
	{
		return Icon.ELEM_NAME.equals(node.getName());
	}

	////////////////////////////////////////////////
	//	mimeType
	////////////////////////////////////////////////

	private final static String MIME_TYPE = "mimetype";
	
	public void setMimeType(String value)
	{
		getIconNode().setNode(MIME_TYPE, value);
	}

	public String getMimeType()
	{
		return getIconNode().getNodeValue(MIME_TYPE);
	}

	public boolean hasMimeType()
	{
		String iconMimeType = getMimeType();
		if (iconMimeType == null)
			return false;
		return (0 < iconMimeType.length()) ? true : false;
	}
	
	////////////////////////////////////////////////
	//	width
	////////////////////////////////////////////////

	private final static String WIDTH = "width";
	
	public void setWidth(String value)
	{
		getIconNode().setNode(WIDTH, value);
	}

	public void setWidth(int value)
	{
		try {
			setWidth(Integer.toString(value));
		}
		catch (Exception e) {};
	}
	
	public int getWidth()
	{
		try {
			return Integer.parseInt(getIconNode().getNodeValue(WIDTH));
		}
		catch (Exception e) {};
		return 0;
	}

	////////////////////////////////////////////////
	//	height
	////////////////////////////////////////////////

	private final static String HEIGHT = "height";
	
	public void setHeight(String value)
	{
		getIconNode().setNode(HEIGHT, value);
	}

	public void setHeight(int value)
	{
		try {
			setHeight(Integer.toString(value));
		}
		catch (Exception e) {};
	}
	
	public int getHeight()
	{
		try {
			return Integer.parseInt(getIconNode().getNodeValue(HEIGHT));
		}
		catch (Exception e) {};
		return 0;
	}

	////////////////////////////////////////////////
	//	depth
	////////////////////////////////////////////////

	private final static String DEPTH = "depth";
	
	public void setDepth(String value)
	{
		getIconNode().setNode(DEPTH, value);
	}

	public void setDepth(int value)
	{
		try {
			setDepth(Integer.toString(value));
		}
		catch (Exception e) {};
	}
	
	public int getDepth()
	{
		try {
			return Integer.parseInt(getIconNode().getNodeValue(DEPTH));
		}
		catch (Exception e) {};
		return 0;
	}

	////////////////////////////////////////////////
	//	URL
	////////////////////////////////////////////////

	private final static String URL = "url";
	
	public void setURL(String value)
	{
		getIconNode().setNode(URL, value);
	}

	public String getURL()
	{
		return getIconNode().getNodeValue(URL);
	}

	public boolean hasURL()
	{
		String iconURL = getURL();
		if (iconURL == null)
			return false;
		return (0 < iconURL.length()) ? true : false;
	}
	
	public boolean isURL(String url)
	{
		if (url == null)
			return false;
		String iconURL = getURL();
		if (iconURL == null)
			return false;
		return iconURL.equals(url);
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

	////////////////////////////////////////////////
	//	Bytes
	////////////////////////////////////////////////

	private byte bytes[] = null; 
	
	public void setBytes(byte data[]) 
	{
		bytes = data;
	}

	public boolean hasBytes() 
	{
        if(bytes != null)
            return true;
        if(hasURL())
            return Icon.class.getResourceAsStream(getURL()) != null;
        else
            return false;
	}
	
	public byte[]getBytes() 
	{
        if (bytes == null && hasURL())
        {
            try
            {
                InputStream inStream = Icon.class.getResourceAsStream(getURL());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int data = -1;
                while ((data = inStream.read()) != -1)
                {
                    baos.write(data);
                }
                inStream.close();
                bytes = baos.toByteArray();
            }
            catch (Exception e)
            {
            }
        }
		return bytes;
	}
}
