/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: StateVariable.java
*
*	Revision;
*
*	12/06/02
*		- first revision.
*	06/17/03
*		- Added setSendEvents(), isSendEvents().
*		- Changed to send a event after check the eventing state using isSendEvents().
*	01/04/04
*		- Added UPnP status methods.
*	01/06/04
*		- Added the following methods.
*		  getQueryListener() 
*		  setQueryListener() 
*		  performQueryListener()
*	01/07/04
*		- Added StateVariable() and set();
*		- Changed performQueryListener() to use a copy of the StateVariable.
*	03/27/04
*		- Thanks for Adavy
*		- Added getAllowedValueList() and getAllowedValueRange().
*	05/11/04
*		- Added hasAllowedValueList() and hasAllowedValueRange().
*	07/09/04
*		- Thanks for Dimas <cyberrate@users.sourceforge.net> and Stefano Lenzi <kismet-sl@users.sourceforge.net>
*		- Changed postQuerylAction() to set the status code to the UPnPStatus.
*	11/09/04
*		- Theo Beisch <theo.beisch@gmx.de>
*		- Changed StateVariable::setValue() to update and send the event when the value is not equal to the current value.
*	11/19/04
*		- Rick Keiner <rick@emanciple.com>
*		- Fixed setValue() to compare only when the current value is not null.
*	02/28/05
*		- Changed getAllowedValueList() to use AllowedValue instead of String as the member.
*	04/12/06
*		- Added setUserData() and getUserData() to set a user original data object.
*	
******************************************************************/

package org.cybergarage.upnp;
import java.util.Iterator;

import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.upnp.control.QueryRequest;
import org.cybergarage.upnp.control.QueryResponse;
import org.cybergarage.upnp.xml.NodeData;
import org.cybergarage.upnp.xml.StateVariableData;
import org.cybergarage.util.Debug;
import org.cybergarage.xml.Node;

