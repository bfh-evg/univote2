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
package ch.bfh.univote2.component.core.manager;

import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.services.RegistrationService;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
public class RegistrationServiceMock implements RegistrationService {

	@Override
	public String register(String board, QueryDTO q) throws UnivoteException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void unregister(String board, String notificationCode) throws UnivoteException {
	}

	@Override
	public void unregisterUnknownNotification(String notificationCode) {
	}

}
