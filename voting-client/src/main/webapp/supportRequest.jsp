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
 */
--%>
<%@page import="java.util.*"%>
<%@page import="ch.bfh.univote.voteclient.beans.LanguageDetails"%>
<%@page import="javax.mail.*"%>
<%@page import="javax.mail.internet.*"%>
<%@page import="javax.activation.*"%>
<%@page import="javax.naming.*"%>
<%@page contentType="text/javascript" pageEncoding="UTF-8"%>
<%
	// Line break used in email
	final String MAIL_LB = "\n";
	final String MAIL_SEPARATOR = MAIL_LB + MAIL_LB + "*******" + MAIL_LB + MAIL_LB;

	ServletContext context = getServletContext();
	String mailTo = context.getInitParameter("support-request-email-to");
	String subject = context.getInitParameter("support-request-email-subject");

	// Get form data
	String email = request.getParameter("email");
	String msgText = request.getParameter("message");
	String userAgent = request.getParameter("useragent");

	// Configure smtp server
	//JNDI Name: mail/MailSession
	//Mail Host: hermes.bfh.ch
	//Default User: virt-due1
	//Default Sender Address: selectio-helvetica@bfh.ch
	//Description: Registration-Mailer
	//Status: Enabled (true)
	//
	//Store Protocol: imap
	//Store Protocol Class: com.sun.mail.imap.IMAPStrore
	//TransportProtocol: smtp
	//Transport Protocol Class: com.sun.mail.smtp.SMTPTransport
	//
	//Additional Properties (4)
	//mail-smtp:port - 25
	//mail-smtp.password - *******
	//mail-smtp.auth - true
	//mail-smtp.starttls.enable - true
	// Get the default mail session object.
	//Session mailSession = Session.getDefaultInstance(properties);
	InitialContext ctx = new InitialContext();
	Session mailSession = (Session) ctx.lookup("mail/MailSession");

	try {
		// Put mail body together
		StringBuilder body = new StringBuilder();
		body.append(msgText + MAIL_SEPARATOR);
		body.append(email + MAIL_SEPARATOR);
		body.append(userAgent + MAIL_LB);

		System.out.println("Sending Message: " + body.toString());
		// Create message
		MimeMessage message = new MimeMessage(mailSession);
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
		message.setSentDate(new Date());
		message.setSubject(subject, "UTF-8");
		message.setText(body.toString(), "UTF-8");

		// Send message
		Transport.send(message);

		System.out.println("Message sent successfully");
		// Send response
		out.println("{\"success\": \"true\"}");

	} catch (Exception ex) {
		System.out.println("Sending message failed! " + ex);
		response.sendError(500, ex.getMessage());

	}
%>