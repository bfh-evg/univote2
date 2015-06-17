<%@page import="java.math.BigInteger"%>
<%@page import="ch.bfh.univote.voteclient.beans.util.RandomDistribution"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%><%
	
	byte[] random = RandomDistribution.getRandomValue(256/8);
	
	out.print((new BigInteger(1, random)).toString(2));
%>