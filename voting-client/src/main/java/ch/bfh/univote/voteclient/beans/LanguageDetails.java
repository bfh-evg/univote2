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
 */
package ch.bfh.univote.voteclient.beans;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.apache.tomcat.util.http.AcceptLanguage;

/**
 * Language Bean
 */
@ManagedBean(name="languageDetails")
@SessionScoped
public class LanguageDetails implements Serializable {
    private static final long serialVersionUID = 1L;

	private String locale = "";
	private static final String DEFAULT_LANGUAGE = "de";

	/**
	 * Set the locale
	 *
	 * @param locale
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * Returns the locale
	 *
	 * @return locale
	 */
	public String getLocale() {
		//No language set yet
		if ("".equals(locale)) {
			// Get the application context
			FacesContext facescontext = FacesContext.getCurrentInstance();
			// Get the request headers
			Map<String, String> requestHeaders = facescontext.getExternalContext().getRequestHeaderMap();
			// Read the supported languages
			String languages = requestHeaders.get("Accept-Language");
			Enumeration<?> langs = AcceptLanguage.getLocales(languages);

			String lang = "";
			boolean languageFound = false;
			do {
				//go through browser prefered languages
				if (langs.hasMoreElements()) {
					lang = ((Locale) langs.nextElement()).getLanguage().toLowerCase();
					if (lang.contains("de")) {
						lang = "de";
						languageFound = true;
					} else if (lang.contains("fr")) {
						lang = "fr";
						languageFound = true;
					} else if (lang.contains("en")) {
						lang = "en";
						languageFound = true;
					}
				} else {
					//default language
					lang = DEFAULT_LANGUAGE;
					languageFound = true;
				}

			} while (!languageFound);


			locale = lang;

		}
		return locale;
	}

	/**
	 * Returns the current datetime
	 *
	 * @return current datetime
	 */
	public Date getCurrentDate(){
		return Calendar.getInstance().getTime();
	}

}
