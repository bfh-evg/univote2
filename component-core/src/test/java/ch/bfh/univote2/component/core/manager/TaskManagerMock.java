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

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class TaskManagerMock implements TaskManager {

	@Override
	public String addTask(Task task) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<Task> getTasks(String tenant) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
