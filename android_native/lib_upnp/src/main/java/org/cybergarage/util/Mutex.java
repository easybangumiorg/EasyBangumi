/******************************************************************
*
*	CyberUtil for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: Mutex.java
*
*	Revision:
*
*	06/19/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.util;

public class Mutex
{
	private boolean syncLock;
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public Mutex()
	{
		syncLock = false;
	}
	
	////////////////////////////////////////////////
	//	lock
	////////////////////////////////////////////////
	
	public synchronized void lock()
	{
		while(syncLock == true) {
			try {
				wait();
			}
			catch (Exception e) {
				Debug.warning(e);
			};
		}
		syncLock = true;
	}

	public synchronized void unlock()
	{
		syncLock = false;
		notifyAll();
	}

}