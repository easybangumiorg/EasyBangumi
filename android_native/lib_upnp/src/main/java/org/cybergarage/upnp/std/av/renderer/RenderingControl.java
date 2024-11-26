/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : RenderingControl.java
*
*	Revision:
*
*	02/22/08
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.renderer;

import org.cybergarage.util.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.control.*;

public class RenderingControl implements ActionListener, QueryListener
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////

	public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:RenderingControl:1";		
	
	// Browse Action	
	
	public final static String PRESETNAMELIST = "PresetNameList";
	public final static String LASTCHANGE = "LastChange";
	public final static String BRIGHTNESS = "Brightness";
	public final static String CONTRAST = "Contrast";
	public final static String SHARPNESS = "Sharpness";
	public final static String REDVIDEOGAIN = "RedVideoGain";
	public final static String GREENVIDEOGAIN = "GreenVideoGain";
	public final static String BLUEVIDEOGAIN = "BlueVideoGain";
	public final static String REDVIDEOBLACKLEVEL = "RedVideoBlackLevel";
	public final static String GREENVIDEOBLACKLEVEL = "GreenVideoBlackLevel";
	public final static String BLUEVIDEOBLACKLEVEL = "BlueVideoBlackLevel";
	public final static String COLORTEMPERATURE = "ColorTemperature";
	public final static String HORIZONTALKEYSTONE = "HorizontalKeystone";
	public final static String VERTICALKEYSTONE = "VerticalKeystone";
	public final static String MUTE = "Mute";
	public final static String VOLUME = "Volume";
	public final static String VOLUMEDB = "VolumeDB";
	public final static String LOUDNESS = "Loudness";
	public final static String LISTPRESETS = "ListPresets";
	public final static String INSTANCEID = "InstanceID";
	public final static String CURRENTPRESETNAMELIST = "CurrentPresetNameList";
	public final static String SELECTPRESET = "SelectPreset";
	public final static String PRESETNAME = "PresetName";
	public final static String GETBRIGHTNESS = "GetBrightness";
	public final static String CURRENTBRIGHTNESS = "CurrentBrightness";
	public final static String SETBRIGHTNESS = "SetBrightness";
	public final static String DESIREDBRIGHTNESS = "DesiredBrightness";
	public final static String GETCONTRAST = "GetContrast";
	public final static String CURRENTCONTRAST = "CurrentContrast";
	public final static String SETCONTRAST = "SetContrast";
	public final static String DESIREDCONTRAST = "DesiredContrast";
	public final static String GETSHARPNESS = "GetSharpness";
	public final static String CURRENTSHARPNESS = "CurrentSharpness";
	public final static String SETSHARPNESS = "SetSharpness";
	public final static String DESIREDSHARPNESS = "DesiredSharpness";
	public final static String GETREDVIDEOGAIN = "GetRedVideoGain";
	public final static String CURRENTREDVIDEOGAIN = "CurrentRedVideoGain";
	public final static String SETREDVIDEOGAIN = "SetRedVideoGain";
	public final static String DESIREDREDVIDEOGAIN = "DesiredRedVideoGain";
	public final static String GETGREENVIDEOGAIN = "GetGreenVideoGain";
	public final static String CURRENTGREENVIDEOGAIN = "CurrentGreenVideoGain";
	public final static String SETGREENVIDEOGAIN = "SetGreenVideoGain";
	public final static String DESIREDGREENVIDEOGAIN = "DesiredGreenVideoGain";
	public final static String GETBLUEVIDEOGAIN = "GetBlueVideoGain";
	public final static String CURRENTBLUEVIDEOGAIN = "CurrentBlueVideoGain";
	public final static String SETBLUEVIDEOGAIN = "SetBlueVideoGain";
	public final static String DESIREDBLUEVIDEOGAIN = "DesiredBlueVideoGain";
	public final static String GETREDVIDEOBLACKLEVEL = "GetRedVideoBlackLevel";
	public final static String CURRENTREDVIDEOBLACKLEVEL = "CurrentRedVideoBlackLevel";
	public final static String SETREDVIDEOBLACKLEVEL = "SetRedVideoBlackLevel";
	public final static String DESIREDREDVIDEOBLACKLEVEL = "DesiredRedVideoBlackLevel";
	public final static String GETGREENVIDEOBLACKLEVEL = "GetGreenVideoBlackLevel";
	public final static String CURRENTGREENVIDEOBLACKLEVEL = "CurrentGreenVideoBlackLevel";
	public final static String SETGREENVIDEOBLACKLEVEL = "SetGreenVideoBlackLevel";
	public final static String DESIREDGREENVIDEOBLACKLEVEL = "DesiredGreenVideoBlackLevel";
	public final static String GETBLUEVIDEOBLACKLEVEL = "GetBlueVideoBlackLevel";
	public final static String CURRENTBLUEVIDEOBLACKLEVEL = "CurrentBlueVideoBlackLevel";
	public final static String SETBLUEVIDEOBLACKLEVEL = "SetBlueVideoBlackLevel";
	public final static String DESIREDBLUEVIDEOBLACKLEVEL = "DesiredBlueVideoBlackLevel";
	public final static String GETCOLORTEMPERATURE = "GetColorTemperature";
	public final static String CURRENTCOLORTEMPERATURE = "CurrentColorTemperature";
	public final static String SETCOLORTEMPERATURE = "SetColorTemperature";
	public final static String DESIREDCOLORTEMPERATURE = "DesiredColorTemperature";
	public final static String GETHORIZONTALKEYSTONE = "GetHorizontalKeystone";
	public final static String CURRENTHORIZONTALKEYSTONE = "CurrentHorizontalKeystone";
	public final static String SETHORIZONTALKEYSTONE = "SetHorizontalKeystone";
	public final static String DESIREDHORIZONTALKEYSTONE = "DesiredHorizontalKeystone";
	public final static String GETVERTICALKEYSTONE = "GetVerticalKeystone";
	public final static String CURRENTVERTICALKEYSTONE = "CurrentVerticalKeystone";
	public final static String SETVERTICALKEYSTONE = "SetVerticalKeystone";
	public final static String DESIREDVERTICALKEYSTONE = "DesiredVerticalKeystone";
	public final static String GETMUTE = "GetMute";
	public final static String CHANNEL = "Channel";
	public final static String CURRENTMUTE = "CurrentMute";
	public final static String SETMUTE = "SetMute";
	public final static String DESIREDMUTE = "DesiredMute";
	public final static String GETVOLUME = "GetVolume";
	public final static String CURRENTVOLUME = "CurrentVolume";
	public final static String SETVOLUME = "SetVolume";
	public final static String DESIREDVOLUME = "DesiredVolume";
	public final static String GETVOLUMEDB = "GetVolumeDB";
	public final static String SETVOLUMEDB = "SetVolumeDB";
	public final static String GETVOLUMEDBRANGE = "GetVolumeDBRange";
	public final static String MINVALUE = "MinValue";
	public final static String MAXVALUE = "MaxValue";
	public final static String GETLOUDNESS = "GetLoudness";
	public final static String CURRENTLOUDNESS = "CurrentLoudness";
	public final static String SETLOUDNESS = "SetLoudness";
	public final static String DESIREDLOUDNESS = "DesiredLoudness";
	
	public final static String MASTER = "Master";
	public final static String FACTORYDEFAULTS = "FactoryDefaults";

	public final static String SCPD = 
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\">\n" +
		"   <specVersion>\n" +
		"      <major>1</major>\n" +
		"      <minor>0</minor>\n" +
		"	</specVersion>\n" +
		"  <serviceStateTable>"+
		"    <stateVariable>"+
		"      <name>PresetNameList</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>string</dataType>"+
		"    </stateVariable>"+
		"    <stateVariable> "+
		"      <name>LastChange</name> <sendEventsAttribute>yes</sendEventsAttribute>"+
		"      <dataType>string</dataType>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>Brightness</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>Contrast</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>Sharpness</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>RedVideoGain</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>GreenVideoGain</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>BlueVideoGain</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>RedVideoBlackLevel</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>GreenVideoBlackLevel</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>BlueVideoBlackLevel</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>ColorTemperature</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>HorizontalKeystone</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>i2</dataType>"+
		"	<allowedValueRange>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>VerticalKeystone</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>i2</dataType>"+
		"	<allowedValueRange>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>Mute</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>boolean</dataType>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>Volume</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui2</dataType>"+
		"	<allowedValueRange>"+
		"		<minimum>0</minimum>"+
		"		<step>1</step>"+
		"	</allowedValueRange>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>VolumeDB</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>i2</dataType>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>Loudness</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>boolean</dataType>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>A_ARG_TYPE_Channel</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>string</dataType>"+
		"      <allowedValueList>"+
		"        <allowedValue>Master</allowedValue>"+
		"      </allowedValueList>"+
		"    </stateVariable>"+
		"    <stateVariable><Optional/>"+
		"      <name>A_ARG_TYPE_InstanceID</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>ui4</dataType>"+
		"    </stateVariable>"+
		"    <stateVariable>"+
		"      <name>A_ARG_TYPE_PresetName</name> <sendEventsAttribute>no</sendEventsAttribute>"+
		"      <dataType>string</dataType>"+
		"      <allowedValueList>"+
		"        <allowedValue>FactoryDefaults</allowedValue>"+
		"      </allowedValueList>"+
		"    </stateVariable>"+
		"  </serviceStateTable>"+
		"  <actionList>"+
		"    <action>"+
		"    <name>ListPresets</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentPresetNameList</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>PresetNameList</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action>"+
		"    <name>SelectPreset</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>PresetName</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_PresetName</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetBrightness</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentBrightness</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>Brightness</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetBrightness</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredBrightness</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>Brightness</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetContrast</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentContrast</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>Contrast</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetContrast</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredContrast</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>Contrast</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetSharpness</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentSharpness</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>Sharpness</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetSharpness</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredSharpness</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>Sharpness</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetRedVideoGain</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentRedVideoGain</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>RedVideoGain</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetRedVideoGain</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredRedVideoGain</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>RedVideoGain</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetGreenVideoGain</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentGreenVideoGain</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>GreenVideoGain</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetGreenVideoGain</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredGreenVideoGain</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>GreenVideoGain</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetBlueVideoGain</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentBlueVideoGain</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>BlueVideoGain</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetBlueVideoGain</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredBlueVideoGain</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>BlueVideoGain</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"   <action><Optional/>"+
		"    <name>GetRedVideoBlackLevel</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentRedVideoBlackLevel</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>RedVideoBlackLevel</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetRedVideoBlackLevel</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredRedVideoBlackLevel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>RedVideoBlackLevel</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetGreenVideoBlackLevel</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentGreenVideoBlackLevel</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>GreenVideoBlackLevel</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetGreenVideoBlackLevel</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredGreenVideoBlackLevel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>GreenVideoBlackLevel</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetBlueVideoBlackLevel</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentBlueVideoBlackLevel</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>BlueVideoBlackLevel</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetBlueVideoBlackLevel</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredBlueVideoBlackLevel</name>"+
		"    <direction>in</direction>"+
		"  <relatedStateVariable>BlueVideoBlackLevel</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetColorTemperature </name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentColorTemperature</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>ColorTemperature</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetColorTemperature</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredColorTemperature</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>ColorTemperature</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetHorizontalKeystone</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentHorizontalKeystone</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>HorizontalKeystone</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetHorizontalKeystone</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredHorizontalKeystone</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>HorizontalKeystone</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetVerticalKeystone</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentVerticalKeystone</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>VerticalKeystone</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetVerticalKeystone</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredVerticalKeystone</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>VerticalKeystone</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetMute</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>Channel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_Channel</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentMute</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>Mute</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetMute</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>Channel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_Channel</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredMute</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>Mute</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetVolume</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>Channel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_Channel</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentVolume</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>Volume</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetVolume</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>Channel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_Channel</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredVolume</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>Volume</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetVolumeDB</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>Channel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_Channel</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentVolume</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>VolumeDB</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetVolumeDB</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>Channel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_Channel</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredVolume</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>VolumeDB</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetVolumeDBRange</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>Channel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_Channel</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>MinValue</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>VolumeDB</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>MaxValue</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>VolumeDB</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>GetLoudness</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>Channel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_Channel</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>CurrentLoudness</name>"+
		"          <direction>out</direction>"+
		"          <relatedStateVariable>Loudness</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"    <action><Optional/>"+
		"    <name>SetLoudness</name>"+
		"      <argumentList>"+
		"        <argument>"+
		"          <name>InstanceID</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>Channel</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>A_ARG_TYPE_Channel</relatedStateVariable>"+
		"        </argument>"+
		"        <argument>"+
		"          <name>DesiredLoudness</name>"+
		"          <direction>in</direction>"+
		"          <relatedStateVariable>Loudness</relatedStateVariable>"+
		"        </argument>"+
		"      </argumentList>"+
		"    </action>"+
		"  </actionList>"+
		"</scpd>";	

	////////////////////////////////////////////////
	// Constructor 
	////////////////////////////////////////////////
	
	public RenderingControl(MediaRenderer render)
	{
		setMediaRenderer(render);
	}
	
	////////////////////////////////////////////////
	// MediaRender
	////////////////////////////////////////////////

	private MediaRenderer mediaRenderer;
	
	private void setMediaRenderer(MediaRenderer render)
	{
		mediaRenderer = render;	
	}
	
	public MediaRenderer getMediaRenderer()
	{
		return mediaRenderer;	
	}
	
	////////////////////////////////////////////////
	// Mutex
	////////////////////////////////////////////////
	
	private Mutex mutex = new Mutex();
	
	public void lock()
	{
		mutex.lock();
	}
	
	public void unlock()
	{
		mutex.unlock();
	}
	
	////////////////////////////////////////////////
	// ActionListener
	////////////////////////////////////////////////

	public boolean actionControlReceived(Action action)
	{
		boolean isActionSuccess;
		
		String actionName = action.getName();

		if (actionName == null)
			return false;
		
		isActionSuccess = false;
		MediaRenderer dmr = getMediaRenderer();
		if (dmr != null) {
			ActionListener listener = dmr.getActionListener();
			if (listener != null)
				listener.actionControlReceived(action);
		}
		
		return isActionSuccess;
	}

	////////////////////////////////////////////////
	// QueryListener
	////////////////////////////////////////////////

	public boolean queryControlReceived(StateVariable stateVar)
	{
		return false;
	}
}

