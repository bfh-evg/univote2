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
package ch.bfh.univote2.component.core.helper;

import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import javax.ejb.Local;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public interface RegistrationHelper {

	public String register(String board, QueryDTO q) throws UnivoteException;

	/**
	 * Unregisters the given notifcationCode on the specified board.
	 *
	 * @param board
	 * @param notificationCode
	 * @throws UnivoteException Throws an exception if the board could not be reached.
	 */

	public void unregister(String board, String notificationCode) throws UnivoteException;

	/**
	 * Tries to unregister the given notification code on every known board.
	 *
	 * @param notificationCode
	 */
	public void unregisterUnknownNotification(String notificationCode);
}
