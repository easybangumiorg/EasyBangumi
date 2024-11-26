/******************************************************************
*
*	CyberXML for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: Element.java
*
*	Revision;
*
*	11/27/02
*		- first revision.
*	11/01/03
*		- Terje Bakken
*		- fixed missing escaping of reserved XML characters
*	11/19/04
*		- Theo Beisch <theo.beisch@gmx.de>
*		- Added "&" and "\"" "\\" to toXMLString().
*	11/19/04
*		- Theo Beisch <theo.beisch@gmx.de>
*		- Changed XML::output() to use short notation when the tag value is null.
*	12/02/04
*		- Brian Owens <brian@b-owens.com>
*		- Fixed toXMLString() to convert from "'" to "&apos;" instead of "\".
*	11/07/05
*		- Changed toString() to return as utf-8 string.
*	02/08/08
*		- Added addValue().
*
******************************************************************/

package org.cybergarage.xml;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class Node 
{

	/**
	 * Create a Node with empty UserData and no Parent Node
	 *
	 */
	public Node() 
	{
		setUserData(null);
		setParentNode(null);
	}

	public Node(String name) 
	{
		this();
		setName(name);
	}

	public Node(String ns, String name) 
	{
		this();
		setName(ns, name);
	}

	public Node(Node otherNode) 
	{
		this();
		set(otherNode);
	}
	
	////////////////////////////////////////////////
	//	parent node
	////////////////////////////////////////////////

	private Node parentNode = null; 
	
	public void setParentNode(Node node) 
	{
		parentNode = node;
	}

	public Node getParentNode() 
	{
		return parentNode;
	}

	////////////////////////////////////////////////
	//	root node
	////////////////////////////////////////////////

	public Node getRootNode() 
	{
		Node rootNode = null;
		Node parentNode = getParentNode();
		while (parentNode != null) {
			 rootNode = parentNode;
			 parentNode = rootNode.getParentNode();
		}
		return rootNode;
	}

	////////////////////////////////////////////////
	//	name
	////////////////////////////////////////////////

	private String name = new String(); 
	
	public void setName(String name) 
	{
		this.name = name;
	}

	public void setName(String ns, String name) 
	{
		this.name = ns + ":" + name;
	}

	public String getName() 
	{
		return name;
	}

	public boolean isName(String value)
	{
		return name.equals(value);	
	}
	
	////////////////////////////////////////////////
	//	value
	////////////////////////////////////////////////

	private String value = new String(); 
	
	public void setValue(String value) 
	{
		this.value = value;
	}

	public void setValue(int value) 
	{
		setValue(Integer.toString(value));
	}

	public void addValue(String value) 
	{
		if (this.value == null) {
			this.value = value;
			return;
		}
		if (value != null)
			this.value += value;
	}
	
	public String getValue()
	{
		return value;
	}

	////////////////////////////////////////////////
	//	Attribute (Basic)
	////////////////////////////////////////////////

	private AttributeList attrList = new AttributeList();

	public int getNAttributes() {
		return attrList.size();
	}

	public Attribute getAttribute(int index) {
		return attrList.getAttribute(index);
	}

	public Attribute getAttribute(String name) 
	{
		return attrList.getAttribute(name);
	}

	public void addAttribute(Attribute attr) {
		attrList.add(attr);
	}

	public void insertAttributeAt(Attribute attr, int index) {
		attrList.insertElementAt(attr, index);
	}

	public void addAttribute(String name, String value) {
		Attribute attr = new Attribute(name, value);
		addAttribute(attr);
	}

	public boolean removeAttribute(Attribute attr) {
		return attrList.remove(attr);
	}

	public boolean removeAttribute(String name) {
		return removeAttribute(getAttribute(name));
	}

	public void removeAllAttributes()
	{
		attrList.clear();
	}
	
	public boolean hasAttributes()
	{
		if (0 < getNAttributes())
			return true;
		return false;
	}

	////////////////////////////////////////////////
	//	Attribute (Extention)
	////////////////////////////////////////////////

	public void setAttribute(String name, String value) {
		Attribute attr = getAttribute(name);
		if (attr != null) {
			attr.setValue(value);
			return;
		}
		attr = new Attribute(name, value);
		addAttribute(attr);
	}

	public void setAttribute(String name, int value) {
		setAttribute(name, Integer.toString(value));
	}

	public String getAttributeValue(String name) {
		Attribute attr = getAttribute(name);
		if (attr != null)
			return attr.getValue();
		return "";
	}

	public int getAttributeIntegerValue(String name) {
		String val = getAttributeValue(name);
		try {
			return Integer.parseInt(val);
		}
		catch (Exception e) {}
		return 0;
	}
	
	////////////////////////////////////////////////
	//	Attribute (xmlns)
	////////////////////////////////////////////////

	public void setNameSpace(String ns, String value) 
	{
		setAttribute("xmlns:" + ns, value);
	}
		
	////////////////////////////////////////////////
	//	set
	////////////////////////////////////////////////
	
	public boolean set(Node otherNode) {
		if (otherNode == null)
			return false;
		
		setName(otherNode.getName());		
		setValue(otherNode.getValue());

		removeAllAttributes();
		int nOtherAttributes = otherNode.getNAttributes();
		for (int n=0; n<nOtherAttributes; n++) {
			Attribute otherAttr = otherNode.getAttribute(n);
			Attribute thisAttr = new Attribute(otherAttr);
			addAttribute(thisAttr);
		}
		
		removeAllNodes();
		int nOtherChildNodes = otherNode.getNNodes();
		for (int n=0; n<nOtherChildNodes; n++) {
			Node otherChildNode = otherNode.getNode(n);
			Node thisChildNode = new Node();
			thisChildNode.set(otherChildNode);
			addNode(thisChildNode);
		}
		
		return true;
	}
	
	////////////////////////////////////////////////
	//	equals
	////////////////////////////////////////////////
	
	public boolean equals(Node otherNode) {
		if (otherNode == null)
			return false;

		String thisNodeString = toString();
		String otherNodeString = otherNode.toString();
		
		return thisNodeString.equals(otherNodeString);
	}
	
	////////////////////////////////////////////////
	//	Child node
	////////////////////////////////////////////////

	private NodeList nodeList = new NodeList();

	public int getNNodes() {
		return nodeList.size();
	}

	public Node getNode(int index) {
		return nodeList.getNode(index);
	}

	public Node getNode(String name) 
	{
		return nodeList.getNode(name);
	}
	
	public Node getNodeEndsWith(String name) 
	{
		return nodeList.getEndsWith(name);
	}

	public void addNode(Node node) {
		node.setParentNode(this);
		nodeList.add(node);
	}

	public void insertNode(Node node, int index) {
		node.setParentNode(this);
		nodeList.insertElementAt(node, index);
	}

	public int getIndex(String name){
		int index = -1;
		for (Iterator i = nodeList.iterator(); i.hasNext();) {
			index++;
			Node n = (Node) i.next();
			if(n.getName().equals(name))
				return index;
		}
		return index;
	}

	public boolean removeNode(Node node) {
		node.setParentNode(null);
		return nodeList.remove(node);
	}

	public boolean removeNode(String name) {
		return nodeList.remove(getNode(name));
	}

	public void removeAllNodes()
	{
		nodeList.clear();
	}
	
	public boolean hasNodes()
	{
		if (0 < getNNodes())
			return true;
		return false;
	}
	
	////////////////////////////////////////////////
	//	Element (Child Node)
	////////////////////////////////////////////////

	public boolean hasNode(String name) {
		Node node = getNode(name);
		if (node != null) {
			return true;
		}
		return false;
	}
	
	public void setNode(String name) {
		if (hasNode(name)) {
			return;
		}
		Node node = new Node(name);
		addNode(node);
	}
	
	public void setNode(String name, String value) {
		Node node = getNode(name);
		if (node == null) {
			node = new Node(name);
			addNode(node);
		}
		node.setValue(value);
	}

	public String getNodeValue(String name) {
		Node node = getNode(name);
		if (node != null)
			return node.getValue();
		return "";
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
	
	////////////////////////////////////////////////
	//	toString 
	////////////////////////////////////////////////

	/**
	 * Inovoke {@link #getIndentLevelString(int, String)} with <code>"   "</code> as String 
	 * 
	 * @see #getIndentLevelString(int, String)
	 */
	public String getIndentLevelString(int nIndentLevel) 
	{
		return getIndentLevelString(nIndentLevel,"   ");
	}

	/**
	 * 
	 * @param nIndentLevel the level of indentation to produce 
	 * @param space the String to use for the intendation 
	 * @since 1.8.0
	 * @return an indentation String
	 */
	public String getIndentLevelString(int nIndentLevel,String space) 
	{
		StringBuffer indentString = new StringBuffer(nIndentLevel*space.length()); 
		for (int n=0; n<nIndentLevel; n++){
			indentString.append(space);
		}
		return indentString.toString();
	}	
	
	public void outputAttributes(PrintWriter ps)
	{
		int nAttributes = getNAttributes();
		for (int n=0; n<nAttributes; n++) {
			Attribute attr = getAttribute(n);
			ps.print(" " + attr.getName() + "=\"" + XML.escapeXMLChars(attr.getValue()) + "\"");
		}
	}

	public void output(PrintWriter ps, int indentLevel, boolean hasChildNode) 
	{
		String indentString = getIndentLevelString(indentLevel);

		String name = getName();
		String value = getValue();

		if (hasNodes() == false || hasChildNode == false) {		
			ps.print(indentString + "<" + name);
			outputAttributes(ps);
			// Thnaks for Tho Beisch (11/09/04)
			if (value == null || value.length() == 0) {
				// Not using the short notation <node /> because it cause compatibility trouble
				ps.println("></" + name + ">");
			} else {
				ps.println(">" + XML.escapeXMLChars(value) + "</" + name + ">");
			}
			
			return;
		}
		
		ps.print(indentString + "<" + name);
		outputAttributes(ps);
		ps.println(">");
	
		int nChildNodes = getNNodes();
		for (int n=0; n<nChildNodes; n++) {
			Node cnode = getNode(n);
			cnode.output(ps, indentLevel+1, true);
		}

		ps.println(indentString +"</" + name + ">");
	}

	public String toString(String enc, boolean hasChildNode)
	{
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		PrintWriter pr = new PrintWriter(byteOut);
		output(pr, 0, hasChildNode);
		pr.flush();
		try {
			if (enc != null && 0 < enc.length())
				return byteOut.toString(enc);
		}
		catch (UnsupportedEncodingException e) {
		}
		return byteOut.toString();
	}
		
	public String toString()
	{
		return toString(XML.CHARSET_UTF8, true);
	}
	
	public String toXMLString(boolean hasChildNode)
	{
		String xmlStr = toString();
		xmlStr = xmlStr.replaceAll("<", "&lt;");	
		xmlStr = xmlStr.replaceAll(">", "&gt;");	
		// Thanks for Theo Beisch (11/09/04)
		xmlStr = xmlStr.replaceAll("&", "&amp;");	
		xmlStr = xmlStr.replaceAll("\"", "&quot;");	
		// Thanks for Brian Owens (12/02/04)
		xmlStr = xmlStr.replaceAll("'", "&apos;");	
		return xmlStr;
	}

	public String toXMLString()
	{
		return toXMLString(true);
	}
	
	public void print(boolean hasChildNode)
	{
		PrintWriter pr = new PrintWriter(System.out);
		output(pr, 0, hasChildNode);
		pr.flush();
	}

	public void print()
	{
		print(true);
	}
}
