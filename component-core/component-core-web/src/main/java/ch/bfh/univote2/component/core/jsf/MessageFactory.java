/*
 * UniVote2
 *
 *  UniVote2(tm): An Internet-based, verifiable e-voting system for student elections in Switzerland
 *  Copyright (c) 2015 Bern University of Applied Sciences (BFH),
 *  Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniVote2 may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH),
 *   Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: univote@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package ch.bfh.univote2.component.core.jsf;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * The class MessageFactory is used to create faces messages.
 *
 * @author Stephan Fischli &lt;stephan.fischli@bfh.ch&gt;
 */
public class MessageFactory {

	public static FacesMessage getMessage(
			FacesMessage.Severity severity, String key, Object... params) {
		String summary = "???" + key + "???";
		String detail = null;
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			String name = context.getApplication().getMessageBundle();
			if (name == null) {
				name = FacesMessage.FACES_MESSAGES;
			}
			Locale locale = context.getViewRoot().getLocale();
			ResourceBundle bundle = ResourceBundle.getBundle(name, locale);
			summary = MessageFormat.format(bundle.getString(key), params);
			detail = MessageFormat.format(bundle.getString(key + "_detail"),
					params);
		} catch (MissingResourceException e) {
		}
		return new FacesMessage(severity, summary, detail);
	}

	public static void info(String key, Object... params) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, getMessage(FacesMessage.SEVERITY_INFO, key, params));
	}

	public static void error(String key, Object... params) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, getMessage(FacesMessage.SEVERITY_ERROR, key, params));
	}

	public static FacesMessage getMessage(
			FacesMessage.Severity severity, String name, String key, Object... params) {
		String summary = "???" + key + "???";
		String detail = null;
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			Locale locale = context.getViewRoot().getLocale();
			ResourceBundle bundle = ResourceBundle.getBundle(name, locale);
			summary = MessageFormat.format(bundle.getString(key), params);
			detail = MessageFormat.format(bundle.getString(key + "_detail"),
					params);
		} catch (MissingResourceException e) {
		}
		return new FacesMessage(severity, summary, detail);
	}

	public static void info(String bundle, String key, Object... params) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, getMessage(FacesMessage.SEVERITY_INFO, bundle, key, params));
	}

	public static void error(String bundle, String key, Object... params) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, getMessage(FacesMessage.SEVERITY_ERROR, bundle, key, params));
	}
}
