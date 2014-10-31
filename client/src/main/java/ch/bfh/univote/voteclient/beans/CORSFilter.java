/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.bfh.univote.voteclient.beans;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Class enabling cross origin requests
 */
public class CORSFilter implements Filter {

	public CORSFilter() { }

	public void init(FilterConfig fConfig) throws ServletException { }

	public void destroy() {	}

	public void doFilter(
		ServletRequest request, ServletResponse response, 
		FilterChain chain) throws IOException, ServletException {

		((HttpServletResponse)response).addHeader(
			"Access-Control-Allow-Origin", "*"
		);
		chain.doFilter(request, response);
	}
}