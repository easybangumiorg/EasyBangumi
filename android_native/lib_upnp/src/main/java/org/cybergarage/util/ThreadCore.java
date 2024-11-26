/******************************************************************
*
*	CyberUtil for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: Thread.java
*
*	Revision:
*
*	01/05/04
*		- first revision.
*	08/23/07
*		- Thanks for Kazuyuki Shudo
*		- Changed stop() to stop more safety using Thread::interrupt().
*	
******************************************************************/

package org.cybergarage.util;

public class ThreadCore implements Runnable
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public ThreadCore()
	{
	}
	
	////////////////////////////////////////////////
	//	Thread
	////////////////////////////////////////////////
	
	private java.lang.Thread mThreadObject = null;
	
	public void setThreadObject(java.lang.Thread obj) {
		mThreadObject = obj;
	}

	public java.lang.Thread getThreadObject() {
		return mThreadObject;
	}

	public void start() 
	{
		java.lang.Thread threadObject = getThreadObject();
		if (threadObject == null) {
			threadObject = new java.lang.Thread(this,"Cyber.ThreadCore");
			setThreadObject(threadObject);
			threadObject.start();
		}
	}
	
	public void run()
	{
	}

	public boolean isRunnable()
	{
		return (Thread.currentThread() == getThreadObject()) ? true : false;
	}
	
	public void stop() 
	{
		java.lang.Thread threadObject = getThreadObject();
		if (threadObject != null) { 
			//threadObject.destroy();
			//threadObject.stop();
			
			// Thanks for Kazuyuki Shudo (08/23/07)
			threadObject.interrupt();
			
			setThreadObject(null);
		}
	}
	
	public void restart()
	{
		stop();
		start();
	}
}
