package org.cybergarage.upnp;


/**
 * @author Stefano "Kismet" Lenzi - kismet-sl@users.sourceforge.net  <br> 
 * 		Copyright (c) 2005
 *
 */
public interface RootDescription {

	public final String ROOT_ELEMENT = "root";
	public final String ROOT_ELEMENT_NAMESPACE = "urn:schemas-upnp-org:device-1-0"; 
		
	
	public final String SPECVERSION_ELEMENT = "specVersion";
	public final String MAJOR_ELEMENT = "major";
	public final String MINOR_ELEMENT = "minor";
	public final String SERVICE_LIST_ELEMENT = "serviceList";
}
