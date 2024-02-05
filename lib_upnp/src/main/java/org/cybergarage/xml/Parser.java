/******************************************************************
*
*	CyberXML for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: Parser.java
*
*	Revision;
*
*	11/26/2003
*		- first revision.
*	03/30/2005
*		- Change parse(String) to use StringBufferInputStream instead of URL.
*	11/11/2009
*		- Changed Parser::parser() to use ByteArrayInputStream instead of StringBufferInputStream because of bugs in Android v1.6.
*
******************************************************************/

package org.cybergarage.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.cybergarage.http.HTTP;
import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;

public abstract class Parser 
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public Parser()
	{
	}

	////////////////////////////////////////////////
	//	parse
	////////////////////////////////////////////////

	public abstract Node parse(InputStream inStream) throws ParserException;

	////////////////////////////////////////////////
	//	parse (URL)
	////////////////////////////////////////////////

	public Node parse(URL locationURL) throws ParserException
	{
		String host = locationURL.getHost();
		int port = locationURL.getPort();
		// Thanks for Hao Hu 
		if (port == -1)
			port = 80;
		String uri = locationURL.getPath();
		
		try {
	 		HttpURLConnection urlCon = (HttpURLConnection)locationURL.openConnection();
			urlCon.setRequestMethod("GET");
			urlCon.setRequestProperty(HTTP.CONTENT_LENGTH,"0");
			if (host != null)
				urlCon.setRequestProperty(HTTP.HOST, host);

			InputStream urlIn = urlCon.getInputStream();

			Node rootElem = parse(urlIn);
			
			urlIn.close();
			urlCon.disconnect();

			return rootElem;
			
		} catch (Exception e) {
			//throw new ParserException(e);
		}

		HTTPRequest httpReq = new HTTPRequest();
		httpReq.setMethod(HTTP.GET);
		httpReq.setURI(uri);
		HTTPResponse httpRes = httpReq.post(host, port);
		if (httpRes.isSuccessful() == false)
			throw new ParserException("HTTP comunication failed: no answer from peer." +
					"Unable to retrive resoure -> "+locationURL.toString());
		String content = new String(httpRes.getContent());
		ByteArrayInputStream strBuf = new ByteArrayInputStream(content.getBytes());
		return parse(strBuf);
	}

	////////////////////////////////////////////////
	//	parse (File)
	////////////////////////////////////////////////

	public Node parse(File descriptionFile) throws ParserException
	{
		try {
			InputStream fileIn = new FileInputStream(descriptionFile);
			Node root = parse(fileIn);
			fileIn.close();
			return root;
			
		} catch (Exception e) {
			throw new ParserException(e);
		}
	}

	////////////////////////////////////////////////
	//	parse (Memory)
	////////////////////////////////////////////////
	
	public Node parse(String descr) throws ParserException
	{
		try {
			InputStream decrIn = new ByteArrayInputStream(descr.getBytes());
			Node root = parse(decrIn);
			return root;
		} catch (Exception e) {
			throw new ParserException(e);
		}
	}

}


