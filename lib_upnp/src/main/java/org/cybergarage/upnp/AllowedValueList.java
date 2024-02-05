/******************************************************************
*
*	CyberLink for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: AllowedValueList.java
*
*	Revision:
*
*	03/27/04
*		- first revision.
*	02/28/05
*		- Changed to use AllowedValue instead of String as the member.
*	
******************************************************************/

package org.cybergarage.upnp;

import java.util.Iterator;
import java.util.Vector;

public class AllowedValueList extends Vector
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public final static String ELEM_NAME = "allowedValueList";


	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public AllowedValueList() 
	{
	}

	public AllowedValueList(String[] values) {
		for (int i = 0; i < values.length; i++) {
			add(new AllowedValue(values[i]));
		};
		
	}

	
	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////
	
	public AllowedValue getAllowedValue(int n)
	{
		return (AllowedValue)get(n);
	}

	public boolean isAllowed(String v){
		for (Iterator i = this.iterator(); i.hasNext();) {
			AllowedValue av = (AllowedValue) i.next();
			if(av.getValue().equals(v))
				return true;
		}
		return false;
	}
}
