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
package ch.bfh.univote2.admin.message;

import ch.bfh.univote2.common.message.I18nText;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"id", "answer"})
public class VotingOption extends ElectionOption {

	private Integer id;
	private I18nText answer;

	public VotingOption() {
	}

	public VotingOption(Integer id, I18nText answer) {
		this.id = id;
		this.answer = answer;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public I18nText getAnswer() {
		return answer;
	}

	public void setAnswer(I18nText answer) {
		this.answer = answer;
	}
}
