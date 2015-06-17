<%-- 
/* 
 * Copyright (c) 2012 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 *
 *
 * Creats dynamically a JS object holding all the localized messages.
 *
 */
--%>
<%@page import="java.util.*"%>
<%@page import="ch.bfh.univote.voteclient.beans.LanguageDetails"%>
<%@page contentType="text/javascript" pageEncoding="UTF-8"%>
<%
	// Get users's locale and the corresponding message resource bundle
	LanguageDetails languageDetails = (LanguageDetails) request.getSession().getAttribute("languageDetails");
	ResourceBundle msg = ResourceBundle.getBundle("messages", new Locale(languageDetails.getLocale()));
	
	// Loop through the messages and create a string representing the js object
	// -> Of course this could be done automatically using a JSON library, but do
	//    it manually is more flexible and leaves the possibility of replacing 
	//    special chars and signs.
	StringBuilder json = new StringBuilder("var msg = {\n");
	Enumeration<String> keys = msg.getKeys();
	boolean first = true;
	while (keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		if ( first ) {
			first = false;
		} else {
			json.append(",\n");
		}
		json.append(key + ": '" + msg.getString(key).replace("'", "\\'").replaceAll("[\n\r]", "<br />") + "'");
	}
	
	json.append("};");
	
	// Finally output the string
	out.println(json.toString());
	out.println("var currentLanguage=\""+languageDetails.getLocale().toUpperCase()+"\";");
%>
