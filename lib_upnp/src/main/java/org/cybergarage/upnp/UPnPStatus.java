/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: UPnPStatus.java
*
*	Revision;
*
*	11/18/02
*		- first revision.
*	01/03/04
*		- Changed the class name from UPnPError to UPnPStatus.
*	
******************************************************************/

package org.cybergarage.upnp;
import org.cybergarage.http.HTTPStatus;

public class UPnPStatus
{
	////////////////////////////////////////////////
	//	Code
	////////////////////////////////////////////////
	
	public static final int INVALID_ACTION = 401;
	public static final int INVALID_ARGS = 402;
	public static final int OUT_OF_SYNC = 403;
	public static final int INVALID_VAR = 404;
	public static final int PRECONDITION_FAILED = 412;
	public static final int ACTION_FAILED = 501;

	public static final String code2String(int code)
	{
		switch (code) {
		case INVALID_ACTION: return "Invalid Action";
		case INVALID_ARGS: return "Invalid Args";
		case OUT_OF_SYNC: return "Out of Sync";
		case INVALID_VAR: return "Invalid Var";
		case PRECONDITION_FAILED: return "Precondition Failed";
		case ACTION_FAILED: return "Action Failed";
	        default: return HTTPStatus.code2String(code);
		}
	}
	
	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////
	
	private int code;
	private String description;
	
	public UPnPStatus()
	{
		setCode(0);
		setDescription("");	
	}
	
	public UPnPStatus(int code, String desc)
	{
		setCode(code);
		setDescription(desc);	
	}
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
