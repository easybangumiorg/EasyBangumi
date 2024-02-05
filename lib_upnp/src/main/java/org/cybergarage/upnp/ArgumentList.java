/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: ArgumentList.java
*
*	Revision:
*
*	12/05/02
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp;

import java.util.Vector;

public class ArgumentList extends Vector 
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public final static String ELEM_NAME = "argumentList";

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public ArgumentList() 
	{
	}
	
	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////
	
	public Argument getArgument(int n)
	{
		return (Argument)get(n);
	}

	public Argument getArgument(String name)
	{
		int nArgs = size();
		for (int n=0; n<nArgs; n++) {
			Argument arg = getArgument(n);
			String argName = arg.getName();
			if (argName == null)
				continue;
			if (argName.equals(name) == true)
				return arg;
		}
		return null;
	}

	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////
	/**
	 * deprecated
	 */
	public void set(ArgumentList inArgList)
	{
		int nInArgs = inArgList.size();
		for (int n=0; n<nInArgs; n++) {
			Argument inArg = inArgList.getArgument(n);
			String inArgName = inArg.getName();
			Argument arg = getArgument(inArgName);
			if (arg == null)
				continue;
			arg.setValue(inArg.getValue());
		}
	}

	/**
	 * Set all the Argument which are Input Argoument to the given value in 
	 * the argument list
	 * 
	 * @param inArgList
	 */
	public void setReqArgs(ArgumentList inArgList)
	{
        int nArgs = size();
        for (int n=0; n<nArgs; n++) { 
            Argument arg = getArgument(n);
            if (arg.isInDirection()){
	            String argName = arg.getName();
	            Argument inArg = inArgList.getArgument(argName);
	            if (inArg == null)
	                throw new IllegalArgumentException("Argument \"" + argName + "\" missing.");
	            arg.setValue(inArg.getValue());
            }
        }
	}
	/**
	 * Set all the Argument which are Output Argoument to the given value in 
	 * the argument list
	 * 
	 * @param outArgList
	 */
	public void setResArgs(ArgumentList outArgList)
	{
        int nArgs = size();
        for (int n=0; n<nArgs; n++) {
            Argument arg = getArgument(n);
            if (arg.isOutDirection()){
	            String argName = arg.getName();
	            Argument outArg = outArgList.getArgument(argName);
	            if (outArg == null)
	                throw new IllegalArgumentException("Argument \"" + argName + "\" missing.");
	            arg.setValue(outArg.getValue());
            }
        }
	}


}

