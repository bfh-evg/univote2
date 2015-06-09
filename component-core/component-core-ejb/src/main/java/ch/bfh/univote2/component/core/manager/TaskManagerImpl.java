/*
 * UniVote2
 *
 *  UniVote2(tm): An Internet-based, verifiable e-voting system for student elections in Switzerland
 *  Copyright (c) 2015 Bern University of Applied Sciences (BFH),
 *  Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniVote2 may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH),
 *   Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: univote@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package ch.bfh.univote2.component.core.manager;

import static ch.bfh.unicrypt.helper.Alphabet.UPPER_CASE;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.RunActionTask;
import ch.bfh.univote2.component.core.data.Task;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.data.UserInputTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Singleton;

@Singleton
public class TaskManagerImpl implements TaskManager {

	@EJB
	ActionManager actionManager;

	Map<String, Task> tasks = new HashMap<>();

	@Override
	public List<Task> getTasks(String tenant) {
		List<Task> tenantTasks = new ArrayList<>();
		for (Task t : tasks.values()) {
			if (t.getTenant().equals(tenant)) {
				tenantTasks.add(t);
			}
		}
		return tenantTasks;
	}

	@Override
	public String addUserInputTask(UserInputTask userInputTask) {
		FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
		String notificationCode = fixedStringSet.getRandomElement().getValue();
		userInputTask.setNotificationCode(notificationCode);
		this.tasks.put(notificationCode, userInputTask);
		return notificationCode;
	}

	@Override
	public void addRunActionTask(RunActionTask runActionTask) {
		FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
		String notificationCode = fixedStringSet.getRandomElement().getValue();
		runActionTask.setNotificationCode(notificationCode);
		this.tasks.put(notificationCode, runActionTask);
	}

	@Override
	public void userInputReceived(String notificationCode, UserInput userInput) throws UnivoteException {
		if (this.tasks.containsKey(notificationCode)) {
			this.tasks.remove(notificationCode);
			this.actionManager.onUserInputNotification(notificationCode, userInput);
		} else {
			throw new UnivoteException("Unknown notification code: " + notificationCode);
		}
	}

	@Override
	public void runAction(String notificationCode) throws UnivoteException {
		if (this.tasks.containsKey(notificationCode)) {
			Task task = this.tasks.remove(notificationCode);
			if (task instanceof RunActionTask) {
				RunActionTask rATask = (RunActionTask) task;
				this.actionManager.runAction(rATask.getActionName(), rATask.getTenant(), rATask.getSection());
			} else {
				throw new UnivoteException("Is not an RunActionTask. Notification code: " + notificationCode);
			}
		} else {
			throw new UnivoteException("Unknown notification code: " + notificationCode);
		}
	}

	@Override
	public void runAction(String actionName, String tenant, String section) throws UnivoteException {
		this.actionManager.runAction(actionName, tenant, section);

	}

	// Methods used for testing
	protected Map<String, Task> getTasks() {
		return tasks;
	}

}
