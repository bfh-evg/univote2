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

import java.util.List;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlSeeAlso({CumulationRule.class, SummationRule.class})
@XmlType(propOrder = {"id", "optionIds", "lowerBound", "upperBound"})
public abstract class ElectionRule {

	private Integer id;
	private List<Integer> optionIds;
	private Integer lowerBound;
	private Integer upperBound;

	public ElectionRule() {
	}

	public ElectionRule(Integer id, List<Integer> optionIds, Integer lowerBound, Integer upperBound) {
		this.id = id;
		this.optionIds = optionIds;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Integer> getOptionIds() {
		return optionIds;
	}

	public void setOptionIds(List<Integer> optionIds) {
		this.optionIds = optionIds;
	}

	public Integer getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(Integer lowerBound) {
		this.lowerBound = lowerBound;
	}

	public Integer getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(Integer upperBound) {
		this.upperBound = upperBound;
	}
}
