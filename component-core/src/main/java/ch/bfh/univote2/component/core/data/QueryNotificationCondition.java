/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote2.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.component.core.data;

import ch.bfh.uniboard.data.QueryDTO;

public class QueryNotificationCondition extends NotificationCondition {

	private final QueryDTO query;
	private final String board;

	public QueryNotificationCondition(QueryDTO query, String board) {
		this.query = query;
		this.board = board;
	}

	public QueryDTO getQuery() {
		return query;
	}

	public String getBoard() {
		return board;
	}

}
