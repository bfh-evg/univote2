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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(propOrder = {"title", "administration", "description", "votingPeriodBegin", "votingPeriodEnd"})
public class ElectionDefinition {

	private I18nText title;
	private I18nText administration;
	private I18nText description;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date votingPeriodBegin;
	@XmlJavaTypeAdapter(DateAdapter.class)
	private Date votingPeriodEnd;

	public ElectionDefinition() {
	}

	public ElectionDefinition(I18nText title, I18nText administration, I18nText description, Date votingPeriodBegin, Date votingPeriodEnd) {
		this.title = title;
		this.administration = administration;
		this.description = description;
		this.votingPeriodBegin = votingPeriodBegin;
		this.votingPeriodEnd = votingPeriodEnd;
	}

	public I18nText getTitle() {
		return title;
	}

	public void setTitle(I18nText title) {
		this.title = title;
	}

	public I18nText getAdministration() {
		return administration;
	}

	public void setAdministration(I18nText administration) {
		this.administration = administration;
	}

	public I18nText getDescription() {
		return description;
	}

	public void setDescription(I18nText description) {
		this.description = description;
	}

	public Date getVotingPeriodBegin() {
		return votingPeriodBegin;
	}

	public void setVotingPeriodBegin(Date votingPeriodBegin) {
		this.votingPeriodBegin = votingPeriodBegin;
	}

	public Date getVotingPeriodEnd() {
		return votingPeriodEnd;
	}

	public void setVotingPeriodEnd(Date votingPeriodEnd) {
		this.votingPeriodEnd = votingPeriodEnd;
	}
}
