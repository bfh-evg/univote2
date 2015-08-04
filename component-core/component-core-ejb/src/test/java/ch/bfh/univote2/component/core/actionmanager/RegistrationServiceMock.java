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
package ch.bfh.univote2.component.core.actionmanager;

import ch.bfh.uniboard.data.QueryDTO;
import static ch.bfh.unicrypt.helper.math.Alphabet.UPPER_CASE;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.services.RegistrationService;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
@LocalBean
public class RegistrationServiceMock implements RegistrationService {

	private QueryDTO lastRegistredQuery;
	private final List<String> lastUnregistredNotificationCode = new ArrayList();

	@Override
	public String register(String board, QueryDTO q) throws UnivoteException {
		this.lastRegistredQuery = q;
		FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
		return fixedStringSet.getRandomElement().getValue();
	}

	@Override
	public void unregister(String board, String notificationCode) throws UnivoteException {
		this.lastUnregistredNotificationCode.add(notificationCode);
	}

	@Override
	public void unregisterUnknownNotification(String notificationCode) {
		this.lastUnregistredNotificationCode.add(notificationCode);
	}

	public QueryDTO getLastRegistredQuery() {
		return lastRegistredQuery;
	}

	public boolean containsUnregistredNotificationCode(String notificationCode) {
		return lastUnregistredNotificationCode.remove(notificationCode);
	}

}
