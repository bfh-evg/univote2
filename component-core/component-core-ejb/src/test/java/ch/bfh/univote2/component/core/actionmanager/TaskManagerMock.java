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

import static ch.bfh.unicrypt.helper.math.Alphabet.UPPER_CASE;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.data.RunActionTask;
import ch.bfh.univote2.component.core.data.Task;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.data.UserInputTask;
import ch.bfh.univote2.component.core.manager.TaskManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
public class TaskManagerMock implements TaskManager {

	Map<String, List<Task>> tasks = new HashMap<>();

	@Override
	public String addUserInputTask(UserInputTask userInputTask) {
		List<Task> t = new ArrayList<>();
		t.add(userInputTask);
		this.tasks.put(userInputTask.getTenant(), t);
		FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
		return fixedStringSet.getRandomElement().getValue();
	}

	@Override
	public List<Task> getTasks(String tenant) {
		return this.tasks.get(tenant);
	}

	@Override
	public void addRunActionTask(RunActionTask runActionTask) {
	}

	@Override
	public void userInputReceived(String notificationCode, UserInput userInput) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void runAction(String notificationCode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void runAction(String actionName, String tenant, String section) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
