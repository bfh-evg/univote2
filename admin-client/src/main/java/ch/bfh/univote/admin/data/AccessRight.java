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

import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(propOrder = {"group", "crypto", "amount", "startTime", "endTime"})
public class AccessRight {

	private String group;
	private Crypto crypto;
	private Integer amount;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date startTime;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date endTime;

	public AccessRight() {
	}

	public AccessRight(String group, Crypto crypto) {
		this.group = group;
		this.crypto = crypto;
	}

	public AccessRight(String group, Crypto crypto, Integer amount, Date startTime, Date endTime) {
		this.group = group;
		this.crypto = crypto;
		this.amount = amount;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@XmlElement(required = true)
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@XmlElement(required = true)
	public Crypto getCrypto() {
		return crypto;
	}

	public void setCrypto(Crypto crypto) {
		this.crypto = crypto;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
}
