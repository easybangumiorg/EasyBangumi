/******************************************************************
*
*	MediaPlayer for CyberLink
*
*	Copyright (C) Satoshi Konno 2005
*
*	File : MediaPlayer.java
*
*	09/26/05
*		- first revision.
*	02/05/08
*		- Added getContentDirectory(dev, objectId).
*		- Added browse().
*
******************************************************************/

package org.cybergarage.upnp.std.av.player;

import org.cybergarage.upnp.std.av.renderer.*;
import org.cybergarage.upnp.std.av.controller.*;

public class MediaPlayer
{
	////////////////////////////////////////////////
	// Member
	////////////////////////////////////////////////
	
	private MediaRenderer renderer = null;
	private MediaController controller = null;
	
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	
	public MediaPlayer(boolean useDMC, boolean useDMR)
	{
		if (useDMC)
			enableRenderer();
		if (useDMR)
			enableController();
	}
	
	public MediaPlayer()
	{
		this(true, true);
	}
	
	////////////////////////////////////////////////
	// Controller
	////////////////////////////////////////////////
	
	public MediaController getController()
	{
		return controller;
	}

	public void enableController()
	{
		if (controller == null)
			controller = new MediaController();
	}
	
	public void disableController()
	{
		controller = null;
	}

	public boolean isControllerEnable()
	{
		return (controller != null) ? true : false;
	}
	
	////////////////////////////////////////////////
	// Renderer
	////////////////////////////////////////////////
	
	public MediaRenderer getRenderer()
	{
		return renderer;
	}

	public void enableRenderer()
	{
		if (renderer == null)
			renderer = new MediaRenderer();
	}
	
	public void disableRenderer()
	{
		renderer = null;
	}

	public boolean isRendererEnable()
	{
		return (renderer != null) ? true : false;
	}
	
	////////////////////////////////////////////////
	// Member
	///////////////////////////
	/////////////////////
	
	public void start()
	{
		if (renderer != null)
			renderer.start();
		if (controller != null)
			controller.start();
	}
	
	public void stop()
	{
		if (renderer != null)
			renderer.stop();
		if (controller != null)
			controller.stop();
	}
}
