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

public class CumulationRule extends ElectionRule {

	public CumulationRule() {
	}

	public CumulationRule(Integer id, List<Integer> optionIds, Integer lowerBound, Integer upperBound) {
		super(id, optionIds, lowerBound, upperBound);
	}
}
