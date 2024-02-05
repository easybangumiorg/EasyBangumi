/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: HTTPSocket.java
*
*	Revision;
*
*	12/12/02
*		- first revision.
*	03/11/04
*		- Added the following methods about chunk size.
*		  setChunkSize(), getChunkSize().
*	08/26/04
*		- Added a isOnlyHeader to post().
*	03/02/05
*		- Changed post() to suppot chunked stream.
*	06/10/05
*		- Changed post() to add a Date headedr to the HTTPResponse before the posting.
*	07/07/05
*		- Lee Peik Feng <pflee@users.sourceforge.net>
*		- Fixed post() to output the chunk size as a hex string.
*	
******************************************************************/

package org.cybergarage.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

public class HTTPSocket
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public HTTPSocket(Socket socket)
	{
		setSocket(socket);
		open();
	}

	public HTTPSocket(HTTPSocket socket)
	{
		setSocket(socket.getSocket());
		setInputStream(socket.getInputStream());
		setOutputStream(socket.getOutputStream());
	}
	
	public void finalize()
	{
		close();
	}
	
	////////////////////////////////////////////////
	//	Socket
	////////////////////////////////////////////////

	private Socket socket = null;

	private void setSocket(Socket socket)
	{
		this.socket = socket;
	}

	public Socket getSocket()
	{
		return socket;
	}

	////////////////////////////////////////////////
	//	local address/port
	////////////////////////////////////////////////
	
	public String getLocalAddress()
	{
		return getSocket().getLocalAddress().getHostAddress();	
	}

	public int getLocalPort()
	{
		return getSocket().getLocalPort();	
	}

	////////////////////////////////////////////////
	//	in/out
	////////////////////////////////////////////////

	private InputStream sockIn = null;
	private OutputStream sockOut = null;

	private void setInputStream(InputStream in)
	{
		sockIn = in;
	}
	
	public InputStream getInputStream()
	{
		return sockIn;
	}

	private void setOutputStream(OutputStream out)
	{
		sockOut = out;
	}
	
	private OutputStream getOutputStream()
	{
		return sockOut;
	}

	////////////////////////////////////////////////
	//	open/close
	////////////////////////////////////////////////

	public boolean open()
	{
		Socket sock = getSocket();
 		try {
			sockIn = sock.getInputStream();
			sockOut = sock.getOutputStream();
		}
		catch (Exception e) {
			//TODO Add blacklistening of the UPnP Device
			return false;
		}
		return true;
	}

	public boolean close()
	{
 		try {
 			if (sockIn != null)
				sockIn.close();
			if (sockOut != null)
				sockOut.close();
			getSocket().close();
		}
		catch (Exception e) {
			//Debug.warning(e);
			return false;
		}
		return true;
	}
	
	////////////////////////////////////////////////
	//	post
	////////////////////////////////////////////////

	private boolean post(HTTPResponse httpRes, byte content[], long contentOffset, long contentLength, boolean isOnlyHeader)
	{
		//TODO Check for bad HTTP agents, this method may be list for IOInteruptedException and for blacklistening
		httpRes.setDate(Calendar.getInstance());
		
		OutputStream out = getOutputStream();

		try {
			httpRes.setContentLength(contentLength);
			
			out.write(httpRes.getHeader().getBytes());
			out.write(HTTP.CRLF.getBytes());
			if (isOnlyHeader == true) {
				out.flush();
				return true;
			}
			
			boolean isChunkedResponse = httpRes.isChunked();
			
			if (isChunkedResponse == true) {
				// Thanks for Lee Peik Feng <pflee@users.sourceforge.net> (07/07/05)
				String chunSizeBuf = Long.toHexString(contentLength);
				out.write(chunSizeBuf.getBytes());
				out.write(HTTP.CRLF.getBytes());
			}
			
			out.write(content, (int)contentOffset, (int)contentLength);
			
			if (isChunkedResponse == true) {
				out.write(HTTP.CRLF.getBytes());
				out.write("0".getBytes());
				out.write(HTTP.CRLF.getBytes());
			}
			
			out.flush();
		}
		catch (Exception e) {
			//Debug.warning(e);
			return false;
		}
		
		return true;
	}
	
	private boolean post(HTTPResponse httpRes, InputStream in, long contentOffset, long contentLength, boolean isOnlyHeader)
	{
		//TODO Check for bad HTTP agents, this method may be list for IOInteruptedException and for blacklistening
		httpRes.setDate(Calendar.getInstance());
		
		OutputStream out = getOutputStream();

		try {
			httpRes.setContentLength(contentLength);
			
			out.write(httpRes.getHeader().getBytes());
			out.write(HTTP.CRLF.getBytes());
			
			if (isOnlyHeader == true) {
				out.flush();
				return true;
			}
			
			boolean isChunkedResponse = httpRes.isChunked();
			
			if (0 < contentOffset)
				in.skip(contentOffset);
			
			int chunkSize = HTTP.getChunkSize();
			byte readBuf[] = new byte[chunkSize];
			long readCnt = 0;
			long readSize = (chunkSize < contentLength) ? chunkSize : contentLength;
			int readLen = in.read(readBuf, 0, (int)readSize);
			while (0 < readLen && readCnt < contentLength) {
				if (isChunkedResponse == true) {
					// Thanks for Lee Peik Feng <pflee@users.sourceforge.net> (07/07/05)
					String chunSizeBuf = Long.toHexString(readLen);
					out.write(chunSizeBuf.getBytes());
					out.write(HTTP.CRLF.getBytes());
				}
				out.write(readBuf, 0, readLen);
				if (isChunkedResponse == true)
					out.write(HTTP.CRLF.getBytes());
				readCnt += readLen;
				readSize = (chunkSize < (contentLength-readCnt)) ? chunkSize : (contentLength-readCnt);
				readLen = in.read(readBuf, 0, (int)readSize);
			}
			
			if (isChunkedResponse == true) {
				out.write("0".getBytes());
				out.write(HTTP.CRLF.getBytes());
			}
			
			out.flush();
		}
		catch (Exception e) {
			//Debug.warning(e);
			return false;
		}
		
		return true;
	}
	
	public boolean post(HTTPResponse httpRes, long contentOffset, long contentLength, boolean isOnlyHeader)
	{
		//TODO Close if Connection != keep-alive
		if (httpRes.hasContentInputStream() == true)
			return post(httpRes,httpRes.getContentInputStream(), contentOffset, contentLength, isOnlyHeader);
		return post(httpRes,httpRes.getContent(), contentOffset, contentLength, isOnlyHeader);
	}
}
