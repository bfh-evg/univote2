<%--
/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniCert.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
--%>
<%@page import="javax.mail.util.ByteArrayDataSource"%>
<%@page import="java.text.MessageFormat"%>
<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.util.*"%>
<%@page import="ch.bfh.univote.voteclient.beans.LanguageDetails"%>
<%@page import="javax.mail.*"%>
<%@page import="javax.mail.internet.*"%>
<%@page import="javax.activation.*"%>
<%@page import="javax.naming.*"%>
<%@page contentType="text/javascript" pageEncoding="UTF-8"%>
<%

    final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    // Line break used in email
    final String MAIL_LB = "\n";

    // Get localized messges
    LanguageDetails languageDetails = (LanguageDetails) session.getAttribute("languageDetails");
    ResourceBundle msg = ResourceBundle.getBundle("messages", new Locale(languageDetails.getLocale()));

    // Read the secret key from the request
    String secretKey = request.getParameter("sk");
    String mailTo = request.getParameter("to");
    String mailTo2 = request.getParameter("to2");
    String confirmationTo = mailTo;

        // Configure smtp server -- see Configuration instructions.
    // Get the default mail session object.
    InitialContext ctx = new InitialContext();
    Session mailSession = (Session) ctx.lookup("mail/MailSession");

    try {

        // Put mail body together
        StringBuilder body = new StringBuilder();
        // 1. localized body text
        body.append(MessageFormat.format(msg.getString("skMailBody"), new Object[]{request.getParameter("appid"), request.getParameter("role"), request.getParameter("idp")}));
        // 2. spacer between body text and secret key
        body.append(MAIL_LB + MAIL_LB + "*******" + MAIL_LB + MAIL_LB);
        // 3. secret key (line by line to split the key into lines of max 60 chars)
        String[] skLines = secretKey.split("\n");
        for (int i = 0; i < skLines.length; i++) {
            String line = skLines[i];
            // key's pre- and postfix are added as they are
            if (line.startsWith("-----")) {
                body.append(line + MAIL_LB);
            } else {
                // split line into lines of 60 chars
                for (int start = 0; start < line.length(); start += 60) {
                    body.append(line.substring(start, Math.min(line.length(), start + 60)) + MAIL_LB);
                }
            }
        }
        body.append(MAIL_LB + MAIL_LB);

        Address[] to;
        if (pattern.matcher(mailTo2).matches()) {
            to = new Address[2];
            to[0] = new InternetAddress(mailTo);
            to[1] = new InternetAddress(mailTo2);
            confirmationTo += " " + msg.getString("andText") + " " + mailTo2;
        } else {
            to = new Address[1];
            to[0] = new InternetAddress(mailTo);
        }

        // Create message
        MimeMessage message = new MimeMessage(mailSession);
        //message.setFrom(new InternetAddress(MAIL_FROM_ADDRESS, MAIL_FROM_NAME));
        message.setRecipients(Message.RecipientType.TO, to);
        message.setSentDate(new Date());
        message.setSubject(msg.getString("skMailSubject"), "UTF-8");
        //message.setText(body.toString(), "UTF-8");
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body.toString(), "UTF-8");

        //Part one is text
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        // Part two is attachment
        MimeBodyPart attachmentPart = new MimeBodyPart();
	DataSource ds = new ByteArrayDataSource(request.getParameter("pem"), "application/x-pem-file");
        attachmentPart.setDataHandler(new DataHandler(ds));
        attachmentPart.setFileName("certificate.pem");
        
        multipart.addBodyPart(attachmentPart);

        // Put parts in message
        message.setContent(multipart);

        // Send message
        Transport.send(message);

        // Send response
        out.println("{\"message\": \"" + msg.getString("skMailSuccess") + "\", \"to\": \"" + confirmationTo + "\"}");

    } catch (Exception ex) {
        response.sendError(500, ex.getMessage());

    }
%>
