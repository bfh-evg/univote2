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
package ch.bfh.univote2.component.core.action;

import ch.bfh.univote2.component.core.data.NotificationCondition;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public interface NotifiableAction extends Action {

	public List<NotificationCondition> getNotificationConditions(String tenant, String section);

	public void notifyAction(String tenant, String section, Object notification);

}