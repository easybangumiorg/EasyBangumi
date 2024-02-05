/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: HTTPServer.java
*
*	Revision;
*
*	12/12/02
*		- first revision.
*	10/20/03
*		- Improved the HTTP server using multithreading.
*	08/27/04
*		- Changed accept() to set a default timeout, HTTP.DEFAULT_TIMEOUT, to the socket.
*	
******************************************************************/

package org.cybergarage.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.cybergarage.util.Debug;
import org.cybergarage.util.ListenerList;

/**
 * 
 * This class identifies an HTTP over TCP server<br>
 * The server must be initialized iether by the {@link HTTPServer#open(InetAddress, int)} or the {@link HTTPServer#open(String, int)} method.<br>
 * Optionally a set of {@link HTTPRequestListener} may be set<br>
 * The server then can be started or stopped by the method {@link HTTPServer#start()} and {@link HTTPServer#stop()}
 * 
 * @author Satoshi "skonno" Konno
 * @author Stefano "Kismet" Lenzi
 * @version 1.8
 *
 */
public class HTTPServer implements Runnable
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////

	public final static String NAME = "CyberHTTP";
	public final static String VERSION = "1.0";

	public final static int DEFAULT_PORT = 80;
	
	/**
	 * Default timeout connection for HTTP comunication
	 * @since 1.8
	 */
	public final static int DEFAULT_TIMEOUT = DEFAULT_PORT * 1000;
	
	public static String getName()
	{
		String osName = System.getProperty("os.name");
		String osVer = System.getProperty("os.version");
		return osName + "/"  + osVer + " " + NAME + "/" + VERSION;
	}
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public HTTPServer()
	{
		serverSock = null;
		
	}

	////////////////////////////////////////////////
	//	ServerSocket
	////////////////////////////////////////////////

	private ServerSocket serverSock = null;
	private InetAddress bindAddr = null;
	private int bindPort = 0;
	/**
	 * Store the current TCP timeout value
	 * The variable should be accessed by getter and setter metho
	 */
	protected int timeout = DEFAULT_TIMEOUT;
	
	public ServerSocket getServerSock()
	{
		return serverSock;
	}

	public String getBindAddress()
	{
			if (bindAddr == null)
				return "";
			return bindAddr.toString();
	}

	public int getBindPort()
	{
		return bindPort;
	}
	
	
	
	////////////////////////////////////////////////
	//	open/close
	////////////////////////////////////////////////
	
	/**
	 * Get the current socket timeout
	 * @since 1.8
	 */
	public synchronized int getTimeout() {
		return timeout;
	}

	/**
	 * Set the current socket timeout
	 * @param longout new timeout
	 * @since 1.8
	 */
	public synchronized void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean open(InetAddress addr,int port){
		if (serverSock != null)
			return true;
		try {
			serverSock = new ServerSocket(bindPort, 0, bindAddr);
		}catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public boolean open(String addr, int port)
	{
		if (serverSock != null)
			return true;
		try {
			bindAddr = InetAddress.getByName(addr);
			bindPort = port;
			serverSock = new ServerSocket(bindPort, 0, bindAddr);
		}
		catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean close()
	{
		if (serverSock == null)
			return true;
		try {
			serverSock.close();
			serverSock = null;
			bindAddr = null;
			bindPort = 0;
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		return true;
	}

	public Socket accept()
	{
		if (serverSock == null)
			return null;
		try {
			Socket sock = serverSock.accept();
			sock.setSoTimeout(getTimeout());
			return sock;
		}
		catch (Exception e) {
			return null;
		}
	}

	public boolean isOpened()
	{
		return (serverSock != null) ? true : false;
	}

	////////////////////////////////////////////////
	//	httpRequest
	////////////////////////////////////////////////

	private ListenerList httpRequestListenerList = new ListenerList();
	 	
	public void addRequestListener(HTTPRequestListener listener)
	{
		httpRequestListenerList.add(listener);
	}		

	public void removeRequestListener(HTTPRequestListener listener)
	{
		httpRequestListenerList.remove(listener);
	}		

	public void performRequestListener(HTTPRequest httpReq)
	{
		int listenerSize = httpRequestListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			HTTPRequestListener listener = (HTTPRequestListener)httpRequestListenerList.get(n);
			listener.httpRequestRecieved(httpReq);
		}
	}		
	
	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////

	private Thread httpServerThread = null;
		
	public void run()
	{
		if (isOpened() == false)
			return;
			
		Thread thisThread = Thread.currentThread();
		
		while (httpServerThread == thisThread) {
			Thread.yield();
			Socket sock;
			try {
				Debug.message("accept ...");
				sock = accept();
				if (sock != null)
					Debug.message("sock = " + sock.getRemoteSocketAddress());
			}
			catch (Exception e){
				Debug.warning(e);
				break;
			}
			HTTPServerThread httpServThread = new HTTPServerThread(this, sock);
			httpServThread.start(); 
			Debug.message("httpServThread ...");
		}
	}
	
	public boolean start(){
		StringBuffer name = new StringBuffer("Cyber.HTTPServer/");
		name.append(serverSock.getLocalSocketAddress());
		httpServerThread = new Thread(this,name.toString());
		httpServerThread.start();
		return true;
	}
	
	public boolean stop()
	{
		httpServerThread = null;
		return true;
	}
}
