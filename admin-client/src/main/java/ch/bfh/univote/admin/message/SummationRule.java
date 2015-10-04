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
package ch.bfh.univote.admin.message;

import java.util.List;

public class SummationRule extends ElectionRule {

	public SummationRule() {
	}

	public SummationRule(Integer id, List<Integer> optionIds, Integer lowerBound, Integer upperBound) {
		super(id, optionIds, lowerBound, upperBound);
	}
}
