/******************************************************************
*
*	CyberUtil for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: TimerUtil.java
*
*	Revision:
*
*	01/15/03
*		- first revision.
*
******************************************************************/

package org.cybergarage.util;

public final class TimerUtil
{
	public final static void wait(int waitTime)
	{
		try {
			Thread.sleep(waitTime);
		}
		catch (Exception e) {}
	}

	public final static void waitRandom(int time)
	{
		int waitTime = (int)(Math.random() * (double)time);		
		try {
			Thread.sleep(waitTime);
		}
		catch (Exception e) {}
	}
}

