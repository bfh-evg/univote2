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

import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({CandidateOption.class, ListOption.class, VotingOption.class})
public class ElectionOption {

	private Integer id;

	public ElectionOption() {
	}

	public ElectionOption(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
