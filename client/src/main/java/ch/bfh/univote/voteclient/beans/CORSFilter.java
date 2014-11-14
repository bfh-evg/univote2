/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.bfh.univote.voteclient.beans;

import java.io.*;
import javax.annotation.Priority;
import javax.servlet.*;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

/**
 * Class enabling cross origin requests
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
@PreMatching
public class CORSFilter implements ContainerRequestFilter, ContainerResponseFilter {

	public CORSFilter() { }

	public void init(FilterConfig fConfig) throws ServletException { }

	public void destroy() {	}

//	public void doFilter(
//		ServletRequest request, ServletResponse response, 
//		FilterChain chain) throws IOException, ServletException {
//
//		
//		HttpServletResponse res = (HttpServletResponse) response;
//		res.setHeader("Access-Control-Allow-Origin", "*");
////		res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
////		res.setHeader("Access-Control-Max-Age", "3600");
////		res.setHeader("Access-Control-Allow-Headers", "x-requested-with");
//		chain.doFilter(request, res);
//	}

    public void filter(ContainerRequestContext requestContext) throws IOException {
	
	requestContext.getHeaders().add("Access-Control-Allow-Origin", "*");
//	requestContext.getHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
//	requestContext.getHeaders().add("Access-Control-Max-Age", "3600");
//	requestContext.getHeaders().add("Access-Control-Allow-Headers", "x-requested-with");
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws
	    IOException {
//	requestContext.getHeaders().add("Access-Control-Allow-Origin", "*");
//	requestContext.getHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
//	requestContext.getHeaders().add("Access-Control-Max-Age", "3600");
//	requestContext.getHeaders().add("Access-Control-Allow-Headers", "x-requested-with");
	
	responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
//	responseContext.getHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
//	responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
//	responseContext.getHeaders().add("Access-Control-Allow-Headers", "x-requested-with");
    }
}