public class StateVariable extends NodeData
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public final static String ELEM_NAME = "stateVariable";

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private Node stateVariableNode;
	private Node serviceNode;

	public Node getServiceNode()
	{
		return serviceNode;
	}
	
	void setServiceNode(Node n){
		serviceNode=n;
	}

	public Service getService()
	{
		Node serviceNode = getServiceNode();
		if (serviceNode == null)
			return null;
		return new Service(serviceNode);
	}

	public Node getStateVariableNode()
	{
		return stateVariableNode;
	}
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public StateVariable()
	{
		this.serviceNode = null;
		this.stateVariableNode = new Node(ELEM_NAME);
	}
	
	public StateVariable(Node serviceNode, Node stateVarNode)
	{
		this.serviceNode = serviceNode;
		this.stateVariableNode = stateVarNode;
	}

	////////////////////////////////////////////////
	//	isStateVariableNode
	////////////////////////////////////////////////

	public static boolean isStateVariableNode(Node node)
	{
		return StateVariable.ELEM_NAME.equals(node.getName());
	}

	////////////////////////////////////////////////
	//	name
	////////////////////////////////////////////////

	private final static String NAME = "name";
	
	public void setName(String value)
	{
		getStateVariableNode().setNode(NAME, value);
	}

	public String getName()
	{
		return getStateVariableNode().getNodeValue(NAME);
	}

	////////////////////////////////////////////////
	//	dataType
	////////////////////////////////////////////////

	private final static String DATATYPE = "dataType";
	
	public void setDataType(String value)
	{
		getStateVariableNode().setNode(DATATYPE, value);
	}

	public String getDataType()
	{
		return getStateVariableNode().getNodeValue(DATATYPE);
	}

	////////////////////////////////////////////////
	// dataType
	////////////////////////////////////////////////

	private final static String SENDEVENTS = "sendEvents";
	private final static String SENDEVENTS_YES = "yes";
	private final static String SENDEVENTS_NO = "no";

	public void setSendEvents(boolean state)
	{
		getStateVariableNode().setAttribute(SENDEVENTS, (state == true) ? SENDEVENTS_YES : SENDEVENTS_NO);
	}
	
	public boolean isSendEvents()
	{
		String state = getStateVariableNode().getAttributeValue(SENDEVENTS);
		if (state == null)
			return false;
		if (state.equalsIgnoreCase(SENDEVENTS_YES) == true)
			return true;
		return false;
	}
	
	////////////////////////////////////////////////
	// set
	////////////////////////////////////////////////

	public void set(StateVariable stateVar) 
	{
		setName(stateVar.getName());
		setValue(stateVar.getValue());
		setDataType(stateVar.getDataType());
		setSendEvents(stateVar.isSendEvents());
	}
	
	////////////////////////////////////////////////
	//	UserData
	////////////////////////////////////////////////

	public StateVariableData getStateVariableData ()
	{
		Node node = getStateVariableNode();
		StateVariableData userData = (StateVariableData)node.getUserData();
		if (userData == null) {
			userData = new StateVariableData();
			node.setUserData(userData);
			userData.setNode(node);
		}
		return userData;
	}

	////////////////////////////////////////////////
	//	Value
	////////////////////////////////////////////////

	public void setValue(String value) 
	{
		// Thnaks for Tho Beisch (11/09/04)
		String currValue = getStateVariableData().getValue();
		// Thnaks for Tho Rick Keiner (11/18/04)
		if (currValue != null && currValue.equals(value) == true)
			return;
		
		getStateVariableData().setValue(value);
		
		// notify event
		Service service = getService();
		if (service == null)
			return;
		if (isSendEvents() == false)
			return;
		service.notify(this);
	}

	public void setValue(int value)
	{
		setValue(Integer.toString(value));	
	}
	
	public void setValue(long value)
	{
		setValue(Long.toString(value));	
	}
	
	public String getValue() 
	{
		return getStateVariableData().getValue();
	}

	////////////////////////////////////////////////
	//	AllowedValueList
	////////////////////////////////////////////////

	public AllowedValueList getAllowedValueList()
	{
		AllowedValueList valueList= new AllowedValueList();
		Node valueListNode = getStateVariableNode().getNode(AllowedValueList.ELEM_NAME);
		if (valueListNode == null)
			return null;
		int nNode = valueListNode.getNNodes();
		for (int n=0; n<nNode; n++) {
			Node node = valueListNode.getNode(n);
			if (AllowedValue.isAllowedValueNode(node) == false)
				continue;
			AllowedValue allowedVal = new AllowedValue(node);
			valueList.add(allowedVal);
		} 
		return valueList;
	}

	/**
	 * This method ovverride the value of the AllowedValueList Node<br>
	 * of this object. <br>
	 * <br>
	 * Note: This method should be used to create a dynamic<br>
	 * Device withtout writing any XML that describe the device<br>.
	 * <br>
	 * Note2: The enforce the constraint of the SCPD rule the<br>
	 * AllowedValueList and AllowedValueRange are mutal exclusive<br>
	 * the last set will be the only present<br>
	 * 
	 * @param avl The new AllowedValueList
	 * 
	 * @author Stefano "Kismet" Lenzi - kismet-sl@users.sourceforge.net  - 2005
	 */
	public void setAllowedValueList(AllowedValueList avl) {
		//TODO Some test done not stable 	
		getStateVariableNode().removeNode(AllowedValueList.ELEM_NAME);
		getStateVariableNode().removeNode(AllowedValueRange.ELEM_NAME);
		Node n = new Node(AllowedValueList.ELEM_NAME);
		Iterator i=avl.iterator();
		while (i.hasNext()) {
			AllowedValue av = (AllowedValue) i.next();
			//n.addNode(new Node(AllowedValue.ELEM_NAME,av.getValue())); wrong!
			n.addNode(av.getAllowedValueNode());						//better (twa)
		}
		getStateVariableNode().addNode(n);		
		
	}

	
	public boolean hasAllowedValueList()
	{
		AllowedValueList valueList = getAllowedValueList();
		return (valueList != null) ? true : false;
	}
	
	////////////////////////////////////////////////
	//	AllowedValueRange
	////////////////////////////////////////////////

	public AllowedValueRange getAllowedValueRange()
	{
		Node valueRangeNode = getStateVariableNode().getNode(AllowedValueRange.ELEM_NAME);
		if (valueRangeNode == null)
			return null;
		return new AllowedValueRange(valueRangeNode);
	}
	
	/**
	 * This method ovverride the value of the AllowedValueRange Node<br>
	 * of this object. <br>
	 * <br>
	 * Note: This method should be used to create a dynamic<br>
	 * Device withtout writing any XML that describe the device<br>.
	 * <br>
	 * Note2: The enforce the constraint of the SCPD rule the<br>
	 * AllowedValueList and AllowedValueRange are mutal exclusive<br>
	 * the last set will be the only present<br>
	 * 
	 * @param avr The new AllowedValueRange
	 * 
	 * @author Stefano "Kismet" Lenzi - kismet-sl@users.sourceforge.net  - 2005
	 */	
	public void setAllowedValueRange(AllowedValueRange avr){
		//TODO Some test done not stable
		getStateVariableNode().removeNode(AllowedValueList.ELEM_NAME);
		getStateVariableNode().removeNode(AllowedValueRange.ELEM_NAME);
		getStateVariableNode().addNode(avr.getAllowedValueRangeNode());
		
	}

	public boolean hasAllowedValueRange()
	{
		return (getAllowedValueRange() != null) ? true : false;
	}

	////////////////////////////////////////////////
	//	queryAction
	////////////////////////////////////////////////

	public QueryListener getQueryListener() 
	{
		return getStateVariableData().getQueryListener();
	}

	public void setQueryListener(QueryListener listener) 
	{
		getStateVariableData().setQueryListener(listener);
	}
	
	public boolean performQueryListener(QueryRequest queryReq)
	{
		QueryListener listener = getQueryListener();
		if (listener == null)
			return false;
		QueryResponse queryRes = new QueryResponse();
		StateVariable retVar = new StateVariable();
		retVar.set(this);
		retVar.setValue("");
		retVar.setStatus(UPnPStatus.INVALID_VAR);
		if (listener.queryControlReceived(retVar) == true) {
			queryRes.setResponse(retVar);
		}
		else {
			UPnPStatus upnpStatus = retVar.getStatus();
			queryRes.setFaultResponse(upnpStatus.getCode(), upnpStatus.getDescription());
		}
		queryReq.post(queryRes);
		return true;
	}

	////////////////////////////////////////////////
	//	ActionControl
	////////////////////////////////////////////////

	public QueryResponse getQueryResponse() 
	{
		return getStateVariableData().getQueryResponse();
	}

	private void setQueryResponse(QueryResponse res) 
	{
		getStateVariableData().setQueryResponse(res);
	}

	public UPnPStatus getQueryStatus()
	{
		return getQueryResponse().getUPnPError();
	}
	
	////////////////////////////////////////////////
	//	ActionControl
	////////////////////////////////////////////////

	public boolean postQuerylAction()
	{
		QueryRequest queryReq = new QueryRequest();
		queryReq.setRequest(this);
		if (Debug.isOn() == true)
			queryReq.print();
		QueryResponse queryRes = queryReq.post();
		if (Debug.isOn() == true)
			queryRes.print();
		setQueryResponse(queryRes);
		// Thanks for Dimas <cyberrate@users.sourceforge.net> and Stefano Lenzi <kismet-sl@users.sourceforge.net> (07/09/04)
		if (queryRes.isSuccessful() == false) {
			setValue(queryRes.getReturnValue());
			return false;
		}
		setValue(queryRes.getReturnValue());
		return true;
	}

	////////////////////////////////////////////////
	//	UPnPStatus
	////////////////////////////////////////////////

	private UPnPStatus upnpStatus = new UPnPStatus();
	
	public void setStatus(int code, String descr)
	{
		upnpStatus.setCode(code);
		upnpStatus.setDescription(descr);
	}

	public void setStatus(int code)
	{
		setStatus(code, UPnPStatus.code2String(code));
	}
	
	public UPnPStatus getStatus()
	{
		return upnpStatus;
	}

	private static final String DEFAULT_VALUE = "defaultValue";
	////////////////////////////////////////////////
	/**
	 * Get the value of DefaultValue of this StateVariable
	 * 
	 * @author Stefano Lenzi kismet-sl@users.sourceforge.net
	 */
	public String getDefaultValue() {
		return getStateVariableNode().getNodeValue(DEFAULT_VALUE);
	}

	/**
	 * This method ovverride the value of the DefaultValue of this object. <br>
	 * <br>
	 * Note: This method should be used to create a dynamic<br>
	 * Device withtout writing any XML that describe the device<br>.
	 * 
	 * @param value The new String value
	 * 
	 * @author Stefano Lenzi kismet-sl@users.sourceforge.net
	 */	
	public void setDefaultValue(String value){
		getStateVariableNode().setNode(DEFAULT_VALUE,value);
	}
	////////////////////////////////////////////////
	//	userData
	////////////////////////////////////////////////

	private Object userData = null; 
	
	public void setUserData(Object data) 
	{
		userData = data;
	}

	public Object getUserData() 
	{
		return userData;
	}
}
