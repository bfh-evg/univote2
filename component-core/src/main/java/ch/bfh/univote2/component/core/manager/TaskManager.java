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

import ch.bfh.univote2.component.core.data.Task;
import java.util.List;
import javax.ejb.Local;

/**
 * Manages the tasks for all the tenants in the system
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public interface TaskManager {

	/**
	 * Allows the NotificationManager to request a new task for an action.
	 *
	 * @param task defines an task that needs to be done by a tenant.
	 * @return NotificationCode returns the notification code set by the TaskManager.
	 */
	public String addTask(Task task);

	/**
	 * Returns all task available for a specified tenant
	 *
	 * @param tenant tenant the tasks returned are for
	 * @return a list of tasks for the specified tenant
	 */
	public List<Task> getTasks(String tenant);

}
