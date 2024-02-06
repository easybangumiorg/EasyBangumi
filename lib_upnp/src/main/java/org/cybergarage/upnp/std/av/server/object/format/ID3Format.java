/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : ID3.java
*
*	Revision:
*
*	12/03/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.format;

import java.io.*;

import org.cybergarage.upnp.std.av.server.object.*;
import org.cybergarage.upnp.std.av.server.object.item.*;
import org.cybergarage.xml.*;
import org.cybergarage.util.*;

public class ID3Format extends Header implements Format, FormatObject
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	
	public final static String HEADER_ID = "ID3";
	public final static int HEADER_SIZE = 10;
	public final static int FRAME_HEADER_SIZE = 10;
	
	////////////////////////////////////////////////
	// Member
	////////////////////////////////////////////////
	
	private byte header[] = new byte[HEADER_SIZE];
	private byte extHeader[] = new byte[4];
	private byte frameHeader[] = new byte[FRAME_HEADER_SIZE];
	private ID3FrameList frameList = new ID3FrameList();
	
	private File mp3File;
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public ID3Format()
	{
		mp3File = null;
	}
	
	public ID3Format(File file)
	{
		mp3File = file;
		loadHeader(file);
	}

	////////////////////////////////////////////////
	// loadHeader
	////////////////////////////////////////////////

	public boolean loadHeader(InputStream inputStream)
	{
		try {
			// Reading a main header
			DataInputStream dataIn = new DataInputStream(inputStream);
			for (int n=0; n<HEADER_SIZE; n++)
				header[n] = dataIn.readByte();
				
			// Reading a extended header
			if (hasExtendedHeader() == true) {
				for (int n=0; n<4; n++)
					header[n] = dataIn.readByte();
				int extHeaderSize = getExtHeaderSize();
				// Ignoring extended header infos
				for (int n=0; n<(extHeaderSize-4); n++) 
					dataIn.readByte();
			}
			
			// Reading frame infos
			frameList.clear();
			int frameDataSize = getHeaderSize() - HEADER_SIZE;
			if (hasExtendedHeader() == true)
				frameDataSize -= getExtHeaderSize();
			int frameDataCnt = 0;
			while (frameDataCnt < frameDataSize) {
				for (int n=0; n<FRAME_HEADER_SIZE; n++)
					frameHeader[n] = dataIn.readByte();
				String frameID = getFrameID(frameHeader);
				int frameSize = getFrameSize(frameHeader);
				int frameFlag = getFrameFlag(frameHeader);
				byte frameData[] = new byte[frameSize];
				for (int i=0; i<frameSize; i++)
					frameData[i] = dataIn.readByte();
				ID3Frame frame = new ID3Frame();
				frame.setID(frameID);
				frame.setSize(frameSize);
				frame.setFlag(frameFlag);
				frame.setData(frameData);
				frameList.add(frame);
				frameDataCnt += frameSize + FRAME_HEADER_SIZE;
			}
			
			dataIn.close();
		}
		catch (EOFException eofe) {
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		return true;
	}
	
	public boolean loadHeader(File file)
	{
		try {
			return loadHeader(new FileInputStream(file));
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
	}

	public boolean hasHeader()
	{
		String id = getHeaderID();
		if (id == null)
			return false;
		return id.equals(HEADER_ID);
	}
	
	////////////////////////////////////////////////
	// Header
	////////////////////////////////////////////////
	
	public String getHeaderID()
	{
		return new String(header, 0, 3);
	}

	public int getHeaderSize()
	{
		int size = 0;
		for (int n=0; n<4; n++)
			size += (header[9-n] & 0xFF ) << n;		
		return size;
	}

	public int getFlag()
	{
		return (header[5] & 0xFF);		
	}
	
	public boolean isUnsynchronisation()
	{
		return (getFlag() & 0x80) == 1 ? true : false;
	}

	public boolean hasExtendedHeader()
	{
		return (getFlag() & 0x40) == 1 ? true : false;
	}

	public boolean isExperimental()
	{
		return (getFlag() & 0x20) == 1 ? true : false;
	}

	public boolean hasFooter()
	{
		return (getFlag() & 0x10) == 1 ? true : false;
	}

	////////////////////////////////////////////////
	// Extended Header
	////////////////////////////////////////////////
	
	public int getExtHeaderSize()
	{
		int size = 0;
		for (int n=0; n<4; n++)
			size += (extHeader[3-n] & 0xFF ) << n;		
		return size;
	}
	
	////////////////////////////////////////////////
	// Header
	////////////////////////////////////////////////
	
	private String getFrameID(byte frameHeader[])
	{
		return new String(frameHeader, 0, 4);
	}

	private int getFrameSize(byte frameHeader[])
	{
		int size = 0;
		for (int n=0; n<4; n++)
			size += (frameHeader[7-n] & 0xFF ) << n;		
		return size;
	}

	private int getFrameFlag(byte frameHeader[])
	{
		return ((frameHeader[8] & 0xFF) << 8) + (frameHeader[9] & 0xFF);		
	}

	public byte[] getFrameData(String name)
	{
		return frameList.getFrameData(name);
	}

	public String getFrameStringData(String name)
	{
		return frameList.getFrameStringData(name);
	}
	
	////////////////////////////////////////////////
	// Abstract Methods
	////////////////////////////////////////////////
	
	public boolean equals(File file)
	{
		String headerID = Header.getIDString(file, 3);
		if (headerID.startsWith(HEADER_ID) == true)
			return true;		
		return false;
	}
	
	public FormatObject createObject(File file)
	{
		return new ID3Format(file);
	}
	
	public String getMimeType()
	{
		return "audio/mpeg";
	}

	public String getMediaClass()
	{
		return "object.item.audioItem.musicTrack";
	}
	
	public AttributeList getAttributeList()
	{
		AttributeList attrList = new AttributeList();
		
		// Size 
		long fsize = mp3File.length();
		Attribute sizeStr = new Attribute(ItemNode.SIZE, Long.toString(fsize));
		attrList.add(sizeStr);
		
		return attrList;	
	}
	
	public String getTitle()
	{
		String title = getFrameStringData(ID3Frame.TIT2);
		if (0 < title.length())
			return title;
		title = getFrameStringData(ID3Frame.TIT1);
		if (0 < title.length())
			return title;
		return getFrameStringData(ID3Frame.TIT2);
	}
	
	public String getCreator()
	{
		String creator = getFrameStringData(ID3Frame.TPE1);
		if (0 < creator.length())
			return creator;
		creator = getFrameStringData(ID3Frame.TPE2);
		if (0 < creator.length())
			return creator;
		creator = getFrameStringData(ID3Frame.TPE3);
		if (0 < creator.length())
			return creator;
		return getFrameStringData(ID3Frame.TPE4);
	}
	
	////////////////////////////////////////////////
	// print
	////////////////////////////////////////////////
	
	public void print()
	{
		String headerStr = new String(header);
		System.out.println("header = " + headerStr);
		System.out.println("ID = " + getHeaderID());
		System.out.println("Size = " + getHeaderSize());
		System.out.println("isUnsynchronisation = " + isUnsynchronisation());
		System.out.println("hasExtendedHeader = " + hasExtendedHeader());
		System.out.println("isExperimental = " + isExperimental());
		System.out.println("hasFooter = " + hasFooter());
		int frameCnt = frameList.size();
		for (int n=0; n<frameCnt; n++) {
			ID3Frame frame = frameList.getFrame(n);
			System.out.println("[" + n + "] : " + frame.getID());
			System.out.println("     " + frame.getData());
		}
	}

}

