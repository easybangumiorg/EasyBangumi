/******************************************************************
*
*	CyberLink for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: HTTPMU.java
*
*	Revision;
*
*	11/20/02
*		- first revision.
*	12/12/03
*		- Inma Mar?n <inma@DIF.UM.ES>
*		- Changed open(addr, port) to send IPv6 SSDP packets.
*		- The socket binds only the port without the interface address.
*		- The full binding socket can send SSDP IPv4 packets. Is it a bug of J2SE v.1.4.2-b28 ?.
*	01/06/04
*		- Oliver Newell <olivern@users.sourceforge.net>
*		- Added to set a current timestamp when the packet are received.
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.cybergarage.util.Debug;

public class HTTPUSocket
{
	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private DatagramSocket ssdpUniSock = null;
	//private MulticastSocket ssdpUniSock = null;

	public DatagramSocket getDatagramSocket()
	{
		return ssdpUniSock;
	}
		
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public HTTPUSocket()
	{
		open();
	}
	
	public HTTPUSocket(String bindAddr, int bindPort) throws BindException
	{
		open(bindAddr, bindPort);
	}

	public HTTPUSocket(int bindPort)
	{
		open(bindPort);
	}

	protected void finalize()
	{
		close();
	}

	////////////////////////////////////////////////
	//	bindAddr
	////////////////////////////////////////////////

	private String localAddr = "";

	public void setLocalAddress(String addr)
	{
		localAddr = addr;
	}

	/**
	 * 
	 * @return {@link DatagramSocket} open for receieving packets
	 * @since 1.8
	 */
	public DatagramSocket getUDPSocket(){
		return ssdpUniSock;
	}	
	
	public String getLocalAddress()
	{
		if (0 < localAddr.length())
			return localAddr;
		return ssdpUniSock.getLocalAddress().getHostAddress();
	}

	////////////////////////////////////////////////
	//	open
	////////////////////////////////////////////////
	
	public boolean open()
	{
		close();
		
		try {
			ssdpUniSock = new DatagramSocket();
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		
		return true;
	}
	
	public boolean open(String bindAddr, int bindPort) throws BindException
	{
		close();
		
		try {
			// Changed to bind the specified address and port for Android v1.6 (2009/10/07)
			InetSocketAddress bindInetAddr = new InetSocketAddress(InetAddress.getByName(bindAddr), bindPort);
			ssdpUniSock = new DatagramSocket(bindInetAddr);
		}
        catch (BindException possible) {
            Debug.warning(possible);
            throw possible;
        }
        catch (Exception e) {
            Debug.warning(e);
            return false;
        }

		/*
		try {
			// Bind only using the port without the interface address. (2003/12/12)
			InetSocketAddress bindInetAddr = new InetSocketAddress(bindPort);
			ssdpUniSock = new DatagramSocket(null);
			ssdpUniSock.setReuseAddress(true);
			ssdpUniSock.bind(bindInetAddr);
			return true;
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		*/
		
		setLocalAddress(bindAddr);
		
		return true;
	}

	public boolean open(int bindPort)
	{
		close();
		
		try {
			InetSocketAddress bindSock = new InetSocketAddress(bindPort);
			ssdpUniSock = new DatagramSocket(null);
			ssdpUniSock.setReuseAddress(true);
			ssdpUniSock.bind(bindSock);
		}
		catch (Exception e) {
			//Debug.warning(e);
			return false;
		}
		
		return true;
	}
		
	////////////////////////////////////////////////
	//	close
	////////////////////////////////////////////////

	public boolean close()
	{
		if (ssdpUniSock == null)
			return true;
			
		try {
			ssdpUniSock.close();
			ssdpUniSock = null;
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		
		return true;
	}

	////////////////////////////////////////////////
	//	send
	////////////////////////////////////////////////

	public boolean post(String addr, int port, String msg)
	{
		try {
			InetAddress inetAddr = InetAddress.getByName(addr);
			DatagramPacket dgmPacket = new DatagramPacket(msg.getBytes(), msg.length(), inetAddr, port);
			ssdpUniSock.send(dgmPacket);
		}
		catch (Exception e) {
			Debug.warning(e);
			if (ssdpUniSock != null) {
				Debug.warning("addr = " + ssdpUniSock.getLocalAddress().getHostName());
				Debug.warning("port = " + ssdpUniSock.getLocalPort());
			}
			return false;
		}
		return true;
	}

	////////////////////////////////////////////////
	//	reveive
	////////////////////////////////////////////////

	public SSDPPacket receive()
	{
		byte ssdvRecvBuf[] = new byte[SSDP.RECV_MESSAGE_BUFSIZE];
 		SSDPPacket recvPacket = new SSDPPacket(ssdvRecvBuf, ssdvRecvBuf.length);
		recvPacket.setLocalAddress(getLocalAddress());
		try {
	 		ssdpUniSock.receive(recvPacket.getDatagramPacket());
			recvPacket.setTimeStamp(System.currentTimeMillis());
		}
		catch (Exception e) {
			//Debug.warning(e);
			return null;
		}
 		return recvPacket;
	}

	////////////////////////////////////////////////
	//	join/leave
	////////////////////////////////////////////////

/*
	boolean joinGroup(String mcastAddr, int mcastPort, String bindAddr)
	{
		try {	 	
			InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
			NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
			ssdpUniSock.joinGroup(mcastGroup, mcastIf);
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		return true;
	}

	boolean leaveGroup(String mcastAddr, int mcastPort, String bindAddr)
	 {
		try {	 	
			InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
			NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
			ssdpUniSock.leaveGroup(mcastGroup, mcastIf);
		 }
		 catch (Exception e) {
			 Debug.warning(e);
			 return false;
		 }
		 return true;
	 }
*/
}

