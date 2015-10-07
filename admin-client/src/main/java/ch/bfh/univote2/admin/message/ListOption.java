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
import java.util.List;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"id", "number", "listName", "partyName", "candidateIds"})
public class ListOption extends ElectionOption {

	private Integer id;
	private String number;
	private I18nText listName;
	private I18nText partyName;
	private List<Integer> candidateIds;

	public ListOption() {
	}

	public ListOption(Integer id, String number, I18nText listName, I18nText partyName, List<Integer> candidateIds) {
		this.id = id;
		this.number = number;
		this.listName = listName;
		this.partyName = partyName;
		this.candidateIds = candidateIds;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public I18nText getListName() {
		return listName;
	}

	public void setListName(I18nText listName) {
		this.listName = listName;
	}

	public I18nText getPartyName() {
		return partyName;
	}

	public void setPartyName(I18nText partyName) {
		this.partyName = partyName;
	}

	public List<Integer> getCandidateIds() {
		return candidateIds;
	}

	public void setCandidateIds(List<Integer> candidateIds) {
		this.candidateIds = candidateIds;
	}
}
