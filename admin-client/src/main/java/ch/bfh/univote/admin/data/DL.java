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

@XmlType(name = "DL", propOrder = {"publickey", "p", "q", "g"})
public class DL extends Crypto {

	private String p;
	private String q;
	private String g;

	public DL() {
	}

	public DL(String p, String q, String g, String publickey) {
		super(publickey);
		this.p = p;
		this.q = q;
		this.g = g;
	}

	@XmlElement(required = true)
	public String getP() {
		return p;
	}

	public void setP(String p) {
		this.p = p;
	}

	@XmlElement(required = true)
	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	@XmlElement(required = true)
	public String getG() {
		return g;
	}

	public void setG(String g) {
		this.g = g;
	}
}
