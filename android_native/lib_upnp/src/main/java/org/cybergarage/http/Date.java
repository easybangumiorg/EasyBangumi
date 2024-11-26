/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File : Date.java
*
*	Revision;
*
*	01/05/03
*		- first revision
*	10/20/04
*		- Theo Beisch <theo.beisch@gmx.de>
*		- Fixed the following methods to use HOUR_OF_DAY instead of HOUR.
*			getHour(), getDateString() getTimeString()
*		- Fixed getInstance() to return GMT instance.
*
******************************************************************/

package org.cybergarage.http;

import java.util.Calendar;
import java.util.TimeZone;

public class Date
{
	private Calendar cal;

	public Date(Calendar cal)
	{
		this.cal = cal;
	}

	public Calendar getCalendar()
	{
		return cal;
	}

	////////////////////////////////////////////////
	//	Time
	////////////////////////////////////////////////

	public int getHour()
	{
		// Thanks for Theo Beisch (10/20/04)
		return getCalendar().get(Calendar.HOUR_OF_DAY);
	}

	public int getMinute()
	{
		return getCalendar().get(Calendar.MINUTE);
	}

	public int getSecond()
	{
		return getCalendar().get(Calendar.SECOND);
	}
	
	////////////////////////////////////////////////
	//	paint
	////////////////////////////////////////////////

	public final static Date getLocalInstance()
	{
		return new Date(Calendar.getInstance());
	}

	public final static Date getInstance()
	{
		// Thanks for Theo Beisch (10/20/04)
		return new Date(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
	}
	
	////////////////////////////////////////////////
	//	getDateString
	////////////////////////////////////////////////

	public final static String toDateString(int value)
	{
		if (value < 10)
			return "0" + Integer.toString(value);
		return Integer.toString(value);
	}

	private final static String MONTH_STRING[] = {
		"Jan",
		"Feb",
		"Mar",
		"Apr",
		"May",
		"Jun",
		"Jul",
		"Aug",
		"Sep",
		"Oct",
		"Nov",
		"Dec",
	};

	public final static String toMonthString(int value)
	{
		value -= Calendar.JANUARY;
		if (0 <= value && value < 12)
			return MONTH_STRING[value];
		return "";
	}
	
	private final static String WEEK_STRING[] = {
		"Sun",
		"Mon",
		"Tue",
		"Wed",
		"Thu",
		"Fri",
		"Sat",
	};

	public final static String toWeekString(int value)
	{
		value -= Calendar.SUNDAY;
		if (0 <= value && value < 7)
			return WEEK_STRING[value];
		return "";
	}

	public final static String toTimeString(int value)
	{
		String str  = "";
		if (value < 10)
			str += "0";
		str += Integer.toString(value);
		return str;
	}
	
	public String getDateString()
	{
		// Thanks for Theo Beisch (10/20/04)
		Calendar cal = getCalendar();
		return
			toWeekString(cal.get(Calendar.DAY_OF_WEEK)) +", " + 
			toTimeString(cal.get(Calendar.DATE)) + " " +
			toMonthString(cal.get(Calendar.MONTH)) + " " +
			Integer.toString(cal.get(Calendar.YEAR)) + " " +
			toTimeString(cal.get(Calendar.HOUR_OF_DAY)) + ":" +
			toTimeString(cal.get(Calendar.MINUTE)) + ":" +
			toTimeString(cal.get(Calendar.SECOND)) + " GMT";
	}

	////////////////////////////////////////////////
	//	getTimeString
	////////////////////////////////////////////////
	
	public String getTimeString()
	{
		// Thanks for Theo Beisch (10/20/04)
		Calendar cal = getCalendar();
		return
			toDateString(cal.get(Calendar.HOUR_OF_DAY)) +
			(((cal.get(Calendar.SECOND) % 2) == 0) ? ":" : " ") +
			toDateString(cal.get(Calendar.MINUTE));
	}
		
}

