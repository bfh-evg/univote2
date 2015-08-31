/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniBoard.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote.admin.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"default", "defaultText", "de", "fr", "it", "en"})
public class I18nText {

	@XmlElement(name = "default")
	private String defaultText;
	private String de;
	private String fr;
	private String it;
	private String en;

	public I18nText() {
	}

	public I18nText(String defaultText) {
		this.defaultText = defaultText;
	}

	public I18nText(String defaultText, String de, String fr, String it, String en) {
		this.defaultText = defaultText;
		this.de = de;
		this.fr = fr;
		this.it = it;
		this.en = en;
	}

	public String getDefault() {
		return defaultText;
	}

	public void setDefault(String defaultText) {
		this.defaultText = defaultText;
	}

	public String getDe() {
		return de;
	}

	public void setDe(String de) {
		this.de = de;
	}

	public String getFr() {
		return fr;
	}

	public void setFr(String fr) {
		this.fr = fr;
	}

	public String getIt() {
		return it;
	}

	public void setIt(String it) {
		this.it = it;
	}

	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en;
	}
}
