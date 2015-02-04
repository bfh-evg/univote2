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

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.univote2.component.core.data.ActionContext;
import ch.bfh.univote2.component.core.data.UserInput;
import javax.ejb.Local;
import javax.ejb.Timeout;
import javax.ejb.Timer;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public interface NotificationManager {

	void onBoardNotification(String notificationCode, PostDTO post);

	@Timeout
	void onTimerNotification(Timer timer);

	void onUserInputNotification(String notificationCode, UserInput userInput);

	void runFinished(ActionContext actionContext);

	void runFailed(ActionContext actionContext);

	void runPartlyFinished(ActionContext actionContext);

}
