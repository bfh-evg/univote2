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
package ch.bfh.univote.voteclient.parameters;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet responsible to load JNDI properties allowing to preconfigure the certificate request page with the already
 * defined values
 *
 * @author Philémon von Bergen &lt;philemon.vonbergen@bfh.ch&gt;
 */
@WebServlet("/parameters/*")
public class ParametersServlet extends HttpServlet {

    /**
     * The logger this servlet uses.
     */
    private static final Logger logger = Logger.getLogger(ParametersServlet.class.getName());

    private static final String PROPERTY_SET_IDENTIFIER = "params";

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

	    String propertiesSetIdentifier = request.getParameter(PROPERTY_SET_IDENTIFIER);
	    propertiesSetIdentifier = "/univote/" + propertiesSetIdentifier;
	    
	    ConfigurationHelperImpl config = new ConfigurationHelperImpl(propertiesSetIdentifier);

	    Gson gson = new Gson();
	    String parameters = gson.toJson(config);

	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");

	    response.getWriter().append(parameters);

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
	return "Servlet loading the properties needed in certificate request.";
    }

    /**
     * Error code returned for the case of an error while processing a request.
     *
     * @param response a HTTP response object
     * @param kind a detailed indication
     * @throws IOException if the response cannot be written
     */
    private void internalServerErrorHandler(HttpServletResponse response, String kind) throws IOException {
	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	response.setContentType("application/json");
	response.setCharacterEncoding("UTF-8");
	String errorCode = kind.substring(0, 3);
	String errorMessage;
	//Checks if error code is valid
	try {
	    Integer.parseInt(errorCode);
	    errorMessage = kind.substring(4);
	} catch (NumberFormatException e) {
	    errorMessage = kind;
	    errorCode = "";
	}
	response.getWriter().write("{\"error\": \"" + errorCode + "\", \"message\": \"" + errorMessage + "\"}");
    }
}
