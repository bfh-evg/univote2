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

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class UserInputPreconditionQuery implements PreconditionQuery {

	private final Task userInputRequest;

	public UserInputPreconditionQuery(Task userInputRequest) {
		this.userInputRequest = userInputRequest;
	}

	public Task getUserInputRequest() {
		return userInputRequest;
	}

}
